package com.williamfq.xhat.call.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _currentDevice = MutableStateFlow<AudioDevice>(AudioDevice.Earpiece)
    val currentDevice: StateFlow<AudioDevice> = _currentDevice

    private val _availableDevices = MutableStateFlow<List<AudioDevice>>(emptyList())
    val availableDevices: StateFlow<List<AudioDevice>> = _availableDevices

    init {
        updateAvailableDevices()
    }

    /** Selects the specified audio device for communication. */
    fun selectAudioDevice(device: AudioDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and above: Use modern API
            val audioDeviceInfo = getAudioDeviceInfo(device)
            if (audioDeviceInfo != null) {
                audioManager.setCommunicationDevice(audioDeviceInfo)
                _currentDevice.value = device
            }
        } else {
            // Legacy support for Android 11 and below
            when (device) {
                AudioDevice.Bluetooth -> {
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager.startBluetoothSco()
                    audioManager.isBluetoothScoOn = true
                }
                AudioDevice.Speaker -> {
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager.stopBluetoothSco()
                    audioManager.isBluetoothScoOn = false
                    audioManager.isSpeakerphoneOn = true
                }
                AudioDevice.Earpiece -> {
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager.stopBluetoothSco()
                    audioManager.isBluetoothScoOn = false
                    audioManager.isSpeakerphoneOn = false
                }
                AudioDevice.WiredHeadset -> {
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager.stopBluetoothSco()
                    audioManager.isBluetoothScoOn = false
                    audioManager.isSpeakerphoneOn = false
                }
            }
            _currentDevice.value = device
        }
    }

    /** Retrieves the AudioDeviceInfo for the specified AudioDevice. */
    private fun getAudioDeviceInfo(device: AudioDevice): AudioDeviceInfo? {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.firstOrNull { info ->
            when (device) {
                AudioDevice.Bluetooth -> info.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                AudioDevice.WiredHeadset -> info.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || info.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                AudioDevice.Earpiece -> info.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                AudioDevice.Speaker -> info.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
        }
    }

    /** Updates the list of available audio devices. */
    private fun updateAvailableDevices() {
        val devices = mutableListOf<AudioDevice>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in audioDevices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> devices.add(AudioDevice.Bluetooth)
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> devices.add(AudioDevice.WiredHeadset)
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> devices.add(AudioDevice.Earpiece)
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> devices.add(AudioDevice.Speaker)
                }
            }
        } else {
            // Fallback for pre-Marshmallow (API 23)
            devices.add(AudioDevice.Earpiece)
            devices.add(AudioDevice.Speaker)
            if (audioManager.isWiredHeadsetOn) {
                devices.add(AudioDevice.WiredHeadset)
            }
            if (audioManager.isBluetoothScoAvailableOffCall) {
                devices.add(AudioDevice.Bluetooth)
            }
        }

        _availableDevices.value = devices.distinct()
    }

    /** Cleans up audio settings and resets to default state. */
    fun cleanup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.clearCommunicationDevice()
        } else {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            audioManager.isSpeakerphoneOn = false
        }
    }
}

enum class AudioDevice {
    Earpiece,
    Speaker,
    WiredHeadset,
    Bluetooth
}