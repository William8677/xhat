package com.williamfq.xhat.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.williamfq.xhat.R
import com.williamfq.xhat.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val androidNotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PROCESSING,
                    "Procesamiento",
                    AndroidNotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notificaciones de procesamiento de imágenes"
                },
                NotificationChannel(
                    CHANNEL_EXPORT,
                    "Exportación",
                    AndroidNotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones de exportación de imágenes"
                },
                NotificationChannel(
                    CHANNEL_ERROR,
                    "Errores",
                    AndroidNotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de errores"
                }
            )

            try {
                androidNotificationManager.createNotificationChannels(channels)
            } catch (e: SecurityException) {
                // Manejar o registrar la excepción según se requiera
            }
        }
    }

    // Verifica si se tiene el permiso POST_NOTIFICATIONS (requerido a partir de Android 13)
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showProcessingNotification(
        title: String,
        progress: Int,
        maxProgress: Int
    ) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_PROCESSING)
            .setSmallIcon(R.drawable.ic_processing)
            .setContentTitle(title)
            .setProgress(maxProgress, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_PROCESSING_ID, notification)
        } catch (e: SecurityException) {
            // Manejar o registrar la excepción según se requiera
        }
    }

    fun showExportNotification(
        title: String,
        message: String,
        uri: String? = null
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            uri?.let { data = Uri.parse(it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EXPORT)
            .setSmallIcon(R.drawable.ic_export)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_EXPORT_ID, notification)
        } catch (e: SecurityException) {
            // Manejar o registrar la excepción según se requiera
        }
    }

    fun showErrorNotification(
        title: String,
        message: String
    ) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ERROR)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ERROR_ID, notification)
        } catch (e: SecurityException) {
            // Manejar o registrar la excepción según se requiera
        }
    }

    fun cancelProcessingNotification() {
        try {
            notificationManager.cancel(NOTIFICATION_PROCESSING_ID)
        } catch (e: SecurityException) {
            // Manejar o registrar la excepción según se requiera
        }
    }

    companion object {
        private const val CHANNEL_PROCESSING = "processing_channel"
        private const val CHANNEL_EXPORT = "export_channel"
        private const val CHANNEL_ERROR = "error_channel"

        private const val NOTIFICATION_PROCESSING_ID = 1
        private const val NOTIFICATION_EXPORT_ID = 2
        private const val NOTIFICATION_ERROR_ID = 3
    }
}
