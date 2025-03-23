package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.data.manager.UserManager
import com.williamfq.data.manager.UserManagerImpl
import com.williamfq.data.service.MessagingServiceImpl
import com.williamfq.domain.repository.ChatRepository
import com.williamfq.domain.service.MessagingService
import com.williamfq.xhat.service.WalkieTalkieService
import com.williamfq.xhat.service.audio.AudioManager
import com.williamfq.xhat.service.audio.AudioManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindMessagingService(
        messagingServiceImpl: MessagingServiceImpl
    ): MessagingService

    companion object {
        @Provides
        @Singleton
        fun provideAudioManager(
            @ApplicationContext context: Context
        ): AudioManager = AudioManagerImpl(context)

        @Provides
        @Singleton
        fun provideUserManager(
            userManagerImpl: UserManagerImpl
        ): UserManager = userManagerImpl

        // No necesitamos proveer WalkieTalkieService aquí porque:
        // 1. Está anotado con @AndroidEntryPoint
        // 2. Sus dependencias (AudioManager y ChatRepository) se inyectan directamente con @Inject
    }
}