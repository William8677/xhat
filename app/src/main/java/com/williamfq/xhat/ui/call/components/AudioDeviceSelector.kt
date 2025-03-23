/*
 * Updated: 2025-01-21 23:04:33
 * Author: William8677
 */

package com.williamfq.xhat.ui.call.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.call.audio.AudioDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDeviceSelector(
    currentDevice: AudioDevice,
    availableDevices: List<AudioDevice>,
    onDeviceSelected: (AudioDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeviceMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showDeviceMenu = true }
        ) {
            Icon(
                imageVector = getAudioDeviceIcon(currentDevice),
                contentDescription = "Seleccionar dispositivo de audio"
            )
        }

        DropdownMenu(
            expanded = showDeviceMenu,
            onDismissRequest = { showDeviceMenu = false }
        ) {
            availableDevices.forEach { device ->
                DropdownMenuItem(
                    text = { Text(getAudioDeviceName(device)) },
                    leadingIcon = {
                        Icon(
                            imageVector = getAudioDeviceIcon(device),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onDeviceSelected(device)
                        showDeviceMenu = false
                    },
                    trailingIcon = if (device == currentDevice) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun getAudioDeviceIcon(device: AudioDevice) = when (device) {
    AudioDevice.Bluetooth -> Icons.Default.Bluetooth
    AudioDevice.Speaker -> Icons.Default.VolumeUp
    AudioDevice.Earpiece -> Icons.Default.Phone
    AudioDevice.WiredHeadset -> Icons.Default.Headphones
}

private fun getAudioDeviceName(device: AudioDevice) = when (device) {
    AudioDevice.Bluetooth -> "Bluetooth"
    AudioDevice.Speaker -> "Altavoz"
    AudioDevice.Earpiece -> "Auricular"
    AudioDevice.WiredHeadset -> "Auriculares"
}