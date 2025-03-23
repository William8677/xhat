/*
 * Updated: 2025-01-25 21:42:45
 * Author: William8677
 */

package com.williamfq.xhat.call.engine

import android.content.Context
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCEngine @Inject constructor(
    private val context: Context
) {
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private val eglBase = EglBase.create()

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    private class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String) {}
        override fun onSetFailure(error: String) {}
    }

    init {
        initializePeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    fun createPeerConnection(observer: PeerConnection.Observer) {
        val factory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .createPeerConnectionFactory()

        val configuration = PeerConnection.RTCConfiguration(iceServers)

        peerConnection = factory.createPeerConnection(configuration, observer)
    }

    fun createOffer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        peerConnection?.createOffer(sdpObserver, constraints)
    }

    fun createAnswer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        peerConnection?.createAnswer(sdpObserver, constraints)
    }

    fun startLocalVideo(surfaceView: SurfaceViewRenderer) {
        val factory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .createPeerConnectionFactory()

        val capturer = createCameraCapturer()
        val videoSource = factory.createVideoSource(capturer.isScreencast)
        localVideoTrack = factory.createVideoTrack("video0", videoSource)

        surfaceView.init(eglBase.eglBaseContext, null)
        surfaceView.setMirror(true)
        surfaceView.setEnableHardwareScaler(true)
        localVideoTrack?.addSink(surfaceView)

        capturer.startCapture(1280, 720, 30)
    }

    fun startLocalAudio() {
        val factory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .createPeerConnectionFactory()

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        }

        val audioSource = factory.createAudioSource(constraints)
        localAudioTrack = factory.createAudioTrack("audio0", audioSource)
    }

    private fun createCameraCapturer(): CameraVideoCapturer {
        val cameraEnumerator = Camera2Enumerator(context)
        val deviceNames = cameraEnumerator.deviceNames

        // Intentar usar cámara frontal primero
        deviceNames.forEach { deviceName ->
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                cameraEnumerator.createCapturer(deviceName, null)?.let { return it }
            }
        }

        // Si no hay cámara frontal, usar cualquier cámara disponible
        deviceNames.forEach { deviceName ->
            cameraEnumerator.createCapturer(deviceName, null)?.let { return it }
        }

        throw IllegalStateException("No se encontró ninguna cámara")
    }

    fun setRemoteDescription(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(SimpleSdpObserver(), sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun dispose() {
        try {
            peerConnection?.dispose()
            localVideoTrack?.dispose()
            localAudioTrack?.dispose()
            eglBase.release()
        } catch (e: Exception) {
            // Log error if needed
        }
    }

    fun getEglBaseContext(): EglBase.Context = eglBase.eglBaseContext
}