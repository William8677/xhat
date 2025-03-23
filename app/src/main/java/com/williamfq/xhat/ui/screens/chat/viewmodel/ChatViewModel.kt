package com.williamfq.xhat.ui.screens.chat.viewmodel

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.MessageType
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.repository.ChatRepository
import com.williamfq.xhat.service.WalkieTalkieService
import com.williamfq.xhat.service.WalkieTalkieState
import com.williamfq.xhat.ui.screens.chat.model.ChatMenuOption
import com.williamfq.xhat.ui.screens.chat.model.ChatUiState
import com.williamfq.xhat.utils.VoiceRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val walkieTalkieService: WalkieTalkieService,
    private val voiceRecorder: VoiceRecorder
) : ViewModel(), ChatViewModelInterface {

    private val _uiState = MutableStateFlow(ChatUiState())
    override val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var currentChatId: String? = null
    private var recordingJob: kotlinx.coroutines.Job? = null
    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        initializeCollectors()
    }

    private fun initializeCollectors() {
        viewModelScope.launch {
            combine(
                walkieTalkieService.walkieTalkieState, // Flow<WalkieTalkieState>
                chatRepository.observeMessages()       // Flow<List<ChatMessage>>
            ) { walkieTalkieState, messages ->
                _uiState.update { state ->
                    state.copy(
                        walkieTalkieState = walkieTalkieState,
                        messages = messages
                    )
                }
            }.collect()
        }
    }

    override fun loadChat(chatId: String) {
        currentChatId = chatId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val chatInfo = chatRepository.getChatInfo(chatId)
                val messages = chatRepository.getChatMessages(chatId)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        chatTitle = chatInfo.title,
                        messages = messages,
                        chatInfo = chatInfo
                    )
                }
            } catch (e: Exception) {
                handleError("Error al cargar el chat", e)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteMessage(messageId)
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.filter { it.messageId != messageId },
                        selectedMessage = null,
                        showMessageOptions = false
                    )
                }
            } catch (e: Exception) {
                handleError("Error al eliminar mensaje", e)
            }
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        if (newContent.isBlank()) return

        viewModelScope.launch {
            try {
                chatRepository.editMessage(messageId, newContent)
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map { message ->
                            if (message.messageId == messageId) {
                                message.copy(
                                    content = newContent,
                                    isEdited = true
                                )
                            } else {
                                message
                            }
                        },
                        selectedMessage = null,
                        showMessageOptions = false,
                        isEditing = false
                    )
                }
            } catch (e: Exception) {
                handleError("Error al editar mensaje", e)
            }
        }
    }

    fun startEditing(message: ChatMessage) {
        _uiState.update {
            it.copy(
                selectedMessage = message,
                isEditing = true,
                currentMessage = message.content
            )
        }
    }

    fun cancelEditing() {
        _uiState.update {
            it.copy(
                selectedMessage = null,
                isEditing = false,
                currentMessage = ""
            )
        }
    }

    override fun onMessageChange(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    override fun onSendMessage() {
        val messageText = uiState.value.currentMessage
        if (messageText.isBlank()) return

        viewModelScope.launch {
            try {
                if (uiState.value.isEditing && uiState.value.selectedMessage != null) {
                    editMessage(uiState.value.selectedMessage!!.messageId, messageText)
                } else {
                    val message = createMessage(messageText)
                    chatRepository.sendMessage(message)
                    _uiState.update { it.copy(currentMessage = "") }
                }
            } catch (e: Exception) {
                handleError("Error al procesar mensaje", e)
            }
        }
    }

    override fun onAttachmentClick() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(showAttachmentOptions = true) }
            } catch (e: Exception) {
                handleError("Error al abrir selector de archivos", e)
            }
        }
    }

    override fun onMessageClick(message: ChatMessage) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(selectedMessage = message) }
            } catch (e: Exception) {
                handleError("Error al procesar mensaje", e)
            }
        }
    }

    override fun onMessageLongPress(message: ChatMessage) {
        viewModelScope.launch {
            try {
                val currentUserId = chatRepository.getCurrentUserId()
                _uiState.update {
                    it.copy(
                        selectedMessage = message,
                        showMessageOptions = true,
                        canEditMessage = message.senderId == currentUserId && !message.isDeleted
                    )
                }
            } catch (e: Exception) {
                handleError("Error al mostrar opciones", e)
            }
        }
    }

    override fun onMenuClick() {
        _uiState.update { it.copy(showMenu = !it.showMenu) }
    }

    override fun onCallClick() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                try {
                    chatRepository.initiateCall(chatId, isVideo = false)
                    _uiState.update { it.copy(isInCall = true) }
                } catch (e: Exception) {
                    handleError("Error al iniciar llamada", e)
                }
            }
        }
    }

    override fun onVideoCallClick() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                try {
                    chatRepository.initiateCall(chatId, isVideo = true)
                    _uiState.update { it.copy(isInVideoCall = true) }
                } catch (e: Exception) {
                    handleError("Error al iniciar videollamada", e)
                }
            }
        }
    }

    override fun onWalkieTalkiePressed() {
        currentChatId?.let { chatId ->
            try {
                walkieTalkieService.startWalkieTalkie(chatId)
                _uiState.update { it.copy(isWalkieTalkieActive = true) }
            } catch (e: Exception) {
                handleError("Error al iniciar walkie-talkie", e)
            }
        }
    }

    override fun onWalkieTalkieReleased() {
        try {
            walkieTalkieService.stopWalkieTalkie()
            _uiState.update { it.copy(isWalkieTalkieActive = false) }
        } catch (e: Exception) {
            handleError("Error al detener walkie-talkie", e)
        }
    }

    override fun onMenuOptionSelected(option: ChatMenuOption) {
        viewModelScope.launch {
            try {
                handleMenuOption(option)
                _uiState.update { it.copy(showMenu = false) }
            } catch (e: Exception) {
                handleError("Error al procesar opción", e)
            }
        }
    }

    private fun handleMenuOption(option: ChatMenuOption) {
        when (option) {
            is ChatMenuOption.ViewContact -> handleViewContact()
            is ChatMenuOption.Search -> handleSearch()
            is ChatMenuOption.AddToList -> handleAddToList()
            is ChatMenuOption.Files -> handleFiles()
            is ChatMenuOption.Links -> handleLinks()
            is ChatMenuOption.MuteNotifications -> handleMute()
            is ChatMenuOption.MuteFor -> handleMuteFor(option.duration)
            is ChatMenuOption.Call -> handleCall()
            is ChatMenuOption.VideoCall -> handleVideoCall()
            is ChatMenuOption.ChangeWallpaper -> handleChangeWallpaper()
            is ChatMenuOption.ClearChat -> handleClearChat()
            is ChatMenuOption.DeleteChat -> handleDeleteChat()
            is ChatMenuOption.TemporaryMessages -> handleTemporaryMessages()
            is ChatMenuOption.Report -> handleReport()
            is ChatMenuOption.Block -> handleBlock()
            is ChatMenuOption.ExportChat -> handleExportChat()
            is ChatMenuOption.CreateShortcut -> handleCreateShortcut()
            is ChatMenuOption.ToggleWalkieTalkie -> handleToggleWalkieTalkie()
            is ChatMenuOption.ToggleChatGPT -> handleToggleChatGPT()
            is ChatMenuOption.ToggleVoiceRecorder -> handleToggleVoiceRecorder()
            is ChatMenuOption.ToggleCamera -> handleToggleCamera()
            is ChatMenuOption.ToggleMicrophone -> handleToggleMicrophone()
            is ChatMenuOption.ToggleSpeaker -> handleToggleSpeaker()
            is ChatMenuOption.ToggleBluetooth -> handleToggleBluetooth()
        }
    }

    private fun handleViewContact() {
        _uiState.update { it.copy(showContactInfo = true) }
    }

    private fun handleSearch() {
        _uiState.update {
            it.copy(
                isSearchActive = true,
                searchResults = emptyList()
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchMessages()
    }

    private fun searchMessages() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val query = _searchQuery.value
                if (query.length >= 3 && currentChatId != null) {
                    _uiState.update { it.copy(isLoading = true) }
                    val results = chatRepository.searchMessages(currentChatId!!, query)
                    _uiState.update {
                        it.copy(
                            searchResults = results,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            searchResults = emptyList(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                handleError("Error al buscar mensajes", e)
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _uiState.update {
            it.copy(
                isSearchActive = false,
                searchResults = emptyList()
            )
        }
    }

    private fun handleAddToList() {
        _uiState.update { it.copy(showAddToListDialog = true) }
    }

    private fun handleFiles() {
        _uiState.update { it.copy(showFilesScreen = true) }
    }

    private fun handleLinks() {
        _uiState.update { it.copy(showLinksScreen = true) }
    }

    private fun handleMute() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                chatRepository.muteChat(chatId)
                _uiState.update { it.copy(isMuted = true) }
            }
        }
    }

    private fun handleMuteFor(duration: String) {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                chatRepository.muteChatFor(chatId, duration)
                _uiState.update {
                    it.copy(
                        isMuted = true,
                        muteDuration = duration
                    )
                }
            }
        }
    }

    private fun handleCall() {
        onCallClick()
    }

    private fun handleVideoCall() {
        onVideoCallClick()
    }

    private fun handleChangeWallpaper() {
        _uiState.update { it.copy(showWallpaperPicker = true) }
    }

    private fun handleClearChat() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                chatRepository.clearChat(chatId)
                _uiState.update { it.copy(messages = emptyList()) }
            }
        }
    }

    private fun handleDeleteChat() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                chatRepository.deleteChat(chatId)
                _uiState.update { it.copy(shouldNavigateBack = true) }
            }
        }
    }

    private fun handleTemporaryMessages() {
        _uiState.update { it.copy(showTemporaryMessagesSettings = true) }
    }

    private fun handleReport() {
        _uiState.update { it.copy(showReportDialog = true) }
    }

    private fun handleBlock() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                chatRepository.blockChat(chatId)
                _uiState.update { it.copy(isBlocked = true) }
            }
        }
    }

    private fun handleExportChat() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                try {
                    val exportUrl = chatRepository.exportChat(chatId)
                    _uiState.update { it.copy(exportUrl = exportUrl) }
                } catch (e: Exception) {
                    handleError("Error al exportar chat", e)
                }
            }
        }
    }

    private fun handleCreateShortcut() {
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                try {
                    chatRepository.createShortcut(chatId)
                    _uiState.update { it.copy(shortcutCreated = true) }
                } catch (e: Exception) {
                    handleError("Error al crear acceso directo", e)
                }
            }
        }
    }

    private fun handleToggleWalkieTalkie() {
        if (uiState.value.isWalkieTalkieActive) {
            onWalkieTalkieReleased()
        } else {
            onWalkieTalkiePressed()
        }
    }

    private fun handleToggleChatGPT() {
        _uiState.update { it.copy(isChatGPTActive = !it.isChatGPTActive) }
    }

    private fun handleToggleVoiceRecorder() {
        _uiState.update { it.copy(isRecording = !it.isRecording) }
    }

    private fun handleToggleCamera() {
        _uiState.update { it.copy(isCameraEnabled = !it.isCameraEnabled) }
    }

    private fun handleToggleMicrophone() {
        _uiState.update { it.copy(isMicrophoneEnabled = !it.isMicrophoneEnabled) }
    }

    private fun handleToggleSpeaker() {
        _uiState.update { it.copy(isSpeakerEnabled = !it.isSpeakerEnabled) }
    }

    private fun handleToggleBluetooth() {
        _uiState.update { it.copy(isBluetoothEnabled = !it.isBluetoothEnabled) }
    }

    // Se crea el mensaje usando el constructor correcto; se asigna "Tú" como username.
    private suspend fun createMessage(content: String): ChatMessage = ChatMessage(
        id = 0,
        messageId = UUID.randomUUID().toString(),
        chatId = currentChatId!!,
        senderId = chatRepository.getCurrentUserId(),
        recipientId = uiState.value.chatInfo?.otherUserId ?: "",
        content = content,
        username = "Tú",
        timestamp = System.currentTimeMillis(),
        type = MessageType.TEXT
    )

    private fun handleError(message: String, error: Exception) {
        _uiState.update {
            it.copy(
                error = "$message: ${error.message}",
                isLoading = false,
                showMenu = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun handlePermissionResult(permission: String, isGranted: Boolean) {
        when (permission) {
            Manifest.permission.RECORD_AUDIO -> {
                _uiState.update { it.copy(hasAudioPermission = isGranted) }
            }
            Manifest.permission.CAMERA -> {
                _uiState.update { it.copy(hasCameraPermission = isGranted) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        recordingJob?.cancel()
        searchJob?.cancel()
        voiceRecorder.pause()
        voiceRecorder.stop()
        voiceRecorder.resume()
        voiceRecorder.start()
    }
}
