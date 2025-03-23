/*
 * Updated: 2025-01-21 23:36:03
 * Author: William8677
 */

package com.williamfq.xhat.call.signaling

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketSignaling @Inject constructor() {
    private var webSocket: WebSocket? = null
    private val _signalingMessages = MutableSharedFlow<SignalingMessage>()
    val signalingMessages: SharedFlow<SignalingMessage> = _signalingMessages

    fun connect(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                when (json.getString("type")) {
                    "offer" -> handleOffer(json)
                    "answer" -> handleAnswer(json)
                    "ice-candidate" -> handleIceCandidate(json)
                }
            }
        })
    }

    fun sendOffer(sdp: String, targetUserId: String) {
        val message = JSONObject().apply {
            put("type", "offer")
            put("sdp", sdp)
            put("targetUserId", targetUserId)
        }
        webSocket?.send(message.toString())
    }

    fun sendAnswer(sdp: String, targetUserId: String) {
        val message = JSONObject().apply {
            put("type", "answer")
            put("sdp", sdp)
            put("targetUserId", targetUserId)
        }
        webSocket?.send(message.toString())
    }

    fun sendIceCandidate(candidate: String, targetUserId: String) {
        val message = JSONObject().apply {
            put("type", "ice-candidate")
            put("candidate", candidate)
            put("targetUserId", targetUserId)
        }
        webSocket?.send(message.toString())
    }

    private fun handleOffer(json: JSONObject) {
        // Manejar oferta recibida
    }

    private fun handleAnswer(json: JSONObject) {
        // Manejar respuesta recibida
    }

    private fun handleIceCandidate(json: JSONObject) {
        // Manejar candidato ICE recibido
    }

    fun disconnect() {
        webSocket?.close(1000, "Desconexión normal")
        webSocket = null
    }
}

sealed class SignalingMessage {
    data class Offer(val sdp: String, val fromUserId: String) : SignalingMessage()
    data class Answer(val sdp: String, val fromUserId: String) : SignalingMessage()
    data class IceCandidate(val candidate: String, val fromUserId: String) : SignalingMessage()
}