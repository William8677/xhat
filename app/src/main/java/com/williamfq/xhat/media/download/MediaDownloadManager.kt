package com.williamfq.xhat.media.download

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.williamfq.xhat.ui.stories.MediaDownloadManagerInterface
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val logger: LoggerInterface
) : MediaDownloadManagerInterface {
    private val _downloads = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadStatus>> = _downloads

    private var cacheSize: Long = DEFAULT_CACHE_SIZE
    private var compressionQuality: Int = DEFAULT_COMPRESSION_QUALITY

    companion object {
        private const val TAG = "MediaDownloadManager"
        private const val BUFFER_SIZE = 8192
        private const val CACHE_DIR = "media_cache"
        private const val DEFAULT_CACHE_SIZE = 100 * 1024 * 1024L // 100MB
        private const val DEFAULT_COMPRESSION_QUALITY = 85
    }

    override suspend fun setCacheSize(size: Long) {
        cacheSize = size
        maintainCacheSize()
    }

    override suspend fun setCompressionQuality(quality: Int) {
        compressionQuality = quality.coerceIn(0, 100)
    }

    override suspend fun preloadImage(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "image")
    }

    override suspend fun preloadVideo(url: String, maxSizeMB: Int): Uri = withContext(Dispatchers.IO) {
        download(url, "video", maxSizeMB)
    }

    override suspend fun preloadAudio(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "audio")
    }

    override suspend fun preloadDocument(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "document")
    }
    override suspend fun preloadAR(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "ar")
    }
    override suspend fun preloadLocation(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "location")
    }
    override suspend fun preloadContact(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "contact")
    }
    override suspend fun preloadPoll(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "poll")
    }
    override suspend fun preloadQuiz(url: String): Uri = withContext(Dispatchers.IO) {
        download(url, "quiz")
    }




    private suspend fun download(url: String, type: String, maxSizeMB: Int = -1): Uri {
        val cacheFile = getCacheFile(url, type)
        if (cacheFile.exists()) {
            return cacheFile.toUri()
        }

        try {
            updateDownloadStatus(url, DownloadStatus.Downloading(0))

            val request = Request.Builder()
                .url(url)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Download failed: ${response.code}")
                }

                val contentLength = response.body?.contentLength() ?: -1
                if (maxSizeMB > 0 && contentLength > maxSizeMB * 1024 * 1024) {
                    throw FileTooLargeException("File exceeds maximum size of $maxSizeMB MB")
                }

                response.body?.byteStream()?.use { input ->
                    cacheFile.outputStream().use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead = 0L
                        var read: Int

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            val progress = if (contentLength > 0) {
                                (bytesRead * 100 / contentLength).toInt()
                            } else 0
                            updateDownloadStatus(url, DownloadStatus.Downloading(progress))
                        }
                    }
                }
            }

            maintainCacheSize()
            updateDownloadStatus(url, DownloadStatus.Completed(cacheFile.toUri()))
            return cacheFile.toUri()

        } catch (e: Exception) {
            updateDownloadStatus(url, DownloadStatus.Failed(e))
            logger.logEvent(TAG, "Download failed: $url", LogLevel.ERROR, e)
            throw e
        }
    }

    private fun getCacheFile(url: String, type: String): File {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        cacheDir.mkdirs()

        val fileName = "${url.hashCode()}_${type}"
        return File(cacheDir, fileName)
    }

    private suspend fun maintainCacheSize() = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        if (!cacheDir.exists()) return@withContext

        val files = cacheDir.listFiles() ?: return@withContext
        var totalSize = files.sumOf { it.length() }

        if (totalSize > cacheSize) {
            files.sortedBy { it.lastModified() }
                .forEach { file ->
                    if (totalSize > cacheSize) {
                        totalSize -= file.length()
                        file.delete()
                    } else {
                        return@withContext
                    }
                }
        }
    }

    private fun updateDownloadStatus(url: String, status: DownloadStatus) {
        _downloads.value = _downloads.value.toMutableMap().apply {
            put(url, status)
        }
    }

    override fun clearCache() {
        File(context.cacheDir, CACHE_DIR).deleteRecursively()
        _downloads.value = emptyMap()
    }
}

sealed class DownloadStatus {
    data class Downloading(val progress: Int) : DownloadStatus()
    data class Completed(val uri: Uri) : DownloadStatus()
    data class Failed(val error: Exception) : DownloadStatus()
}

class FileTooLargeException(message: String) : Exception(message)