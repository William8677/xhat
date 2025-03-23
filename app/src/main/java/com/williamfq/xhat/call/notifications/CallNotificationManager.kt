/*
 * Updated: 2025-01-26 17:51:45
 * Author: William8677
 */

package com.williamfq.xhat.call.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.williamfq.domain.model.User
import com.williamfq.xhat.MainActivity
import com.williamfq.xhat.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createCallNotificationChannels()
    }

    private fun createCallNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val incomingChannel = NotificationChannel(
                CHANNEL_INCOMING_CALLS,
                "Llamadas entrantes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para llamadas entrantes"
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val ongoingChannel = NotificationChannel(
                CHANNEL_ONGOING_CALLS,
                "Llamadas en curso",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para llamadas en curso"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannels(listOf(incomingChannel, ongoingChannel))
        }
    }

    fun showIncomingCallNotification(
        caller: User,
        isVideoCall: Boolean,
        callId: String
    ) {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_CALLER_ID, caller.id)
            putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val acceptIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ACCEPT_CALL
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_CALLER_ID, caller.id)
            putExtra(EXTRA_USERNAME, caller.username)
            putExtra(EXTRA_AVATAR_URL, caller.avatarUrl)
            putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall)
        }

        val acceptPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            acceptIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val rejectIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_REJECT_CALL
            putExtra(EXTRA_CALL_ID, callId)
        }

        val rejectPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            rejectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_INCOMING_CALLS)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle(if (isVideoCall) "Videollamada entrante" else "Llamada entrante")
            .setContentText("De ${caller.username}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setTimeoutAfter(45000) // 45 segundos
            .addAction(
                R.drawable.ic_call_accept,
                "Aceptar",
                acceptPendingIntent
            )
            .addAction(
                R.drawable.ic_call_reject,
                "Rechazar",
                rejectPendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_INCOMING_CALL_ID, notification)
    }

    fun showOngoingCallNotification(
        user: User,
        isVideoCall: Boolean,
        callDuration: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ONGOING_CALLS)
            .setSmallIcon(R.drawable.ic_call_ongoing)
            .setContentTitle(if (isVideoCall) "Videollamada en curso" else "Llamada en curso")
            .setContentText("${user.username} • $callDuration")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .build()

        notificationManager.notify(NOTIFICATION_ONGOING_CALL_ID, notification)
    }

    fun cancelIncomingCallNotification() {
        notificationManager.cancel(NOTIFICATION_INCOMING_CALL_ID)
    }

    fun cancelOngoingCallNotification() {
        notificationManager.cancel(NOTIFICATION_ONGOING_CALL_ID)
    }

    companion object {
        const val CHANNEL_INCOMING_CALLS = "incoming_calls_channel"
        const val CHANNEL_ONGOING_CALLS = "ongoing_calls_channel"
        private const val NOTIFICATION_INCOMING_CALL_ID = 1001
        const val NOTIFICATION_ONGOING_CALL_ID = 1002
        const val NOTIFICATION_SERVICE_ID = 2001

        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_CALLER_ID = "extra_caller_id"
        const val EXTRA_IS_VIDEO_CALL = "extra_is_video_call"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_AVATAR_URL = "extra_avatar_url"
    }
}