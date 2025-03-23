/*
 * Updated: 2025-01-21 20:42:23
 * Author: William8677
 */

package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.data.cache.ImageCache
import com.williamfq.xhat.data.storage.ImageStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideImageStorage(
        @ApplicationContext context: Context
    ): ImageStorage = ImageStorage(context)

    @Provides
    @Singleton
    fun provideImageCache(): ImageCache = ImageCache()
}