package com.williamfq.xhat.utils.analytics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.williamfq.xhat.ui.stories.AnalyticsManagerInterface
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
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsManagerInterface {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var crashlytics: FirebaseCrashlytics? = null
    private var isEnabled = false
    private var userId: String? = null
    private var isInitializing = false
    private val analyticsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val USER_ID = "William8677"
        private const val TIMESTAMP = "2025-02-21 20:01:53"
        private const val DEFAULT_ERROR_TAG = "unknown"
        private const val EVENT_AD = "ad_event"
        private const val EVENT_SCREEN_VIEW = "screen_view"
        private const val TAG = "AnalyticsManager"
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    @Synchronized
    fun initialize(enabled: Boolean, userId: String?, properties: Map<String, Any>) {
        if (isInitializing) {
            Timber.w("Analytics initialization already in progress")
            return
        }

        isInitializing = true
        analyticsScope.launch {
            try {
                tryWithRetries("initialize") {
                    this@AnalyticsManager.isEnabled = enabled
                    this@AnalyticsManager.userId = userId

                    if (enabled) {
                        initializeFirebaseComponents()
                        setupUserAndProperties(userId, properties)
                        setUserProperty("app_version", getAppVersion())
                        logScreenView("AppStarted", "AnalyticsManager")
                        logAllAdEvents(userId ?: USER_ID) // Usamos userId como adUnitId dinámico
                        Timber.d("Analytics initialized successfully")
                    }
                }
            } catch (e: Exception) {
                handleInitializationError(e) // Usamos handleInitializationError
            } finally {
                isInitializing = false
            }
        }
    }

    private fun initializeFirebaseComponents() {
        try {
            firebaseAnalytics = Firebase.analytics
            crashlytics = FirebaseCrashlytics.getInstance()

            firebaseAnalytics?.setAnalyticsCollectionEnabled(true)
            crashlytics?.isCrashlyticsCollectionEnabled = true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error initializing Firebase components")
            throw e
        }
    }

    private fun setupUserAndProperties(userId: String?, properties: Map<String, Any>) {
        try {
            userId?.let {
                firebaseAnalytics?.setUserId(it)
                crashlytics?.setUserId(it)
            }

            properties.forEach { (key, value) ->
                when (value) {
                    is String -> {
                        firebaseAnalytics?.setUserProperty(key, value)
                        crashlytics?.setCustomKey(key, value)
                    }
                    is Number -> {
                        crashlytics?.setCustomKey(key, value.toString())
                        firebaseAnalytics?.setUserProperty(key, value.toString())
                    }
                    is Boolean -> {
                        crashlytics?.setCustomKey(key, value)
                        firebaseAnalytics?.setUserProperty(key, value.toString())
                    }
                }
            }

            crashlytics?.apply {
                setCustomKey("initialization_time", TIMESTAMP)
                setCustomKey("default_user", USER_ID)
                setCustomKey("analytics_enabled", isEnabled)
            }

            Timber.d("User and properties setup completed for user: $userId")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error setting up user and properties")
            throw e
        }
    }

    override fun logEvent(eventName: String, params: Bundle) {
        if (!isEnabled) return
        analyticsScope.launch {
            tryWithRetries("logEvent") {
                val enhancedParams = Bundle(params).apply {
                    putString("timestamp", TIMESTAMP)
                    putString("user_id", userId ?: USER_ID)
                }
                firebaseAnalytics?.logEvent(eventName, enhancedParams)
                Timber.d("Analytics event logged: $eventName")
            }
        }
    }

    fun logError(tag: String, throwable: Throwable?) {
        if (!isEnabled) return

        analyticsScope.launch {
            tryWithRetries("logError") {
                val errorTag = if (tag.isEmpty()) DEFAULT_ERROR_TAG else tag
                crashlytics?.apply {
                    setCustomKey("error_timestamp", TIMESTAMP)
                    setCustomKey("error_tag", errorTag)
                    setCustomKey("error_type", throwable?.javaClass?.simpleName ?: "Unknown")
                    log("Error in $errorTag: ${throwable?.message}")
                    throwable?.let { recordException(it) }
                }
                Timber.tag(TAG).e(throwable, "Error logged: $errorTag")
            }
        }
    }

    fun logAdapterStatus(adapter: String, status: AdapterStatus) {
        if (!isEnabled) return

        analyticsScope.launch {
            tryWithRetries("logAdapterStatus") {
                val params = Bundle().apply {
                    putString("adapter_name", adapter)
                    putString("adapter_status", status.description)
                    putInt("adapter_latency", status.latency)
                    putString("timestamp", TIMESTAMP)
                    putString("user_id", userId ?: USER_ID)
                }
                firebaseAnalytics?.logEvent("admob_adapter_status", params)
                Timber.d("AdMob adapter status logged: $adapter")
            }
        }
    }

    fun setUserProperty(name: String, value: String) {
        if (!isEnabled) return

        analyticsScope.launch {
            tryWithRetries("setUserProperty") {
                firebaseAnalytics?.setUserProperty(name, value)
                crashlytics?.setCustomKey(name, value)
                Timber.d("User property set: $name = $value")
            }
        }
    }

    fun logScreenView(screenName: String, screenClass: String) {
        if (!isEnabled) return

        analyticsScope.launch {
            tryWithRetries("logScreenView") {
                val params = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                    putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
                    putString("timestamp", TIMESTAMP)
                    putString("user_id", userId ?: USER_ID)
                }
                firebaseAnalytics?.logEvent(EVENT_SCREEN_VIEW, params)
                Timber.d("Screen view logged: $screenName")
            }
        }
    }

    fun logAdEvent(adUnitId: String, eventType: AdEventType) {
        if (!isEnabled) return

        analyticsScope.launch {
            tryWithRetries("logAdEvent") {
                val params = Bundle().apply {
                    putString("ad_unit_id", adUnitId)
                    putString("event_type", eventType.name)
                    putString("timestamp", TIMESTAMP)
                    putString("user_id", userId ?: USER_ID)
                }
                firebaseAnalytics?.logEvent(EVENT_AD, params)
                Timber.d("Ad event logged: $eventType for unit $adUnitId")
            }
        }
    }

    private fun logAllAdEvents(adUnitId: String) {
        AdEventType.entries.forEach { eventType -> // Reemplazamos values() por entries
            logAdEvent(adUnitId, eventType)
        }
    }

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
                Timber.tag(TAG).w(e, "$operationName failed, retrying ($attempts/$MAX_RETRY_ATTEMPTS)")
                delay(1000L * attempts)
            }
        }
    }

    private fun handleAnalyticsError(message: String, error: Exception) {
        Timber.tag(TAG).e(error, message)
        crashlytics?.apply {
            setCustomKey("analytics_error_time", TIMESTAMP)
            setCustomKey("analytics_error_type", error.javaClass.simpleName)
            recordException(error)
        }
    }

    private fun handleInitializationError(error: Exception) {
        Timber.tag(TAG).e(error, "Analytics initialization failed")
        crashlytics?.apply {
            setCustomKey("init_error_time", TIMESTAMP)
            setCustomKey("init_error_type", error.javaClass.simpleName)
            recordException(error)
        }
    }

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

enum class AdEventType {
    LOADED,
    FAILED_TO_LOAD,
    IMPRESSION,
    CLICK,
    COMPLETION,
    INTERACTION,
    CONVERSION,
    CLOSED,
    LEFT_APPLICATION,
    SKIPPED,
    REWARDED,
    EXPANDED,
    COLLAPSED,
    VIDEO_STARTED,
    VIDEO_COMPLETED,
    VIDEO_PROGRESS,
    AUDIO_STARTED,
    AUDIO_COMPLETED,
    AUDIO_MUTED,
    AUDIO_UNMUTED
}