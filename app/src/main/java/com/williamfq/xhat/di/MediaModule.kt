package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.utils.VoiceRecorder
import com.williamfq.xhat.utils.VoiceRecorderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideVoiceRecorder(@ApplicationContext context: Context): VoiceRecorder {
        return VoiceRecorderImpl(context)
    }
}
