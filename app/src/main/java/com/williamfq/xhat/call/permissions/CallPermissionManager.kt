/*
 * Updated: 2025-01-21 22:54:34
 * Author: William8677
 */

package com.williamfq.xhat.call.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun checkAndRequestPermissions(
        isVideoCall: Boolean,
        permissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        val requiredPermissions = mutableListOf<String>().apply {
            add(Manifest.permission.RECORD_AUDIO)
            if (isVideoCall) {
                add(Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    fun hasRequiredPermissions(isVideoCall: Boolean): Boolean {
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val hasVideoPermission = if (isVideoCall) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return hasAudioPermission && hasVideoPermission && hasNotificationPermission
    }

    fun getMissingPermissions(isVideoCall: Boolean): List<CallPermission> {
        return mutableListOf<CallPermission>().apply {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                add(CallPermission.MICROPHONE)
            }

            if (isVideoCall && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                add(CallPermission.CAMERA)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                add(CallPermission.NOTIFICATIONS)
            }
        }
    }
}

enum class CallPermission(
    val permission: String,
    val title: String,
    val description: String
) {
    MICROPHONE(
        permission = Manifest.permission.RECORD_AUDIO,
        title = "Micrófono",
        description = "Necesario para realizar llamadas de voz"
    ),
    CAMERA(
        permission = Manifest.permission.CAMERA,
        title = "Cámara",
        description = "Necesario para realizar videollamadas"
    ),
    NOTIFICATIONS(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        title = "Notificaciones",
        description = "Necesario para recibir notificaciones de llamadas"
    )
}