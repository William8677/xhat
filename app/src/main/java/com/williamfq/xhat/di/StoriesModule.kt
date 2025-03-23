package com.williamfq.xhat.di

import com.williamfq.xhat.ads.manager.NativeStoryAdManager
import com.williamfq.xhat.core.compression.MediaCompressor
import com.williamfq.xhat.core.encryption.E2EEncryption
import com.williamfq.xhat.domain.repository.MessageRepository
import com.williamfq.xhat.media.download.MediaDownloadManager
import com.williamfq.xhat.ui.stories.*
import com.williamfq.xhat.utils.analytics.AnalyticsManager
import com.williamfq.xhat.utils.share.ShareManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StoriesModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepository: MessageRepository
    ): MessageRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindShareManager(
        shareManager: ShareManager
    ): ShareManagerInterface

    @Binds
    @Singleton
    abstract fun bindMediaDownloadManager(
        mediaDownloadManager: MediaDownloadManager
    ): MediaDownloadManagerInterface

    @Binds
    @Singleton
    abstract fun bindMediaCompressor(
        mediaCompressor: MediaCompressor
    ): MediaCompressorInterface

    @Binds
    @Singleton
    abstract fun bindE2EEncryption(
        e2eEncryption: E2EEncryption
    ): E2EEncryptionInterface

    @Binds
    @Singleton
    abstract fun bindNativeStoryAdManager(
        nativeStoryAdManager: NativeStoryAdManager
    ): NativeStoryAdManagerInterface

    @Binds
    @Singleton
    abstract fun bindAnalyticsManager(
        analyticsManager: AnalyticsManager
    ): AnalyticsManagerInterface
}