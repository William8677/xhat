/*
 * Updated: 2025-01-21 20:42:23
 * Author: William8677
 */

package com.williamfq.xhat.data.storage

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheDir: File = context.cacheDir
    private val imagesDir: File = File(cacheDir, "images").apply {
        if (!exists()) mkdirs()
    }
    private val filtersDir: File = File(cacheDir, "filters").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Guarda un [Bitmap] como imagen JPEG en [imagesDir].
     *
     * @param bitmap: La imagen a guardar.
     * @param fileName: El nombre del archivo resultante.
     * @param quality: Calidad JPEG (0-100).
     * @return [File] resultante.
     */
    suspend fun saveImage(
        bitmap: Bitmap,
        fileName: String,
        quality: Int = 90
    ): File = withContext(Dispatchers.IO) {
        val file = File(imagesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        file
    }

    /**
     * Guarda una imagen con filtro en [filtersDir].
     *
     * @param bitmap: La imagen filtrada.
     * @param originalFileName: Nombre de la imagen original.
     * @param filterName: Identificador del filtro aplicado.
     * @param quality: Calidad JPEG (0-100).
     * @return [File] resultante.
     */
    suspend fun saveFilteredImage(
        bitmap: Bitmap,
        originalFileName: String,
        filterName: String,
        quality: Int = 90
    ): File = withContext(Dispatchers.IO) {
        val fileName = "${originalFileName}_${filterName}.jpg"
        val file = File(filtersDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        file
    }

    /**
     * Recupera una imagen filtrada si existe.
     *
     * @param originalFileName: Nombre de la imagen original.
     * @param filterName: Identificador del filtro aplicado.
     * @return [File] si existe, o `null` si no existe.
     */
    suspend fun getFilteredImage(
        originalFileName: String,
        filterName: String
    ): File? = withContext(Dispatchers.IO) {
        val fileName = "${originalFileName}_${filterName}.jpg"
        val file = File(filtersDir, fileName)
        if (file.exists()) file else null
    }

    /**
     * Limpia la caché, borrando todos los archivos en [imagesDir] y [filtersDir].
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        imagesDir.listFiles()?.forEach { it.delete() }
        filtersDir.listFiles()?.forEach { it.delete() }
    }

    /**
     * Suma el tamaño total de archivos en [imagesDir] y [filtersDir].
     * @return Tamaño en bytes.
     */
    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        var size = 0L
        imagesDir.listFiles()?.forEach { size += it.length() }
        filtersDir.listFiles()?.forEach { size += it.length() }
        size
    }

    /**
     * Tamaño máximo de caché (public).
     */
    companion object {
        const val MAX_CACHE_SIZE = 100 * 1024 * 1024 // 100 MB
    }
}
