package com.williamfq.xhat.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.williamfq.domain.location.LocationTracker
import com.williamfq.domain.models.Location
import com.williamfq.domain.usecases.SendPanicAlertUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PanicService : Service() {

    @Inject
    lateinit var sendPanicAlertUseCase: SendPanicAlertUseCase

    @Inject
    lateinit var locationTracker: LocationTracker

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = intent?.getStringExtra(EXTRA_USER_ID)
        if (userId == null) {
            Timber.e("User ID not provided")
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            try {
                val location = locationTracker.getCurrentLocation().first()
                if (location != null) {
                    sendPanicAlert(userId, location)
                } else {
                    Timber.e("Could not get location")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending panic alert")
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun sendPanicAlert(userId: String, location: Location) {
        try {
            sendPanicAlertUseCase.invoke(
                message = buildPanicMessage(location),
                userId = userId,
                location = location
            )
            Timber.i("Panic alert sent successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send panic alert")
        }
    }

    private fun buildPanicMessage(location: Location): String {
        return buildString {
            append("¡EMERGENCIA! ")
            append("Necesito ayuda inmediata. ")
            append("Mi ubicación actual es: ")
            append("${location.latitude}, ${location.longitude}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        private const val TAG = "PanicService"
    }
}