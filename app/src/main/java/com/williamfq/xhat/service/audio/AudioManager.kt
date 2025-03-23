package com.williamfq.xhat.service.audio

import android.content.Context
import javax.inject.Inject

interface AudioManager {
    fun start()
    fun stop()
    fun setMuted(muted: Boolean)
    fun isMuted(): Boolean

    // Métodos adicionales sugeridos
    fun setVolume(volume: Float)
    fun getVolume(): Float
    fun setAudioDevice(deviceType: AudioDeviceType)
    fun getCurrentAudioDevice(): AudioDeviceType
    fun getAvailableAudioDevices(): List<AudioDeviceType>
    fun registerAudioDeviceCallback(callback: AudioDeviceCallback)
    fun unregisterAudioDeviceCallback(callback: AudioDeviceCallback)
}

enum class AudioDeviceType {
    SPEAKER_PHONE,
    WIRED_HEADSET,
    BLUETOOTH,
    EARPIECE,
    NONE
}

interface AudioDeviceCallback {
    fun onAudioDeviceChanged(newDevice: AudioDeviceType)
    fun onVolumeChanged(newVolume: Float)
    fun onMuteChanged(isMuted: Boolean)
}