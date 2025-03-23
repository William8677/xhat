package com.williamfq.xhat.service.signaling

import com.williamfq.xhat.call.signaling.SignalingMessage
import com.williamfq.xhat.utils.logging.LoggerInterface
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Implementa [SignalingInterface] y maneja la lógica WebRTC pura:
 * WebSocket, offers, answers, ICE, etc.
 */
class WebRTCSignalingClient(
    private val roomId: String,
    private val userId: String,
    private val signalingServerUrl: String,
    private val webRTCListener: WebRTCListener,
    private val logger: LoggerInterface

) : SignalingInterface, CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val connectedPeers = ConcurrentHashMap<String, Boolean>()

    init {
        connect()
    }

    override fun connect() {
        _connectionState.value = ConnectionState.Connecting
        val request = Request.Builder()
            .url("$signalingServerUrl/room/$roomId/user/$userId")
            .build()
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }

    override fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        job.cancel()
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sendOffer(offer: SessionDescription, toUserId: String) {
        val message = JSONObject().apply {
            put("type", "offer")
            put("fromUserId", userId)
            put("toUserId", toUserId)
            put("sdp", offer.description)
        }
        sendMessage(message)
    }

    override fun sendAnswer(answer: SessionDescription, toUserId: String) {
        val message = JSONObject().apply {
            put("type", "answer")
            put("fromUserId", userId)
            put("toUserId", toUserId)
            put("sdp", answer.description)
        }
        sendMessage(message)
    }

    override fun sendIceCandidate(iceCandidate: IceCandidate, toUserId: String) {
        val message = JSONObject().apply {
            put("type", "ice_candidate")
            put("fromUserId", userId)
            put("toUserId", toUserId)
            put("candidate", JSONObject().apply {
                put("sdpMid", iceCandidate.sdpMid)
                put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                put("candidate", iceCandidate.sdp)
            })
        }
        sendMessage(message)
    }

    private fun createWebSocketListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            launch(Dispatchers.Main) {
                _connectionState.value = ConnectionState.Connected
                sendJoinRoom()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                handleSignalingMessage(text)
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error handling message: ${e.message}")
                webRTCListener.onError("Error processing message: ${e.message}")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            launch(Dispatchers.Main) {
                _connectionState.value = ConnectionState.Disconnected
                connectedPeers.keys.forEach { webRTCListener.onPeerDisconnected(it) }
                connectedPeers.clear()
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            launch(Dispatchers.Main) {
                _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
                webRTCListener.onError("WebSocket error: ${t.message}")
            }
        }
    }

    private fun handleSignalingMessage(message: String) {
        val json = JSONObject(message)
        when (json.getString("type")) {
            "offer" -> {
                val fromUserId = json.getString("fromUserId")
                val sdp = json.getString("sdp")
                val offer = SessionDescription(SessionDescription.Type.OFFER, sdp)
                webRTCListener.onOfferReceived(offer, fromUserId)
            }
            "answer" -> {
                val fromUserId = json.getString("fromUserId")
                val sdp = json.getString("sdp")
                val answer = SessionDescription(SessionDescription.Type.ANSWER, sdp)
                webRTCListener.onAnswerReceived(answer, fromUserId)
            }
            "ice_candidate" -> {
                val fromUserId = json.getString("fromUserId")
                val candidateJson = json.getJSONObject("candidate")
                val iceCandidate = IceCandidate(
                    candidateJson.getString("sdpMid"),
                    candidateJson.getInt("sdpMLineIndex"),
                    candidateJson.getString("candidate")
                )
                webRTCListener.onIceCandidateReceived(iceCandidate, fromUserId)
            }
            "user_left" -> {
                val userId = json.getString("userId")
                connectedPeers.remove(userId)
                webRTCListener.onPeerDisconnected(userId)
            }
        }
    }

    private fun sendJoinRoom() {
        val message = JSONObject().apply {
            put("type", "join_room")
            put("roomId", roomId)
            put("userId", userId)
        }
        sendMessage(message)
    }

    private fun sendMessage(message: JSONObject) {
        launch(Dispatchers.IO) {
            try {
                webSocket?.send(message.toString())
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error sending message: ${e.message}")
                _connectionState.value =
                    ConnectionState.Error("Failed to send message: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "WebRTCSignalingClient"
    }

    // Estados de conexión
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    // Listener para callbacks de WebRTC
    interface WebRTCListener {
        fun onOfferReceived(offer: SessionDescription, fromUserId: String)
        fun onAnswerReceived(answer: SessionDescription, fromUserId: String)
        fun onIceCandidateReceived(iceCandidate: IceCandidate, fromUserId: String)
        fun onPeerDisconnected(userId: String)
        fun onError(error: String)
    }
}