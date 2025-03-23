package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.service.signaling.SignalingClientImpl
import com.williamfq.xhat.service.signaling.SignalingInterface
import com.williamfq.xhat.service.signaling.WebRTCSignalingClient
import com.williamfq.xhat.utils.logging.LoggerInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.webrtc.SessionDescription
import org.webrtc.IceCandidate

@Module
@InstallIn(SingletonComponent::class)
object CallModule {

    /**
     * Provee la implementación de SignalingInterface usando el WebRTCSignalingClient.
     * Este binding se coloca aquí para centralizar la lógica de llamadas.
     */
    @Provides
    @Singleton
    fun provideSignalingClient(
        @ApplicationContext context: Context,
        loggerInterface: LoggerInterface,
        realClient: WebRTCSignalingClient
    ): SignalingInterface {
        return SignalingClientImpl(
            context = context,
            logger = loggerInterface,
            realClient = realClient
        )
    }
}
