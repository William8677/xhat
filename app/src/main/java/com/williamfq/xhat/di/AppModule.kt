package com.williamfq.xhat.di

import android.content.Context
import android.content.SharedPreferences
import com.williamfq.xhat.service.signaling.WebRTCSignalingClient
import com.williamfq.xhat.utils.analytics.AnalyticsManager
import com.williamfq.xhat.ads.config.AdServicesConfigManager
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
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("xhat_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAnalyticsManager(
        @ApplicationContext context: Context
    ): AnalyticsManager = AnalyticsManager(context)

    @Provides
    @Singleton
    fun provideAdServicesConfigManager(
        @ApplicationContext context: Context,
        analytics: AnalyticsManager,
        logger: LoggerInterface
    ): AdServicesConfigManager = AdServicesConfigManager(context, analytics, logger)

    @Provides
    @Singleton
    fun provideWebRTCSignalingClient(
        logger: LoggerInterface
    ): WebRTCSignalingClient {
        val roomId = "defaultRoom"
        val userId = "William8677"
        val signalingServerUrl = "wss://mySignalingServer.com"
        val dummyListener = object : WebRTCSignalingClient.WebRTCListener {
            override fun onOfferReceived(offer: SessionDescription, fromUserId: String) {}
            override fun onAnswerReceived(answer: SessionDescription, fromUserId: String) {}
            override fun onIceCandidateReceived(iceCandidate: IceCandidate, fromUserId: String) {}
            override fun onPeerDisconnected(userId: String) {}
            override fun onError(error: String) {}
        }
        return WebRTCSignalingClient(
            roomId = roomId,
            userId = userId,
            signalingServerUrl = signalingServerUrl,
            webRTCListener = dummyListener,
            logger = logger
        )
    }
}