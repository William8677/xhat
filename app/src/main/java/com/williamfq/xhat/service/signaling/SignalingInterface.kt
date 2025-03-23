package com.williamfq.xhat.service.signaling

import org.webrtc.SessionDescription
import org.webrtc.IceCandidate

interface SignalingInterface {
    fun connect()
    fun disconnect()
    fun sendOffer(offer: SessionDescription, toUserId: String)
    fun sendAnswer(answer: SessionDescription, toUserId: String)
    fun sendIceCandidate(iceCandidate: IceCandidate, toUserId: String)
}