package com.williamfq.xhat

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.williamfq.xhat.ads.config.AdServicesConfigManager
import com.williamfq.xhat.utils.ConnectionManager
import com.williamfq.xhat.utils.CrashReporter
import com.williamfq.xhat.utils.analytics.AnalyticsManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class XhatApplication : MultiDexApplication() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var adServicesConfigManager: AdServicesConfigManager
    @Inject
    lateinit var crashReporter: CrashReporter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var connectionManager: ConnectionManager

    companion object {
        private const val TAG = "XhatApplication"
        private const val CURRENT_TIMESTAMP = "2025-02-21 19:58:50"
        private const val CURRENT_USER = "William8677"

        @Volatile
        private lateinit var instance: XhatApplication
        private var isInitialized = false

        fun getInstance(): XhatApplication = instance
    }

    private var isHandlingException = false

    override fun onCreate() {
        super.onCreate()
        if (!isInitialized) {
            instance = this

            initializeCrashReporting()
            setupUncaughtExceptionHandler()

            applicationScope.launch(Dispatchers.IO) {
                try {
                    FirebaseApp.initializeApp(this@XhatApplication)?.let {
                        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                            PlayIntegrityAppCheckProviderFactory.getInstance()
                        )
                    } ?: run {
                        Timber.e("FirebaseApp no pudo inicializarse")
                        crashReporter.reportException(Exception("FirebaseApp no pudo inicializarse"))
                    }

                    setupStrictMode()
                    initializeComponents()

                    applicationScope.launch(Dispatchers.Main) {
                        isInitialized = true
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error durante la inicialización de la aplicación")
                    crashReporter.reportException(e)
                }
            }
        }
    }

    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                if (!isHandlingException) {
                    isHandlingException = true
                    crashReporter.reportException(Exception("Uncaught exception in thread ${thread.name}", throwable))
                    crashReporter.logEvent("fatal_crash", mapOf(
                        "thread_name" to thread.name,
                        "exception_type" to throwable.javaClass.simpleName,
                        "exception_message" to (throwable.message ?: "Unknown error"),
                        "timestamp" to CURRENT_TIMESTAMP,
                        "user" to CURRENT_USER
                    ))
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error reporting uncaught exception")
            } finally {
                isHandlingException = false
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun initializeComponents() {
        try {
            initializeTimber()
            initializeAnalytics()
            initializeNetworkMonitoring()
        } catch (e: Exception) {
            handleInitializationError(e)
        }
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .detectResourceMismatches()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .detectLeakedRegistrationObjects()
                    .detectFileUriExposure()
                    .detectContentUriWithoutPermission()
                    .detectCleartextNetwork()
                    .penaltyLog()
                    .build()
            )
            Timber.d("StrictMode configurado para desarrollo")
        }
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ReleaseTree(crashReporter))
        }
    }

    private fun initializeCrashReporting() {
        if (!BuildConfig.DEBUG) {
            crashReporter.initialize(this)
        }
    }

    private fun initializeAnalytics() {
        analyticsManager.initialize(
            enabled = !BuildConfig.DEBUG,
            userId = CURRENT_USER,
            properties = getAnalyticsProperties()
        )
    }

    private fun initializeNetworkMonitoring() {
        connectionManager.startMonitoring()
    }

    private fun handleInitializationError(error: Exception) {
        Timber.e(error, "Error en la inicialización de la aplicación")
        crashReporter.reportException(error)
        analyticsManager.logError("app_initialization_error", error)
    }

    private fun getAnalyticsProperties(): Map<String, Any> = mapOf(
        "app_version" to BuildConfig.VERSION_NAME,
        "build_type" to BuildConfig.BUILD_TYPE,
        "device_model" to Build.MODEL,
        "android_version" to Build.VERSION.SDK_INT,
        "timestamp" to CURRENT_TIMESTAMP
    )

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        handleLowMemory()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        handleTrimMemory(level)
    }

    private fun handleLowMemory() {
        Timber.w("Memoria baja detectada")
        clearMemoryCache()
        System.gc()
        analyticsManager.logEvent("low_memory_warning", Bundle())
    }

    @Suppress("DEPRECATION")
    private fun handleTrimMemory(level: Int) {
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE -> {
                Timber.d("Memoria moderada - Limpiando caches no esenciales")
                clearNonEssentialCaches()
                analyticsManager.logEvent("memory_trim_moderate", Bundle())
            }
            TRIM_MEMORY_RUNNING_LOW -> {
                Timber.w("Memoria baja - Limpiando todos los caches")
                clearAllCaches()
                analyticsManager.logEvent("memory_trim_low", Bundle())
            }
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Timber.e("Memoria crítica - Limpieza de emergencia")
                performEmergencyCleanup()
                analyticsManager.logEvent("memory_trim_critical", Bundle())
            }
        }
        checkMemoryThreshold()
    }

    private fun checkMemoryThreshold() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usageRatio = usedMemory.toDouble() / maxMemory.toDouble()
        if (usageRatio > 0.85) {
            Timber.w("Uso de memoria ($usageRatio) excede el umbral definido (0.85).")
            analyticsManager.logEvent("memory_threshold_exceeded", Bundle().apply {
                putString("usage_ratio", usageRatio.toString())
            })
        } else {
            Timber.d("Uso de memoria ($usageRatio) dentro del umbral aceptable.")
        }
    }

    private fun clearMemoryCache() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                adServicesConfigManager.clearAdCache()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing memory cache")
                crashReporter.reportException(e)
            }
        }
    }

    private fun clearNonEssentialCaches() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                adServicesConfigManager.clearNonEssentialAdCache()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing non-essential caches")
                crashReporter.reportException(e)
            }
        }
    }

    private fun clearAllCaches() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                adServicesConfigManager.clearAllAdCaches()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing all caches")
                crashReporter.reportException(e)
            }
        }
    }

    private fun performEmergencyCleanup() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                clearMemoryCache()
                clearAllCaches()
                System.gc()
            } catch (e: Exception) {
                Timber.e(e, "Error performing emergency cleanup")
                crashReporter.reportException(e)
            }
        }
    }

    private inner class DebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            return String.format("[%s:%s]", super.createStackElementTag(element), element.lineNumber)
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val timeStamp = CURRENT_TIMESTAMP
            val enhancedMessage = "[$timeStamp][User: $CURRENT_USER] $message"
            super.log(priority, tag, enhancedMessage, t)
        }
    }

    private inner class ReleaseTree(private val crashReporter: CrashReporter) : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= android.util.Log.ERROR) {
                crashReporter.logError(priority, tag, message, t)
                analyticsManager.logError(tag ?: "unknown", t)
            }
        }
    }
}