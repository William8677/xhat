package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.domain.repository.ChatRepository
import com.williamfq.xhat.service.WalkieTalkieService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideWalkieTalkieService(
        @ApplicationContext context: Context,
        chatRepository: ChatRepository
    ): WalkieTalkieService {
        // Nota: La creación manual de un Service es atípica; asegúrate
        // de que este provider se ajuste a tus necesidades.
        return WalkieTalkieService()
    }
}
