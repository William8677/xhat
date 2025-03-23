package com.williamfq.xhat.ui.stories.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.*
import com.williamfq.domain.repository.StoryRepository
import com.williamfq.domain.repository.UserRepository
import com.williamfq.xhat.core.ads.AdType
import com.williamfq.xhat.core.config.AdMobConfig
import com.williamfq.xhat.ui.stories.*
import com.williamfq.xhat.ui.stories.components.StoryEffect
import com.williamfq.xhat.ui.stories.model.CompletionStatus
import com.williamfq.xhat.ui.stories.model.StoryMetrics
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.metrics.EngagementType
import com.williamfq.xhat.utils.metrics.StoryMetricsTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StoriesViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepositoryInterface,
    private val shareManager: ShareManagerInterface,
    private val mediaDownloadManager: MediaDownloadManagerInterface,
    private val mediaCompressor: MediaCompressorInterface,
    private val e2eEncryption: E2EEncryptionInterface,
    val nativeStoryAdManager: NativeStoryAdManagerInterface,
    private val analyticsManager: AnalyticsManagerInterface,
    private val metricsTracker: StoryMetricsTracker,
    private val logger: LoggerInterface
) : ViewModel() {

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _selectedStory = MutableStateFlow<Story?>(null)
    val selectedStory: StateFlow<Story?> = _selectedStory.asStateFlow()

    private val _storyMetrics = MutableStateFlow<Map<Int, StoryMetrics>>(emptyMap())
    val storyMetrics: StateFlow<Map<Int, StoryMetrics>> = _storyMetrics.asStateFlow()

    private val _activeStoryEffects = MutableStateFlow<List<StoryEffect>>(emptyList())
    val activeStoryEffects: StateFlow<List<StoryEffect>> = _activeStoryEffects.asStateFlow()

    private val _replyChains = MutableStateFlow<Map<Int, List<StoryReply>>>(emptyMap())
    val replyChains: StateFlow<Map<Int, List<StoryReply>>> = _replyChains.asStateFlow()

    private val _currentStoryIndex = MutableStateFlow(0)
    val currentStoryIndex: StateFlow<Int> = _currentStoryIndex

    private val _showingAd = MutableStateFlow(false)
    val showingAd: StateFlow<Boolean> = _showingAd

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _mediaCache = MutableStateFlow<Map<String, Uri>>(emptyMap())
    private val _compressionQueue = MutableStateFlow<List<MediaCompressionTask>>(emptyList())
    private val _encryptedContent = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _viewerMetrics = MutableStateFlow<Map<Int, ViewerMetrics>>(emptyMap())

    private var storyLoadingJob: Job? = null
    private var mediaPreloadingJob: Job? = null
    private var metricTrackingJob: Job? = null
    private var compressionJob: Job? = null

    companion object {
        private const val TAG = "StoriesViewModel"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val STORIES_PAGE_SIZE = 10
        private const val MAX_STORY_SIZE_MB = 50
        private const val PRELOAD_THRESHOLD = 3
        private const val COMPRESSION_QUALITY = 85
        private const val METRICS_UPDATE_INTERVAL = 1000L
        private const val CACHE_SIZE_MB = 100
    }

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            try {
                initializeCurrentUser()
                initializeE2EEncryption()
                initializeMediaCache()
                initializeMetricsTracking()
                loadStories()
                startMetricsTracking()
                startCompressionQueue()
                nativeStoryAdManager.resetSession()
                Timber.d("StoriesViewModel inicializado correctamente")
            } catch (e: Exception) {
                handleError(e, "Error inicializando StoriesViewModel")
            }
        }
    }

    fun loadStories() {
        storyLoadingJob?.cancel()
        storyLoadingJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val initialStories = withContext(Dispatchers.IO) {
                    storyRepository.getStories()
                }
                val moreStories = withContext(Dispatchers.IO) {
                    storyRepository.getMoreStories(STORIES_PAGE_SIZE)
                }
                _stories.value = initialStories + moreStories
                validateStoryDurations(_stories.value)
                initializeStoryMetrics(_stories.value)
                preloadStoryMedia(_stories.value)
                _replyChains.value = _stories.value.associate { it.id to emptyList() }
                Timber.d("Historias cargadas: ${initialStories.size + moreStories.size}")
            } catch (e: Exception) {
                handleError(e, "Error cargando historias")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun initializeCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUserId.value = userRepository.getCurrentUserId()
                analyticsManager.logEvent("stories_viewer_initialized", Bundle().apply {
                    putString("user_id", _currentUserId.value)
                    putLong("timestamp", System.currentTimeMillis())
                })
                Timber.d("Usuario actual inicializado: ${_currentUserId.value}")
            } catch (e: Exception) {
                handleError(e, "Error inicializando usuario actual")
            }
        }
    }

    private suspend fun initializeMediaCache() {
        try {
            withContext(Dispatchers.IO) {
                mediaDownloadManager.clearCache()
                mediaDownloadManager.setCacheSize(CACHE_SIZE_MB * 1024 * 1024L)
                mediaDownloadManager.setCompressionQuality(COMPRESSION_QUALITY)
            }
            Timber.d("Caché de medios inicializado")
        } catch (e: Exception) {
            handleError(e, "Error inicializando caché de medios")
        }
    }

    private suspend fun initializeE2EEncryption() {
        try {
            _currentUserId.value?.let { userId ->
                e2eEncryption.initialize(userId)
                _encryptedContent.value = mapOf("user_$userId" to e2eEncryption.encrypt("Stories init"))
                Timber.d("Cifrado E2E inicializado para usuario: $userId")
            }
        } catch (e: Exception) {
            handleError(e, "Error inicializando cifrado E2E")
        }
    }

    private suspend fun initializeMetricsTracking() {
        try {
            _currentUserId.value?.let { userId ->
                metricsTracker.initializeTracking(userId)
                viewModelScope.launch {
                    logger.logEvent(TAG, "Seguimiento de métricas inicializado para usuario $userId", LogLevel.INFO)
                }
            }
        } catch (e: Exception) {
            handleError(e, "Error inicializando seguimiento de métricas")
        }
    }

    private fun initializeStoryMetrics(stories: List<Story>) {
        val metricsMap = stories.associate { story ->
            story.id to StoryMetrics(
                viewCount = story.views,
                reactionCount = story.reactions.size,
                commentCount = story.comments.size,
                lastViewed = story.timestamp,
                completionStatus = if (story.isActive) CompletionStatus.VIEWING else CompletionStatus.COMPLETED
            )
        }
        _storyMetrics.value = metricsMap
        viewModelScope.launch {
            logger.logEvent(TAG, "Métricas inicializadas para ${storyMetrics.value.size} historias", LogLevel.INFO)
        }
    }

    private suspend fun preloadStoryMedia(stories: List<Story>) {
        stories.take(PRELOAD_THRESHOLD).forEach { story ->
            try {
                when (story.mediaType) {
                    MediaType.IMAGE -> mediaDownloadManager.preloadImage(story.mediaUrl ?: return@forEach)
                    MediaType.VIDEO -> mediaDownloadManager.preloadVideo(story.mediaUrl ?: return@forEach, MAX_STORY_SIZE_MB)
                    MediaType.AUDIO -> mediaDownloadManager.preloadAudio(story.mediaUrl ?: return@forEach)
                    MediaType.DOCUMENT -> mediaDownloadManager.preloadDocument(story.mediaUrl ?: return@forEach)
                    MediaType.AR -> mediaDownloadManager.preloadAR(story.mediaUrl ?: return@forEach)
                    MediaType.LOCATION -> mediaDownloadManager.preloadLocation(story.mediaUrl ?: return@forEach)
                    MediaType.CONTACT -> mediaDownloadManager.preloadContact(story.mediaUrl ?: return@forEach)
                    MediaType.POLL -> mediaDownloadManager.preloadPoll(story.mediaUrl ?: return@forEach)
                    MediaType.QUIZ -> mediaDownloadManager.preloadQuiz(story.mediaUrl ?: return@forEach)
                    MediaType.TEXT -> {}
                    MediaType.LINK -> mediaDownloadManager.preloadImage(story.mediaUrl ?: return@forEach)
                    MediaType.CUSTOM -> {}
                }
                Timber.d("Medios precargados para historia: ${story.id}")
            } catch (e: Exception) {
                viewModelScope.launch {
                    logger.logEvent(TAG, "Error precargando medios para historia ${story.id}", LogLevel.ERROR, e)
                }
            }
        }
    }

    private fun validateStoryDurations(stories: List<Story>) {
        stories.forEach { story ->
            val durationSeconds = (story.durationSeconds ?: AdMobConfig.DEFAULT_STORY_DURATION_SECONDS)
            if (durationSeconds < AdMobConfig.MIN_STORY_DURATION_SECONDS) {
                viewModelScope.launch {
                    logger.logEvent(TAG, "Historia ${story.id} duración demasiado corta: $durationSeconds", LogLevel.WARNING)
                }
            }
            if (durationSeconds > AdMobConfig.MAX_STORY_DURATION_SECONDS) {
                viewModelScope.launch {
                    logger.logEvent(TAG, "Historia ${story.id} duración demasiado larga: $durationSeconds", LogLevel.WARNING)
                }
            }
        }
    }

    fun selectStory(story: Story) {
        viewModelScope.launch {
            try {
                _selectedStory.value = story
                _currentStoryIndex.value = _stories.value.indexOf(story)

                if (nativeStoryAdManager.isAdLoading.value) {
                    viewModelScope.launch {
                        logger.logEvent(TAG, "Anuncio cargando: ${nativeStoryAdManager.isAdLoading.value}", LogLevel.INFO)
                    }
                }

                val showAd = nativeStoryAdManager.shouldShowAd(_currentStoryIndex.value)
                _showingAd.value = showAd
                if (showAd) {
                    _uiEvent.value = UiEvent.ShowAd(AdType.NATIVE)
                    nativeStoryAdManager.markAdAsShown()
                }

                _uiState.value = UiState.ViewingStory(
                    story = story,
                    index = _currentStoryIndex.value,
                    showingAd = showAd
                )

                _activeStoryEffects.value = listOf(StoryEffect.Sparkle(Offset(100f, 100f)))
                _uiEvent.value = UiEvent.ShowEffect(_activeStoryEffects.value.first())

                _currentUserId.value?.let { userId ->
                    messageRepository.sendMessage(
                        senderId = userId,
                        receiverId = story.userId,
                        chatId = "chat_${story.id}",
                        content = "Visto historia ${story.id}",
                        senderName = "Usuario",
                        type = MessageType.TEXT
                    )
                    shareManager.importContent("Mira esta historia: ${story.title}")
                }

                updateStoryMetrics(story.id)
                metricsTracker.trackEngagement(story.id, EngagementType.VIEW)

                if (story.privacy == PrivacyLevel.CUSTOM) {
                    _uiState.value = UiState.SelectUsers(story)
                    _uiEvent.value = UiEvent.NavigateToUserSelection
                }
                Timber.d("Historia seleccionada: ${story.id}")
            } catch (e: Exception) {
                handleError(e, "Error seleccionando historia")
            }
        }
    }

    fun closeStory() {
        viewModelScope.launch {
            try {
                _selectedStory.value?.let { story ->
                    updateStoryMetrics(story.id, completionStatus = CompletionStatus.COMPLETED)
                    delay(AdMobConfig.DEFAULT_STORY_DURATION_SECONDS * 1000L)
                }
                _selectedStory.value = null
                _showingAd.value = false
                _uiState.value = UiState.Initial
                Timber.d("Historia cerrada")
            } catch (e: Exception) {
                handleError(e, "Error cerrando historia")
            }
        }
    }

    fun handleReaction(
        storyId: Int,
        reaction: Reaction,
        reactionId: String = UUID.randomUUID().toString()
    ) {
        viewModelScope.launch {
            var attempts = 0
            while (attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    val updatedReaction = reaction.copy(
                        id = reactionId,
                        content = if (reaction.isPrivate) {
                            e2eEncryption.encrypt(reaction.content)
                        } else reaction.content
                    )

                    storyRepository.addReaction(storyId, updatedReaction)
                    updateStoryMetrics(storyId, reactionAdded = true)
                    metricsTracker.trackEngagement(storyId, EngagementType.REACTION)
                    Timber.d("Reacción añadida a historia: $storyId")
                    break
                } catch (e: Exception) {
                    attempts++
                    if (attempts == MAX_RETRY_ATTEMPTS) {
                        handleError(e, "Error añadiendo reacción tras $MAX_RETRY_ATTEMPTS intentos")
                    }
                    delay(1000L)
                }
            }
        }
    }

    fun handleComment(
        storyId: Int,
        commentText: String,
        commentId: String = UUID.randomUUID().toString()
    ) {
        viewModelScope.launch {
            try {
                val comment = Comment(
                    id = commentId,
                    content = commentText,
                    userId = _currentUserId.value ?: throw IllegalStateException("Usuario no identificado"),
                    timestamp = System.currentTimeMillis(),
                    isPrivate = false,
                    mentions = extractMentions(commentText),
                    attachments = extractAttachments(commentText)
                )

                storyRepository.addComment(storyId, comment.id, comment.content)
                updateStoryMetrics(storyId, commentAdded = true)

                if (commentText.startsWith("@")) {
                    initializeReplyChain(storyId, comment)
                }

                metricsTracker.trackEngagement(storyId, EngagementType.COMMENT)
                Timber.d("Comentario añadido a historia: $storyId")
            } catch (e: Exception) {
                handleError(e, "Error añadiendo comentario")
            }
        }
    }

    fun addInteraction(storyId: Int, interaction: StoryInteraction) {
        viewModelScope.launch {
            try {
                storyRepository.addInteraction(storyId, interaction)
                updateStoryMetrics(storyId, viewed = interaction.type == InteractionType.VIEW)
                metricsTracker.trackEngagement(storyId, when (interaction.type) {
                    InteractionType.VIEW -> EngagementType.VIEW
                    InteractionType.REPLY -> EngagementType.REPLY
                    InteractionType.SHARE -> EngagementType.SHARE
                    InteractionType.SAVE -> EngagementType.SAVE
                    InteractionType.REPORT -> EngagementType.REPORT
                    InteractionType.QUIZ_ANSWER -> EngagementType.QUIZ_ANSWER
                    InteractionType.POLL_VOTE -> EngagementType.POLL_VOTE
                    InteractionType.AR_INTERACTION -> EngagementType.AR_INTERACTION
                    InteractionType.LINK_CLICK -> EngagementType.LINK_CLICK
                    InteractionType.SWIPE_UP -> EngagementType.SWIPE_UP
                    InteractionType.CUSTOM_ACTION -> EngagementType.COMMENT
                })
                Timber.d("Interacción añadida a historia: $storyId, tipo: ${interaction.type}")
            } catch (e: Exception) {
                handleError(e, "Error añadiendo interacción")
            }
        }
    }

    private fun startMetricsTracking() {
        metricTrackingJob?.cancel()
        metricTrackingJob = viewModelScope.launch {
            while (isActive) {
                _selectedStory.value?.let { story ->
                    trackViewerMetrics(story.id)
                }
                delay(METRICS_UPDATE_INTERVAL)
            }
        }
    }

    private suspend fun trackViewerMetrics(storyId: Int) {
        try {
            val currentMetrics = _viewerMetrics.value[storyId] ?: ViewerMetrics()
            val updatedMetrics = currentMetrics.copy(
                viewDuration = currentMetrics.viewDuration + METRICS_UPDATE_INTERVAL,
                interactionCount = currentMetrics.interactionCount + 1,
                lastUpdated = System.currentTimeMillis()
            )
            _viewerMetrics.value = _viewerMetrics.value + (storyId to updatedMetrics)

            metricsTracker.trackStoryView(storyId, updatedMetrics.viewDuration)
            if (updatedMetrics.viewDuration >= AdMobConfig.MIN_AD_VISIBILITY_TIME_MS) {
                metricsTracker.trackEngagement(storyId, EngagementType.VIEW)
            }
            Timber.d("Métricas de visor actualizadas para historia: $storyId")
        } catch (e: Exception) {
            handleError(e, "Error actualizando métricas de visor")
        }
    }

    private fun startCompressionQueue() {
        compressionJob?.cancel()
        compressionJob = viewModelScope.launch {
            _compressionQueue.collect { queue ->
                queue.firstOrNull()?.let { task ->
                    try {
                        val compressedUri = when (task.type) {
                            MediaType.IMAGE -> mediaCompressor.compressImage(task.uri, maxWidth = 1920, maxHeight = 1080, quality = COMPRESSION_QUALITY, format = Bitmap.CompressFormat.JPEG, applyBlur = false, blurRadius = 0f, addWatermark = false, watermarkText = null)
                            MediaType.VIDEO -> mediaCompressor.compressVideo(task.uri, targetBitrate = 5000000, targetFrameRate = 30, targetResolution = null, includeAudio = true, enableHEVC = false, addWatermark = false)
                            MediaType.AUDIO -> mediaCompressor.compressAudio(task.uri)
                            MediaType.DOCUMENT -> mediaCompressor.compressDocument(task.uri)
                            else -> task.uri
                        }
                        _mediaCache.value = _mediaCache.value + (task.id to compressedUri)
                        _compressionQueue.value = queue.drop(1)
                        Timber.d("Medio comprimido: ${task.id}")
                    } catch (e: Exception) {
                        handleError(e, "Error comprimiendo medio")
                        _compressionQueue.value = queue.drop(1)
                    }
                }
            }
        }
    }

    private fun updateStoryMetrics(
        storyId: Int,
        viewed: Boolean = false,
        reactionAdded: Boolean = false,
        commentAdded: Boolean = false,
        completionStatus: CompletionStatus? = null
    ) {
        val currentMetrics = _storyMetrics.value.toMutableMap()
        val storyMetrics = currentMetrics[storyId] ?: StoryMetrics()

        currentMetrics[storyId] = storyMetrics.copy(
            viewCount = if (viewed) storyMetrics.viewCount + 1 else storyMetrics.viewCount,
            reactionCount = if (reactionAdded) storyMetrics.reactionCount + 1 else storyMetrics.reactionCount,
            commentCount = if (commentAdded) storyMetrics.commentCount + 1 else storyMetrics.commentCount,
            lastViewed = if (viewed) System.currentTimeMillis() else storyMetrics.lastViewed,
            completionStatus = completionStatus ?: storyMetrics.completionStatus
        )

        _storyMetrics.value = currentMetrics
    }

    private suspend fun initializeReplyChain(storyId: Int, comment: Comment) {
        try {
            val currentChains = _replyChains.value.toMutableMap()
            val newReply = StoryReply(
                id = comment.id,
                content = comment.content,
                timestamp = comment.timestamp,
                userId = comment.userId,
                mentions = comment.mentions,
                attachments = comment.attachments.map { it.url }
            )
            currentChains[storyId] = (currentChains[storyId] ?: emptyList()) + newReply
            _replyChains.value = currentChains
            Timber.d("Cadena de respuestas inicializada para historia: $storyId")
        } catch (e: Exception) {
            handleError(e, "Error inicializando cadena de respuestas")
        }
    }

    private fun extractMentions(commentText: String): List<String> {
        return try {
            Regex("@\\w+").findAll(commentText).map { it.value }.toList()
        } catch (e: Exception) {
            Timber.e(e, "Error extrayendo menciones")
            emptyList()
        }
    }

    private fun extractAttachments(commentText: String): List<Attachment> {
        return try {
            Regex("https?://\\S+").findAll(commentText).map { url ->
                Attachment(
                    id = UUID.randomUUID().toString(),
                    type = when {
                        url.value.endsWith(".jpg") || url.value.endsWith(".png") -> AttachmentType.IMAGE
                        url.value.endsWith(".mp4") -> AttachmentType.VIDEO
                        url.value.endsWith(".mp3") -> AttachmentType.AUDIO
                        url.value.endsWith(".pdf") -> AttachmentType.DOCUMENT
                        url.value.endsWith(".gif") -> AttachmentType.GIF
                        else -> AttachmentType.STICKER
                    },
                    url = url.value
                )
            }.toList()
        } catch (e: Exception) {
            Timber.e(e, "Error extrayendo adjuntos")
            emptyList()
        }
    }

    private fun handleError(error: Exception, message: String) {
        viewModelScope.launch {
            logger.logEvent(TAG, message, LogLevel.ERROR, error)
            _error.value = error.message ?: message
            _uiEvent.value = UiEvent.ShowSnackbar(message)
            Timber.e(error, message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        storyLoadingJob?.cancel()
        mediaPreloadingJob?.cancel()
        metricTrackingJob?.cancel()
        compressionJob?.cancel()
        viewModelScope.launch {
            try {
                mediaDownloadManager.clearCache()
                mediaCompressor.clearCache()
                _selectedStory.value?.id?.let { metricsTracker.generateReport(it) }
                nativeStoryAdManager.release()
                Timber.d("StoriesViewModel limpiado")
            } catch (e: Exception) {
                logger.logEvent(TAG, "Error en onCleared", LogLevel.ERROR, e)
            }
        }
    }

    data class MediaCompressionTask(
        val id: String,
        val uri: Uri,
        val type: MediaType
    )

    data class StoryReply(
        val id: String,
        val content: String,
        val timestamp: Long,
        val userId: String,
        val mentions: List<String> = emptyList(),
        val attachments: List<String> = emptyList()
    )

    data class ViewerMetrics(
        val viewDuration: Long = 0L,
        val interactionCount: Int = 0,
        val lastUpdated: Long = System.currentTimeMillis(),
        val completionStatus: CompletionStatus = CompletionStatus.VIEWING
    )

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateToUserSelection : UiEvent()
        data class ShowAd(val adType: AdType) : UiEvent()
        data class ShowEffect(val effect: StoryEffect) : UiEvent()
    }

    sealed class UiState {
        object Initial : UiState()
        data class SelectUsers(val story: Story) : UiState()
        data class ViewingStory(
            val story: Story,
            val index: Int,
            val showingAd: Boolean
        ) : UiState()
    }
}