/*
 * Updated: 2025-01-26 17:57:27
 * Author: William8677
 */

package com.williamfq.xhat.call.signaling

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallSignalingService @Inject constructor() {
    private var webSocket: WebSocket? = null
    private val _signalEvents = MutableSharedFlow<SignalEvent>()
    val signalEvents: SharedFlow<SignalEvent> = _signalEvents

    fun connectToSignalingServer(userId: String) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("wss://tu-servidor-websocket.com/ws/$userId")
                .build()

            webSocket = client.newWebSocket(request, createWebSocketListener())
            Timber.d("Conectando al servidor de señalización: $userId")
        } catch (e: Exception) {
            Timber.e(e, "Error conectando al servidor de señalización")
        }
    }

    private fun createWebSocketListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket conectado: ${response.code}")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val json = JSONObject(text)
                when (json.getString("type")) {
                    "call_offer" -> handleCallOffer(json)
                    "call_answer" -> handleCallAnswer(json)
                    "ice_candidate" -> handleIceCandidate(json)
                    "call_end" -> handleCallEnd(json)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error procesando mensaje WebSocket")
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "Error en WebSocket: ${response?.code}")
            _signalEvents.tryEmit(SignalEvent.SignalingError("Error de conexión: ${t.message}"))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket cerrado: $code - $reason")
        }
    }

    fun sendCallOffer(receiverId: String, callType: CallType, sdpOffer: String) {
        try {
            val message = JSONObject().apply {
                put("type", "call_offer")
                put("receiverId", receiverId)
                put("callType", callType.name)
                put("sdp", sdpOffer)
            }
            webSocket?.send(message.toString())
            Timber.d("Oferta de llamada enviada a: $receiverId")
        } catch (e: Exception) {
            Timber.e(e, "Error enviando oferta de llamada")
        }
    }

    fun sendCallAnswer(callId: String, accepted: Boolean, sdpAnswer: String? = null) {
        try {
            val message = JSONObject().apply {
                put("type", "call_answer")
                put("callId", callId)
                put("accepted", accepted)
                sdpAnswer?.let { put("sdp", it) }
            }
            webSocket?.send(message.toString())
            Timber.d("Respuesta de llamada enviada: callId=$callId, accepted=$accepted")
        } catch (e: Exception) {
            Timber.e(e, "Error enviando respuesta de llamada")
        }
    }

    fun sendIceCandidate(callId: String, candidate: String, sdpMid: String, sdpMLineIndex: Int) {
        try {
            val message = JSONObject().apply {
                put("type", "ice_candidate")
                put("callId", callId)
                put("candidate", candidate)
                put("sdpMid", sdpMid)
                put("sdpMLineIndex", sdpMLineIndex)
            }
            webSocket?.send(message.toString())
            Timber.d("ICE candidate enviado para callId=$callId")
        } catch (e: Exception) {
            Timber.e(e, "Error enviando ICE candidate")
        }
    }

    private fun handleCallOffer(json: JSONObject) {
        try {
            val callId = json.getString("callId")
            val callerId = json.getString("callerId")
            val callType = CallType.valueOf(json.getString("callType"))
            val sdpOffer = json.getString("sdp")
            _signalEvents.tryEmit(SignalEvent.IncomingCall(callId, callerId, callType, sdpOffer))
            Timber.d("Llamada entrante recibida de: $callerId")
        } catch (e: Exception) {
            Timber.e(e, "Error procesando oferta de llamada")
        }
    }

    private fun handleCallAnswer(json: JSONObject) {
        try {
            val callId = json.getString("callId")
            val accepted = json.getBoolean("accepted")
            val sdpAnswer = if (accepted) json.optString("sdp") else null
            _signalEvents.tryEmit(SignalEvent.CallAnswered(callId, accepted, sdpAnswer))
            Timber.d("Respuesta de llamada recibida: callId=$callId, accepted=$accepted")
        } catch (e: Exception) {
            Timber.e(e, "Error procesando respuesta de llamada")
        }
    }

    private fun handleIceCandidate(json: JSONObject) {
        try {
            val callId = json.getString("callId")
            val candidate = json.getString("candidate")
            val sdpMid = json.getString("sdpMid")
            val sdpMLineIndex = json.getInt("sdpMLineIndex")
            _signalEvents.tryEmit(
                SignalEvent.IceCandidate(
                    callId,
                    candidate,
                    sdpMid,
                    sdpMLineIndex
                )
            )
            Timber.d("ICE candidate recibido para callId=$callId")
        } catch (e: Exception) {
            Timber.e(e, "Error procesando ICE candidate")
        }
    }

    private fun handleCallEnd(json: JSONObject) {
        try {
            val callId = json.getString("callId")
            val reason = json.optString("reason", "Normal closure")
            _signalEvents.tryEmit(SignalEvent.CallEnded(callId, reason))
            Timber.d("Llamada finalizada: callId=$callId, reason=$reason")
        } catch (e: Exception) {
            Timber.e(e, "Error procesando fin de llamada")
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            Timber.d("Desconectado del servidor de señalización")
        } catch (e: Exception) {
            Timber.e(e, "Error desconectando del servidor de señalización")
        }
    }
}

sealed class SignalEvent {
    data class IncomingCall(
        val callId: String,
        val callerId: String,
        val callType: CallType,
        val sdpOffer: String
    ) : SignalEvent()

    data class CallAnswered(
        val callId: String,
        val accepted: Boolean,
        val sdpAnswer: String? = null
    ) : SignalEvent()

    data class IceCandidate(
        val callId: String,
        val candidate: String,
        val sdpMid: String,
        val sdpMLineIndex: Int
    ) : SignalEvent()

    data class CallEnded(
        val callId: String,
        val reason: String = "Normal closure"
    ) : SignalEvent()

    data class SignalingError(
        val message: String
    ) : SignalEvent()
}

enum class CallType {
    VOICE,
    VIDEO
}