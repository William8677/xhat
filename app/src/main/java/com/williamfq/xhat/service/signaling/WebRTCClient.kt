package com.williamfq.xhat.service.signaling

import android.content.Context
import org.webrtc.*
import timber.log.Timber

class WebRTCClient(
    private val context: Context,
    private val eglBaseContext: EglBase.Context,
    private val listener: WebRTCListener
) {
    private var peerConnection: PeerConnection? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var remoteUserId: String? = null

    interface WebRTCListener {
        fun onCallConnected()
        fun onCallEnded()
        fun onLocalVideoTrackCreated(videoTrack: VideoTrack)
        fun onRemoteVideoTrackReceived(videoTrack: VideoTrack)
        fun onLocalAudioTrackCreated(audioTrack: AudioTrack)
        fun onRemoteAudioTrackReceived(audioTrack: AudioTrack)
        fun onError(error: String)
    }

    fun initialize(
        videoWidth: Int,
        videoHeight: Int,
        videoFps: Int,
        audioEnabled: Boolean,
        videoEnabled: Boolean
    ) {
        try {
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
                .createPeerConnectionFactory()

            if (videoEnabled) {
                videoCapturer = createVideoCapturer()
                setupLocalVideoTrack(videoWidth, videoHeight, videoFps)
            }

            if (audioEnabled) {
                setupLocalAudioTrack()
            }

            setupPeerConnection()
        } catch (e: Exception) {
            Timber.e(e, "Error initializing WebRTCClient")
            listener.onError("Error initializing WebRTCClient: ${e.message}")
        }
    }

    private fun createVideoCapturer(): CameraVideoCapturer {
        val cameraEnumerator = Camera2Enumerator(context)
        val deviceNames = cameraEnumerator.deviceNames
        for (deviceName in deviceNames) {
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                return cameraEnumerator.createCapturer(deviceName, null) as CameraVideoCapturer
            }
        }
        throw IllegalStateException("No front-facing camera found.")
    }

    private fun setupLocalVideoTrack(videoWidth: Int, videoHeight: Int, videoFps: Int) {
        val videoSource = peerConnectionFactory?.createVideoSource(false)
        videoCapturer?.initialize(
            SurfaceTextureHelper.create("CaptureThread", eglBaseContext),
            context,
            videoSource?.capturerObserver
        )
        videoCapturer?.startCapture(videoWidth, videoHeight, videoFps)

        localVideoTrack = peerConnectionFactory?.createVideoTrack("LOCAL_VIDEO", videoSource)
        listener.onLocalVideoTrackCreated(localVideoTrack!!)
    }

    private fun setupLocalAudioTrack() {
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("LOCAL_AUDIO", audioSource)
        listener.onLocalAudioTrackCreated(localAudioTrack!!)
    }

    private fun setupPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                peerConnection?.addIceCandidate(candidate)
            }

            override fun onAddStream(stream: MediaStream) {
                if (stream.videoTracks.isNotEmpty()) {
                    listener.onRemoteVideoTrackReceived(stream.videoTracks[0])
                }
                if (stream.audioTracks.isNotEmpty()) {
                    listener.onRemoteAudioTrackReceived(stream.audioTracks[0])
                }
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                if (state == PeerConnection.IceConnectionState.DISCONNECTED) {
                    listener.onCallEnded()
                }
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {}
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onDataChannel(channel: DataChannel) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onRenegotiationNeeded() {}
            override fun onTrack(transceiver: RtpTransceiver) {}
            override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {}
        })
    }

    fun startLocalVideo() {
        localVideoTrack?.setEnabled(true)
    }

    fun startLocalAudio() {
        localAudioTrack?.setEnabled(true)
    }

    fun createOffer(targetUserId: String) {
        val constraints = MediaConstraints()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        // Enviar la descripción de sesión al servidor de señalización
                    }

                    override fun onSetFailure(error: String) {
                        listener.onError("Error setting local description: $error")
                    }

                    override fun onCreateSuccess(description: SessionDescription) {}
                    override fun onCreateFailure(error: String) {}
                }, description)
                remoteUserId = targetUserId // Set the remote user ID
            }

            override fun onCreateFailure(error: String) {
                listener.onError("Error creating offer: $error")
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    fun toggleVideo(enabled: Boolean) {
        localVideoTrack?.setEnabled(enabled)
    }

    fun toggleAudio(enabled: Boolean) {
        localAudioTrack?.setEnabled(enabled)
    }

    fun stopLocalVideo() {
        videoCapturer?.stopCapture()
        localVideoTrack?.setEnabled(false)
    }

    fun stopLocalAudio() {
        localAudioTrack?.setEnabled(false)
    }

    fun disconnect() {
        peerConnection?.close()
    }

    fun release() {
        videoCapturer?.dispose()
        peerConnectionFactory?.dispose()
    }

    fun getRemoteUserId(): String? {
        return remoteUserId
    }
}