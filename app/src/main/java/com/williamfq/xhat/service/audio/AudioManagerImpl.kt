/*
 * Updated: 2025-02-13 00:07:32
 * Author: William8677
 */

package com.williamfq.xhat.service.audio

import android.content.Context
import android.media.AudioManager as AndroidAudioManager
import javax.inject.Inject

class AudioManagerImpl @Inject constructor(
    private val context: Context
) : AudioManager {
    private var muted = false
    private var currentVolume = 1.0f
    private var currentDevice = AudioDeviceType.EARPIECE
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AndroidAudioManager
    private val deviceCallbacks = mutableSetOf<AudioDeviceCallback>()

    override fun start() {
        // Implementar inicio de audio
        updateAudioDevices()
    }

    override fun stop() {
        // Implementar parada de audio
        deviceCallbacks.clear()
    }

    override fun setMuted(muted: Boolean) {
        if (this.muted != muted) {
            this.muted = muted
            if (muted) {
                setVolume(0f)
            } else {
                setVolume(currentVolume)
            }
            deviceCallbacks.forEach { it.onMuteChanged(muted) }
        }
    }

    override fun isMuted(): Boolean = muted

    override fun setVolume(volume: Float) {
        val newVolume = volume.coerceIn(0f, 1f)
        if (currentVolume != newVolume) {
            currentVolume = newVolume
            if (!muted) {
                val streamVolume = (currentVolume * audioManager.getStreamMaxVolume(AndroidAudioManager.STREAM_VOICE_CALL)).toInt()
                audioManager.setStreamVolume(
                    AndroidAudioManager.STREAM_VOICE_CALL,
                    streamVolume,
                    0
                )
            }
            deviceCallbacks.forEach { it.onVolumeChanged(currentVolume) }
        }
    }

    override fun getVolume(): Float = currentVolume

    override fun setAudioDevice(deviceType: AudioDeviceType) {
        if (currentDevice != deviceType) {
            when (deviceType) {
                AudioDeviceType.SPEAKER_PHONE -> {
                    audioManager.isSpeakerphoneOn = true
                    audioManager.mode = AndroidAudioManager.MODE_IN_COMMUNICATION
                }
                AudioDeviceType.EARPIECE -> {
                    audioManager.isSpeakerphoneOn = false
                    audioManager.mode = AndroidAudioManager.MODE_IN_COMMUNICATION
                }
                AudioDeviceType.BLUETOOTH -> {
                    audioManager.isSpeakerphoneOn = false
                    audioManager.mode = AndroidAudioManager.MODE_IN_COMMUNICATION
                    // Implementar lógica específica para Bluetooth
                }
                AudioDeviceType.WIRED_HEADSET -> {
                    audioManager.isSpeakerphoneOn = false
                    audioManager.mode = AndroidAudioManager.MODE_IN_COMMUNICATION
                }
                AudioDeviceType.NONE -> {
                    // No hacer nada o manejar caso especial
                }
            }
            currentDevice = deviceType
            deviceCallbacks.forEach { it.onAudioDeviceChanged(deviceType) }
        }
    }

    override fun getCurrentAudioDevice(): AudioDeviceType = currentDevice

    override fun getAvailableAudioDevices(): List<AudioDeviceType> {
        val devices = mutableListOf<AudioDeviceType>()

        // Siempre disponible
        devices.add(AudioDeviceType.EARPIECE)
        devices.add(AudioDeviceType.SPEAKER_PHONE)

        // Verificar auriculares con cable
        if (audioManager.isWiredHeadsetOn) {
            devices.add(AudioDeviceType.WIRED_HEADSET)
        }

        // Verificar dispositivos Bluetooth
        if (audioManager.isBluetoothScoAvailableOffCall) {
            devices.add(AudioDeviceType.BLUETOOTH)
        }

        return devices
    }

    override fun registerAudioDeviceCallback(callback: AudioDeviceCallback) {
        deviceCallbacks.add(callback)
    }

    override fun unregisterAudioDeviceCallback(callback: AudioDeviceCallback) {
        deviceCallbacks.remove(callback)
    }

    private fun updateAudioDevices() {
        // Actualizar estado inicial de dispositivos de audio
        val availableDevices = getAvailableAudioDevices()
        if (availableDevices.isNotEmpty()) {
            setAudioDevice(availableDevices.first())
        }
    }
}