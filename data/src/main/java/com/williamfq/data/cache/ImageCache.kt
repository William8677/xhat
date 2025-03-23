/*
 * Updated: 2025-01-21 20:42:23
 * Author: William8677
 */

package com.williamfq.xhat.data.cache

import android.graphics.Bitmap
import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCache @Inject constructor() {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun addBitmapToCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    fun getBitmapFromCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }

    fun clearCache() {
        memoryCache.evictAll()
    }

    fun getCacheSize(): Int {
        return memoryCache.size()
    }

    fun getCacheMaxSize(): Int {
        return memoryCache.maxSize()
    }
}