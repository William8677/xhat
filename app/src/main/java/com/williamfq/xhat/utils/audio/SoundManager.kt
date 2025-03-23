package com.williamfq.xhat.utils.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.williamfq.xhat.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    private val context: Context
) {
    private var callRingtonePlayer: MediaPlayer? = null
    private var messagePlayer: MediaPlayer? = null
    private var notificationPlayer: MediaPlayer? = null

    fun playCallRingtone() {
        callRingtonePlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        } ?: initCallRingtone()
    }

    fun stopCallRingtone() {
        callRingtonePlayer?.apply {
            if (isPlaying) {
                stop()
                prepare()
            }
        }
    }

    fun playMessageTone() {
        messagePlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        } ?: initMessageTone()
    }

    fun playNotificationTone() {
        notificationPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        } ?: initNotificationTone()
    }

    private fun initCallRingtone() {
        callRingtonePlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.xhat_ringtone}"))
            isLooping = true
            prepare()
            start()
        }
    }

    private fun initMessageTone() {
        messagePlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.xhat_message}"))
            prepare()
            start()
        }
    }

    private fun initNotificationTone() {
        notificationPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.xhat_notification}"))
            prepare()
            start()
        }
    }

    fun release() {
        callRingtonePlayer?.release()
        callRingtonePlayer = null

        messagePlayer?.release()
        messagePlayer = null

        notificationPlayer?.release()
        notificationPlayer = null
    }
}