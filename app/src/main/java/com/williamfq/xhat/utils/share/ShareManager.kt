package com.williamfq.xhat.utils.share

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.williamfq.xhat.ui.stories.ShareManagerInterface // Importar la interfaz
import com.williamfq.xhat.utils.analytics.AnalyticsManager
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analytics: AnalyticsManager,
    private val logger: LoggerInterface,
    private val gson: Gson,
    private val okHttpClient: OkHttpClient
) : ShareManagerInterface { // Implementar la interfaz
    private val _currentShare = MutableStateFlow<SharedContent?>(null)
    val currentShare: StateFlow<SharedContent?> = _currentShare

    companion object {
        private const val FACEBOOK_API = "https://graph.facebook.com/v13.0/"
        private const val INSTAGRAM_API = "https://graph.instagram.com/v12.0/"
        private const val TWITTER_API = "https://api.twitter.com/2/"
        private const val TIKTOK_API = "https://open.tiktokapis.com/v2/"
    }

    override suspend fun importContent(url: String): SharedContent = withContext(Dispatchers.IO) {
        try {
            when {
                isFacebookUrl(url) -> importFacebookContent(url)
                isInstagramUrl(url) -> importInstagramContent(url)
                isTwitterUrl(url) -> importTwitterContent(url)
                isTikTokUrl(url) -> importTikTokContent(url)
                else -> throw UnsupportedPlatformException("URL no soportada")
            }.also {
                _currentShare.value = it
                analytics.logEvent("content_imported", Bundle().apply {
                    putString("platform", it.platform.name)
                    putString("type", it.type.name)
                })
            }
        } catch (e: Exception) {
            logger.logEvent("ShareManager", "Error importando contenido", LogLevel.ERROR, e)
            throw e
        }
    }

    private fun importFacebookContent(url: String): SharedContent {
        val postId = extractFacebookPostId(url)
        val request = Request.Builder()
            .url("$FACEBOOK_API$postId?fields=id,message,full_picture,source")
            .build()

        return parsePublicContent(okHttpClient.newCall(request).execute(), SocialPlatform.FACEBOOK)
    }

    private fun importInstagramContent(url: String): SharedContent {
        val mediaId = extractInstagramMediaId(url)
        val request = Request.Builder()
            .url("$INSTAGRAM_API$mediaId")
            .build()

        return parsePublicContent(okHttpClient.newCall(request).execute(), SocialPlatform.INSTAGRAM)
    }

    private fun importTwitterContent(url: String): SharedContent {
        val tweetId = extractTwitterTweetId(url)
        val request = Request.Builder()
            .url("$TWITTER_API/tweets/$tweetId")
            .build()

        return parsePublicContent(okHttpClient.newCall(request).execute(), SocialPlatform.TWITTER)
    }

    private fun importTikTokContent(url: String): SharedContent {
        val videoId = extractTikTokVideoId(url)
        val request = Request.Builder()
            .url("$TIKTOK_API/video/info/?video_id=$videoId")
            .build()

        return parsePublicContent(okHttpClient.newCall(request).execute(), SocialPlatform.TIKTOK)
    }

    private fun parsePublicContent(response: Response, platform: SocialPlatform): SharedContent {
        val json = response.body?.string() ?: throw Exception("Respuesta vacía")
        val jsonObject = gson.fromJson(json, Map::class.java)

        return SharedContent(
            platform = platform,
            type = determineContentType(jsonObject),
            url = jsonObject["url"] as? String ?: "",
            text = jsonObject["text"] as? String,
            mediaUrls = (jsonObject["media"] as? List<*>)?.mapNotNull { it as? String } ?: listOf(),
            author = parseAuthor(jsonObject),
            metadata = parseMetadata(jsonObject),
            importedAt = LocalDateTime.now()
        )
    }

    private fun determineContentType(data: Map<*, *>): ContentType {
        return when {
            data["type"] == "photo" || data["full_picture"] != null -> ContentType.IMAGE
            data["type"] == "video" || data["source"] != null -> ContentType.VIDEO
            data["type"] == "story" -> ContentType.STORY
            else -> ContentType.TEXT
        }
    }

    private fun parseAuthor(data: Map<*, *>): ContentAuthor {
        val authorData = data["author"] as? Map<*, *> ?: mapOf<String, Any>()
        return ContentAuthor(
            id = authorData["id"] as? String ?: "",
            username = authorData["username"] as? String ?: "",
            displayName = authorData["name"] as? String,
            profileUrl = authorData["profile_url"] as? String,
            avatarUrl = authorData["avatar_url"] as? String
        )
    }

    private fun parseMetadata(data: Map<*, *>): ContentMetadata {
        val stats = data["statistics"] as? Map<*, *> ?: mapOf<String, Any>()
        return ContentMetadata(
            likes = (stats["likes"] as? Number)?.toInt() ?: 0,
            shares = (stats["shares"] as? Number)?.toInt() ?: 0,
            comments = (stats["comments"] as? Number)?.toInt() ?: 0,
            views = (stats["views"] as? Number)?.toInt() ?: 0,
            createdAt = try {
                LocalDateTime.parse(data["created_at"] as? String)
            } catch (_: Exception) {
                null
            },
            originalUrl = data["url"] as? String ?: "",
            tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: listOf()
        )
    }

    private fun extractFacebookPostId(url: String): String {
        val regex = Regex("""(?:facebook\.com|fb\.com)/.*?/(\d+)""")
        return regex.find(url)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("URL de Facebook inválida")
    }

    private fun extractInstagramMediaId(url: String): String {
        val regex = Regex("""instagram\.com/p/([^/]+)""")
        return regex.find(url)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("URL de Instagram inválida")
    }

    private fun extractTwitterTweetId(url: String): String {
        val regex = Regex("""(?:twitter\.com|x\.com)/\w+/status/(\d+)""")
        return regex.find(url)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("URL de Twitter inválida")
    }

    private fun extractTikTokVideoId(url: String): String {
        val regex = Regex("""tiktok\.com/@[^/]+/video/(\d+)""")
        return regex.find(url)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("URL de TikTok inválida")
    }

    private fun isFacebookUrl(url: String): Boolean =
        url.contains("facebook.com") || url.contains("fb.com")

    private fun isInstagramUrl(url: String): Boolean =
        url.contains("instagram.com")

    private fun isTwitterUrl(url: String): Boolean =
        url.contains("twitter.com") || url.contains("x.com")

    private fun isTikTokUrl(url: String): Boolean =
        url.contains("tiktok.com")
}

data class SharedContent(
    val id: String = System.currentTimeMillis().toString(),
    val platform: SocialPlatform,
    val type: ContentType,
    val url: String,
    val text: String? = null,
    val mediaUrls: List<String> = listOf(),
    val author: ContentAuthor,
    val metadata: ContentMetadata,
    val importedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false,
    val isImported: Boolean = true,
    val isShared: Boolean = false,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isSharedToFacebook: Boolean = false,
    val isSharedToInstagram: Boolean = false,
    val isSharedToTwitter: Boolean = false,
    val isSharedToTikTok: Boolean = false,
    val isSharedToWhatsApp: Boolean = false,
    val isSharedToTelegram: Boolean = false,
    val isSharedToLinkedIn: Boolean = false,
    val isSharedToSnapchat: Boolean = false,
    val isSharedToPinterest: Boolean = false,
    val isSharedToReddit: Boolean = false,
    val isSharedToTumblr: Boolean = false,
)

data class ContentAuthor(
    val id: String,
    val username: String,
    val displayName: String?,
    val profileUrl: String?,
    val avatarUrl: String?
)

data class ContentMetadata(
    val likes: Int = 0,
    val shares: Int = 0,
    val comments: Int = 0,
    val views: Int = 0,
    val createdAt: LocalDateTime?,
    val originalUrl: String,
    val tags: List<String> = listOf()
)

enum class SocialPlatform {
    FACEBOOK,
    INSTAGRAM,
    TWITTER,
    TIKTOK
}

enum class ContentType {
    TEXT,
    IMAGE,
    VIDEO,
    STORY,
    AUDIO,
    MIXED
}

class UnsupportedPlatformException(message: String) : Exception(message)