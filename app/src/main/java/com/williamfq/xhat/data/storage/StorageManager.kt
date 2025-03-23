/*
 * Updated: 2025-01-21 20:42:23
 * Author: William8677
 */

package com.williamfq.xhat.data.storage

import android.content.Context
import android.os.StatFs
import com.williamfq.xhat.data.cache.ImageCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageStorage: ImageStorage,
    private val imageCache: ImageCache
) {
    /**
     * Retorna información sobre el almacenamiento (disponible, total, etc.)
     * y el uso de cache (tanto en disco como en memoria).
     */
    fun getStorageStats(): Flow<StorageStats> = flow {
        val stats = StatFs(context.cacheDir.path)
        val availableBytes = stats.availableBytes
        val totalBytes = stats.totalBytes
        val cacheSize = imageStorage.getCacheSize()
        val memoryCacheSize = imageCache.getCacheSize()

        emit(
            StorageStats(
                availableStorage = availableBytes,
                totalStorage = totalBytes,
                cacheSize = cacheSize,
                memoryCacheSize = memoryCacheSize.toLong(),
                memoryCacheMaxSize = imageCache.getCacheMaxSize().toLong()
            )
        )
    }

    /**
     * Limpia la cache en disco y en memoria.
     */
    suspend fun clearCache() {
        imageStorage.clearCache()
        imageCache.clearCache()
    }

    /**
     * Determina si se debe limpiar la cache en disco.
     * Compara el tamaño actual con [ImageStorage.MAX_CACHE_SIZE].
     */
    suspend fun shouldClearCache(): Boolean {
        val cacheSize = imageStorage.getCacheSize()
        return cacheSize > ImageStorage.MAX_CACHE_SIZE
    }
}

/**
 * Estructura con información de almacenamiento y tamaños de caché.
 */
data class StorageStats(
    val availableStorage: Long,
    val totalStorage: Long,
    val cacheSize: Long,
    val memoryCacheSize: Long,
    val memoryCacheMaxSize: Long
) {
    val usedStorage: Long
        get() = totalStorage - availableStorage

    val storageUsagePercent: Float
        get() = (usedStorage.toFloat() / totalStorage) * 100

    val cacheUsagePercent: Float
        get() = if (memoryCacheMaxSize > 0) {
            (memoryCacheSize.toFloat() / memoryCacheMaxSize) * 100
        } else 0f
}
