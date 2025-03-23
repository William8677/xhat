package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.domain.repository.GalleryRepository
import com.williamfq.xhat.utils.image.ImageProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GalleryModule {

    @Provides
    @Singleton
    fun provideGalleryRepository(
        @ApplicationContext context: Context
    ): GalleryRepository = GalleryRepository(context)

    @Provides
    @Singleton
    fun provideImageProcessor(
        @ApplicationContext context: Context
    ): ImageProcessor = ImageProcessor(context)
}