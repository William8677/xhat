package com.williamfq.xhat.utils

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class CrashReporter @Inject constructor() {
    private lateinit var crashlytics: FirebaseCrashlytics
    private var isInitialized = false
    private var isReporting = false

    companion object {
        private const val TAG = "CrashReporter"
        private const val USER_ID = "William8677"
        private const val TIMESTAMP = "2025-02-21 20:00:03"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val REPORTING_TIMEOUT = 5000L // 5 segundos
    }

    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            crashlytics = FirebaseCrashlytics.getInstance()
            setupCrashlytics()
            isInitialized = true
            Timber.d("CrashReporter inicializado correctamente")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error al inicializar CrashReporter")
            handleInitializationError(e)
        }
    }

    private fun setupCrashlytics() {
        crashlytics.apply {
            setCustomKey("user_id", USER_ID)
            setCustomKey("timestamp", TIMESTAMP)
            setCustomKey("initialization_time", System.currentTimeMillis())
            setUserId(USER_ID)
            isCrashlyticsCollectionEnabled = true
        }
    }

    private fun handleInitializationError(error: Exception) {
        Timber.tag(TAG).e(error, "Fallback: usando logging básico debido a error de inicialización")
        // Implementar logging básico como fallback
        Timber.tag(TAG).e("Error details: ${error.message}")
        error.stackTrace.forEach { element ->
            Timber.tag(TAG).e("    at $element")
        }
    }

    @Synchronized
    fun reportException(exception: Exception) {
        if (isReporting) {
            Timber.tag(TAG).w("Ya se está reportando una excepción, evitando bucle")
            return
        }

        isReporting = true
        try {
            if (!isInitialized) {
                Timber.tag(TAG).e("CrashReporter no inicializado")
                return
            }

            crashlytics.apply {
                setCustomKey("last_action", "custom_exception")
                setCustomKey("exception_time", System.currentTimeMillis())
                setCustomKey("exception_class", exception.javaClass.name)
                setCustomKey("exception_message", exception.message ?: "No message")
                setCustomKey("report_timestamp", TIMESTAMP)
                recordException(exception)
            }

            // Log local para debugging
            Timber.tag(TAG).e(exception, "Excepción reportada: ${exception.message}")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error al reportar excepción")
            fallbackExceptionLogging(exception)
        } finally {
            isReporting = false
        }
    }

    private fun fallbackExceptionLogging(exception: Exception) {
        try {
            Timber.tag(TAG).e("FALLBACK LOG - Exception details:")
            Timber.tag(TAG).e("Time: $TIMESTAMP")
            Timber.tag(TAG).e("User: $USER_ID")
            Timber.tag(TAG).e("Exception: ${exception.javaClass.name}")
            Timber.tag(TAG).e("Message: ${exception.message}")
            Timber.tag(TAG).e("Stack trace:")
            exception.stackTrace.forEach { element ->
                Timber.tag(TAG).e("    at $element")
            }
        } catch (e: Exception) {
            // Último recurso - log básico
            Timber.tag(TAG).e("Critical error in fallback logging: ${e.message}")
        }
    }

    fun logError(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (!isInitialized) {
            Timber.tag(TAG).e("CrashReporter no inicializado")
            return
        }

        try {
            crashlytics.apply {
                setCustomKey("error_priority", priority)
                setCustomKey("error_tag", tag ?: "NO_TAG")
                setCustomKey("error_time", TIMESTAMP)
                log("$priority/$tag: $message")
                throwable?.let { recordException(it) }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error al registrar error")
            fallbackErrorLogging(priority, tag, message, throwable)
        }
    }

    private fun fallbackErrorLogging(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        Timber.tag(TAG).e("FALLBACK ERROR LOG:")
        Timber.tag(TAG).e("Priority: $priority")
        Timber.tag(TAG).e("Tag: $tag")
        Timber.tag(TAG).e("Message: $message")
        throwable?.let {
            Timber.tag(TAG).e("Throwable: ${it.message}")
            it.stackTrace.forEach { element ->
                Timber.tag(TAG).e("    at $element")
            }
        }
    }

    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        if (!isInitialized) {
            Timber.tag(TAG).e("CrashReporter no inicializado")
            return
        }

        try {
            crashlytics.apply {
                setCustomKey("event_name", eventName)
                setCustomKey("event_time", TIMESTAMP)
                params.forEach { (key, value) ->
                    setCustomKey(key, value)
                }
                log("Event: $eventName, Params: $params")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error al registrar evento")
            fallbackEventLogging(eventName, params)
        }
    }

    private fun fallbackEventLogging(eventName: String, params: Map<String, String>) {
        Timber.tag(TAG).i("FALLBACK EVENT LOG:")
        Timber.tag(TAG).i("Event: $eventName")
        Timber.tag(TAG).i("Time: $TIMESTAMP")
        Timber.tag(TAG).i("Params:")
        params.forEach { (key, value) ->
            Timber.tag(TAG).i("    $key: $value")
        }
    }

    fun setUserIdentifier(userId: String) {
        if (!isInitialized) {
            Timber.tag(TAG).e("CrashReporter no inicializado")
            return
        }

        try {
            crashlytics.setUserId(userId)
            crashlytics.setCustomKey("custom_user_id", userId)
            crashlytics.setCustomKey("id_set_time", TIMESTAMP)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error al establecer identificador de usuario")
        }
    }

    fun enableCrashReporting(enable: Boolean) {
        if (!isInitialized) {
            Timber.tag(TAG).e("CrashReporter no inicializado")
            return
        }

        try {
            crashlytics.setCrashlyticsCollectionEnabled(enable)
            crashlytics.setCustomKey("crash_reporting_enabled", enable)
            crashlytics.setCustomKey("reporting_status_change_time", TIMESTAMP)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error al configurar reporte de crashes")
        }
    }
}