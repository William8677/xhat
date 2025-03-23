package com.williamfq.xhat.utils.image

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessor @Inject constructor(
    private val context: Context
) {
    suspend fun processImage(
        sourceUri: Uri,
        filters: List<ImageFilter>,
        targetFile: File
    ): ProcessingResult = withContext(Dispatchers.IO) {
        try {
            var bitmap = loadImage(sourceUri)

            filters.forEach { filter ->
                bitmap = when (filter) {
                    is ImageFilter.Resize -> resizeImage(bitmap, filter.maxWidth, filter.maxHeight)
                    is ImageFilter.Rotate -> rotateImage(bitmap, filter.degrees)
                    is ImageFilter.Crop -> cropImage(bitmap, filter.rect)
                    is ImageFilter.ColorAdjust -> adjustColors(
                        bitmap,
                        filter.brightness,
                        filter.contrast,
                        filter.saturation
                    )
                    is ImageFilter.Effect -> applyEffect(bitmap, filter.effect)
                }
            }

            saveImage(bitmap, targetFile)
            ProcessingResult.Success(targetFile)
        } catch (e: Exception) {
            ProcessingResult.Error(e.message ?: "Error desconocido al procesar imagen")
        }
    }

    private suspend fun loadImage(
        uri: Uri,
        reqWidth: Int = MAX_IMAGE_WIDTH,
        reqHeight: Int = MAX_IMAGE_HEIGHT
    ): Bitmap = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, options)

            val scale = calculateInSampleSize(options, reqWidth, reqHeight)

            val input2 = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("No se pudo reabrir el stream")

            val decodeOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = scale
            }

            val bitmap = BitmapFactory.decodeStream(input2, null, decodeOptions)
                ?: throw IllegalStateException("No se pudo decodificar la imagen")

            correctOrientation(bitmap, uri)
        } ?: throw IllegalStateException("No se pudo abrir la imagen")
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private suspend fun correctOrientation(bitmap: Bitmap, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                }
                if (!matrix.isIdentity) {
                    return@withContext Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
            }
            bitmap
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun resizeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) return bitmap
        val ratio = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
        return bitmap.scale((bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt())
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun cropImage(bitmap: Bitmap, rect: RectF): Bitmap {
        val left = (rect.left * bitmap.width).toInt()
        val top = (rect.top * bitmap.height).toInt()
        val width = (rect.width() * bitmap.width).toInt()
        val height = (rect.height() * bitmap.height).toInt()
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    private fun adjustColors(bitmap: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap {
        val cm = ColorMatrix().apply { setSaturation(saturation) }
        val cmContrast = ColorMatrix().apply { setScale(contrast, contrast, contrast, 1f) }
        cm.postConcat(cmContrast)
        val cmBrightness = ColorMatrix().apply {
            set(floatArrayOf(1f, 0f, 0f, 0f, brightness, 0f, 1f, 0f, 0f, brightness, 0f, 0f, 1f, 0f, brightness, 0f, 0f, 0f, 1f, 0f))
        }
        cm.postConcat(cmBrightness)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        val newBitmap = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return newBitmap
    }

    private fun applyEffect(bitmap: Bitmap, effect: ImageEffect): Bitmap {
        return when (effect) {
            ImageEffect.BLUR -> bitmap // Implementar efecto real si es necesario
            ImageEffect.VIGNETTE -> bitmap
            ImageEffect.GRAYSCALE -> bitmap
            ImageEffect.SEPIA -> bitmap
        }
    }

    private fun saveImage(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    }

    companion object {
        private const val MAX_IMAGE_WIDTH = 4096
        private const val MAX_IMAGE_HEIGHT = 4096
    }
}

sealed class ImageFilter {
    data class Resize(val maxWidth: Int, val maxHeight: Int) : ImageFilter()
    data class Rotate(val degrees: Float) : ImageFilter()
    data class Crop(val rect: RectF) : ImageFilter()
    data class ColorAdjust(val brightness: Float, val contrast: Float, val saturation: Float) : ImageFilter()
    data class Effect(val effect: ImageEffect) : ImageFilter()
}

enum class ImageEffect {
    BLUR, VIGNETTE, GRAYSCALE, SEPIA
}

sealed class ProcessingResult {
    data class Success(val file: File) : ProcessingResult()
    data class Error(val message: String) : ProcessingResult()
}