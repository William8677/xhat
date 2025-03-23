package com.williamfq.xhat.core.compression

import android.content.Context
import android.graphics.*
import android.media.*
import android.net.Uri
import android.provider.MediaStore
import com.williamfq.xhat.ui.stories.MediaCompressorInterface
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.*
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: LoggerInterface
) : MediaCompressorInterface {
    private val _compressionProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val compressionProgress: StateFlow<Map<String, Int>> = _compressionProgress

    companion object {
        private const val OUTPUT_DIR = "compressed_media"
        private const val MAX_IMAGE_DIMENSION = 1920
        private const val JPEG_QUALITY = 85
        private const val VIDEO_BITRATE = 2_000_000 // 2Mbps
        private const val VIDEO_FRAMERATE = 30
        private const val VIDEO_I_FRAME_INTERVAL = 5
        private const val AUDIO_BITRATE = 128_000 // 128kbps
        private const val AUDIO_SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 1024 * 1024
        private const val MAX_BLUR_RADIUS = 25f
        private const val STICKER_MAX_SIZE = 512
        private const val AVATAR_SIZE = 400
        private const val THUMBNAIL_SIZE = 200
    }

    override suspend fun compressImage(
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int,
        format: Bitmap.CompressFormat,
        applyBlur: Boolean,
        blurRadius: Float,
        addWatermark: Boolean,
        watermarkText: String?
    ): Uri = withContext(Dispatchers.IO) {
        try {
            updateProgress(uri.toString(), 0)

            val inputStream = context.contentResolver.openInputStream(uri)
            val outputFile = createOutputFile("img", getExtensionForFormat(format))

            inputStream?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                var processedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

                if (applyBlur && blurRadius > 0) {
                    processedBitmap = applyBlurEffect(processedBitmap, blurRadius.coerceAtMost(MAX_BLUR_RADIUS))
                }

                if (addWatermark && !watermarkText.isNullOrBlank()) {
                    processedBitmap = addWatermarkToBitmap(processedBitmap, watermarkText)
                }

                outputFile.outputStream().use { output ->
                    processedBitmap.compress(format, quality, output)
                }

                bitmap.recycle()
                processedBitmap.recycle()
            }

            updateProgress(uri.toString(), 100)
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            logger.logEvent("MediaCompressor", "Image compression failed", LogLevel.ERROR, e)
            throw e
        }
    }

    private fun applyBlurEffect(bitmap: Bitmap, radius: Float): Bitmap {
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        val paint = Paint().apply {
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return outputBitmap
    }

    private fun addWatermarkToBitmap(bitmap: Bitmap, watermarkText: String): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            textSize = result.width * 0.05f
            isAntiAlias = true
            setShadowLayer(1f, 0f, 1f, Color.BLACK)
        }

        val x = result.width * 0.05f
        val y = result.height * 0.95f
        canvas.drawText(watermarkText, x, y, paint)
        return result
    }

    override suspend fun compressVideo(
        uri: Uri,
        targetBitrate: Int,
        targetFrameRate: Int,
        targetResolution: Pair<Int, Int>?,
        includeAudio: Boolean,
        enableHEVC: Boolean,
        addWatermark: Boolean
    ): Uri = withContext(Dispatchers.IO) {
        try {
            updateProgress(uri.toString(), 0)
            val extractor = MediaExtractor()
            val outputFile = createOutputFile("vid", "mp4")

            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                extractor.setDataSource(descriptor.fileDescriptor)
            }

            val muxer = MediaMuxer(outputFile.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            try {
                val videoCodec = if (enableHEVC) MediaFormat.MIMETYPE_VIDEO_HEVC else MediaFormat.MIMETYPE_VIDEO_AVC
                val videoTrackIndex = compressVideoTrack(
                    extractor,
                    muxer,
                    targetBitrate,
                    targetFrameRate,
                    targetResolution,
                    videoCodec,
                    addWatermark
                )

                val audioTrackIndex = if (includeAudio) compressAudioTrack(extractor, muxer) else -1

                muxer.start()
                processFrames(extractor, muxer, videoTrackIndex, audioTrackIndex, uri.toString())
            } finally {
                try {
                    muxer.stop()
                    muxer.release()
                } catch (e: Exception) {
                    logger.logEvent("MediaCompressor", "Error stopping muxer", LogLevel.ERROR, e)
                }
                extractor.release()
            }

            updateProgress(uri.toString(), 100)
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            logger.logEvent("MediaCompressor", "Video compression failed", LogLevel.ERROR, e)
            throw e
        }
    }

    override suspend fun compressAudio(uri: Uri): Uri = withContext(Dispatchers.IO) {
        val outputFile = createOutputFile("audio", "mp3")
        // Implementación básica para audio
        Uri.fromFile(outputFile)
    }

    override suspend fun compressDocument(uri: Uri): Uri = withContext(Dispatchers.IO) {
        val outputFile = createOutputFile("doc", "pdf")
        // Implementación básica para documentos
        Uri.fromFile(outputFile)
    }

    private fun compressVideoTrack(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        targetBitrate: Int,
        targetFrameRate: Int,
        targetResolution: Pair<Int, Int>?,
        videoCodec: String,
        addWatermark: Boolean
    ): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                extractor.selectTrack(i)

                val width = targetResolution?.first ?: format.getInteger(MediaFormat.KEY_WIDTH)
                val height = targetResolution?.second ?: format.getInteger(MediaFormat.KEY_HEIGHT)

                val outputFormat = MediaFormat.createVideoFormat(videoCodec, width, height).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
                    setInteger(MediaFormat.KEY_FRAME_RATE, targetFrameRate)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_I_FRAME_INTERVAL)
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                    format.getByteBuffer("csd-0")?.let { setByteBuffer("csd-0", it) }
                    format.getByteBuffer("csd-1")?.let { setByteBuffer("csd-1", it) }
                }

                return muxer.addTrack(outputFormat)
            }
        }
        throw IllegalStateException("No video track found")
    }

    private fun compressAudioTrack(extractor: MediaExtractor, muxer: MediaMuxer): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                extractor.selectTrack(i)

                val outputFormat = MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC,
                    AUDIO_SAMPLE_RATE,
                    format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                ).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BITRATE)
                    setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                    setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE)
                    format.getByteBuffer("csd-0")?.let { setByteBuffer("csd-0", it) }
                }

                return muxer.addTrack(outputFormat)
            }
        }
        return -1
    }

    private suspend fun processFrames(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        videoTrackIndex: Int,
        audioTrackIndex: Int,
        uriString: String
    ) = withContext(Dispatchers.Default) {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE)
        val bufferInfo = MediaCodec.BufferInfo()

        var totalBytesRead = 0L
        val fileSize = context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")?.use { it.statSize } ?: 0L

        while (true) {
            val trackIndex = extractor.sampleTrackIndex
            if (trackIndex == -1) break

            bufferInfo.offset = 0
            bufferInfo.size = extractor.readSampleData(buffer, 0)
            if (bufferInfo.size < 0) break

            bufferInfo.presentationTimeUs = extractor.sampleTime
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME

            val currentTrackIndex = when (trackIndex) {
                videoTrackIndex -> videoTrackIndex
                audioTrackIndex -> audioTrackIndex
                else -> continue
            }

            muxer.writeSampleData(currentTrackIndex, buffer, bufferInfo)

            totalBytesRead += bufferInfo.size
            val progress = if (fileSize > 0) ((totalBytesRead.toDouble() / fileSize) * 100).toInt() else 0
            updateProgress(uriString, progress.coerceIn(0, 100))

            extractor.advance()
        }
    }

    suspend fun createVideoThumbnail(
        uri: Uri,
        width: Int = THUMBNAIL_SIZE,
        height: Int = THUMBNAIL_SIZE,
        timeMs: Long = 0
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                retriever.setDataSource(descriptor.fileDescriptor)
            }

            val frame = retriever.getFrameAtTime(timeMs * 1000)
            frame?.let { resizeBitmap(it, width, height) }
        } catch (e: Exception) {
            logger.logEvent("MediaCompressor", "Thumbnail creation failed", LogLevel.ERROR, e)
            null
        }
    }

    fun createSticker(bitmap: Bitmap, maxSize: Int = STICKER_MAX_SIZE): Bitmap {
        return resizeBitmap(bitmap, maxSize, maxSize)
    }

    suspend fun createAvatarThumbnail(
        uri: Uri,
        size: Int = AVATAR_SIZE,
        circular: Boolean = true
    ): Bitmap = withContext(Dispatchers.IO) {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        var resized = resizeBitmap(bitmap, size, size)

        if (circular) {
            resized = createCircularBitmap(resized)
        }

        resized
    }

    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, bitmap.width / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun createOutputFile(prefix: String, extension: String): File {
        val dir = File(context.cacheDir, OUTPUT_DIR).apply { mkdirs() }
        return File(dir, "${prefix}_${System.currentTimeMillis()}.$extension")
    }

    private fun getExtensionForFormat(format: Bitmap.CompressFormat): String {
        return when (format) {
            Bitmap.CompressFormat.JPEG -> "jpg"
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP -> "webp"
            else -> "jpg"
        }
    }

    private fun updateProgress(uri: String, progress: Int) {
        _compressionProgress.value = _compressionProgress.value + (uri to progress)
    }

    override fun clearCache() {
        File(context.cacheDir, OUTPUT_DIR).deleteRecursively()
        _compressionProgress.value = emptyMap()
    }
}