package com.williamfq.xhat.di

import android.content.Context
import android.media.AudioManager
import com.williamfq.xhat.call.audio.AudioProcessor
import com.williamfq.xhat.call.audio.AudioProcessorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindAudioProcessor(
        audioProcessorImpl: AudioProcessorImpl
    ): AudioProcessor

    companion object {
        @Provides
        @Singleton
        fun provideAudioManager(@ApplicationContext context: Context): AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}
