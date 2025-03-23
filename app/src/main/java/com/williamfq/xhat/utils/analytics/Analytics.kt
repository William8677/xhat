package com.williamfq.xhat.utils.analytics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Analytics @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: LoggerInterface
) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
    private val analyticsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isInitialized = false

    companion object {
        private const val USER_ID = "William8677"
        private const val TIMESTAMP = "2025-02-21 20:03:37"
        private const val TAG = "Analytics"
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    init {
        analyticsScope.launch {
            try {
                setupDefaultProperties()
                // Usamos setUserProperty para establecer la versión de la app como propiedad
                setUserProperty(UserProperty("app_version", getAppVersion()))
                isInitialized = true
            } catch (e: Exception) {
                handleInitializationError(e)
            }
        }
    }

    private suspend fun setupDefaultProperties() {
        tryWithRetries("setupDefaultProperties") {
            firebaseAnalytics.setUserId(USER_ID)
            crashlytics.apply {
                setUserId(USER_ID)
                setCustomKey("timestamp", TIMESTAMP)
                setCustomKey("initialization_time", System.currentTimeMillis())
            }
            logger.logEvent(TAG, "Default properties setup completed", LogLevel.DEBUG)
        }
    }

    suspend fun logEvent(event: AnalyticsEvent) {
        if (!isInitialized) {
            logger.logEvent(TAG, "Analytics not initialized", LogLevel.WARNING)
            return
        }

        analyticsScope.launch {
            tryWithRetries("logEvent") {
                val bundle = Bundle().apply {
                    event.parameters.forEach { (key, value) ->
                        when (value) {
                            is String -> putString(key, value)
                            is Int -> putInt(key, value)
                            is Long -> putLong(key, value)
                            is Float -> putFloat(key, value)
                            is Double -> putDouble(key, value)
                            is Boolean -> putBoolean(key, value)
                            is List<*> -> putStringArrayList(key, ArrayList(value.map { it.toString() }))
                        }
                    }
                    putString("event_timestamp", TIMESTAMP)
                    putString("user_id", USER_ID)
                }

                firebaseAnalytics.logEvent(event.name, bundle)
                logger.logEvent(TAG, "Analytics event logged: ${event.name}", LogLevel.DEBUG)
            }
        }
    }

    suspend fun trackEvent(eventName: String) {
        if (!isInitialized) {
            logger.logEvent(TAG, "Analytics not initialized", LogLevel.WARNING)
            return
        }

        analyticsScope.launch {
            tryWithRetries("trackEvent") {
                val simpleEvent = object : AnalyticsEvent {
                    override val name = eventName
                    override val parameters: Map<String, Any> = mapOf(
                        "timestamp" to TIMESTAMP,
                        "user_id" to USER_ID
                    )
                }
                logEvent(simpleEvent)
            }
        }
    }

    suspend fun setUserProperty(property: UserProperty) {
        if (!isInitialized) {
            logger.logEvent(TAG, "Analytics not initialized", LogLevel.WARNING)
            return
        }

        analyticsScope.launch {
            tryWithRetries("setUserProperty") {
                firebaseAnalytics.setUserProperty(property.name, property.value)
                crashlytics.setCustomKey(property.name, property.value)
                logger.logEvent(
                    TAG,
                    "User property set: ${property.name} = ${property.value}",
                    LogLevel.DEBUG
                )
            }
        }
    }

    /** Helper function to execute a block with retries. */
    private suspend fun tryWithRetries(operationName: String, block: suspend () -> Unit) {
        var attempts = 0
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                block()
                return
            } catch (e: Exception) {
                attempts++
                if (attempts == MAX_RETRY_ATTEMPTS) {
                    handleAnalyticsError("Failed $operationName after $MAX_RETRY_ATTEMPTS attempts", e)
                    return
                }
                logger.logEvent(TAG, "$operationName failed, retrying ($attempts/$MAX_RETRY_ATTEMPTS)", LogLevel.WARNING, e)
                delay(1000L * attempts) // Exponential backoff: 1s, 2s, 3s
            }
        }
    }

    private fun handleAnalyticsError(message: String, error: Exception) {
        analyticsScope.launch {
            try {
                logger.logEvent(TAG, message, LogLevel.ERROR, error)
                crashlytics.recordException(error)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Critical error in analytics")
            }
        }
    }

    private fun handleInitializationError(error: Exception) {
        Timber.tag(TAG).e(error, "Failed to initialize Analytics")
        analyticsScope.launch {
            logger.logEvent(TAG, "Analytics initialization failed", LogLevel.ERROR, error)
        }
    }

    /** Retrieves the app version name from the package info. */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.tag(TAG).e(e, "Failed to get app version")
            "unknown"
        }
    }
}

interface AnalyticsEvent {
    val name: String
    val parameters: Map<String, Any>
}

data class UserProperty(
    val name: String,
    val value: String
)