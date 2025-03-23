package com.williamfq.xhat.service.signaling

import android.content.Context
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.webrtc.SessionDescription
import org.webrtc.IceCandidate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [SignalingInterface] que inyecta:
 * - [LoggerInterface] para logs
 * - [WebRTCSignalingClient] como el realClient de WebRTC
 */
@Singleton
class SignalingClientImpl @Inject constructor(
    private val context: Context,
    private val logger: LoggerInterface,
    private val realClient: WebRTCSignalingClient
) : SignalingInterface {

    private val signalingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "SignalingClientImpl"
    }

    override fun connect() {
        signalingScope.launch {
            try {
                logger.logEvent(TAG, "Connecting to signaling server", LogLevel.INFO)
                realClient.connect()
            } catch (e: Exception) {
                logger.logEvent(TAG, "Error connecting to signaling server", LogLevel.ERROR, e)
            }
        }
    }

    override fun disconnect() {
        signalingScope.launch {
            try {
                realClient.disconnect()
                logger.logEvent(TAG, "Disconnected from signaling server", LogLevel.INFO)
            } catch (e: Exception) {
                logger.logEvent(TAG, "Error disconnecting from signaling server", LogLevel.ERROR, e)
            }
        }
    }

    override fun sendOffer(offer: SessionDescription, toUserId: String) {
        signalingScope.launch {
            try {
                realClient.sendOffer(offer, toUserId)
                logger.logEvent(TAG, "Sent offer to user: $toUserId", LogLevel.DEBUG)
            } catch (e: Exception) {
                logger.logEvent(TAG, "Error sending offer to user: $toUserId", LogLevel.ERROR, e)
            }
        }
    }

    override fun sendAnswer(answer: SessionDescription, toUserId: String) {
        signalingScope.launch {
            try {
                realClient.sendAnswer(answer, toUserId)
                logger.logEvent(TAG, "Sent answer to user: $toUserId", LogLevel.DEBUG)
            } catch (e: Exception) {
                logger.logEvent(TAG, "Error sending answer to user: $toUserId", LogLevel.ERROR, e)
            }
        }
    }

    override fun sendIceCandidate(iceCandidate: IceCandidate, toUserId: String) {
        signalingScope.launch {
            try {
                realClient.sendIceCandidate(iceCandidate, toUserId)
                logger.logEvent(TAG, "Sent ICE candidate to user: $toUserId", LogLevel.DEBUG)
            } catch (e: Exception) {
                logger.logEvent(TAG, "Error sending ICE candidate to user: $toUserId", LogLevel.ERROR, e)
            }
        }
    }
}