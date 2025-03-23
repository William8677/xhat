/*
 * Updated: 2025-02-20
 * Author: William8677
 */
package com.williamfq.xhat.ads.config

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.williamfq.xhat.utils.analytics.AnalyticsManager
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AdServicesConfigManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analytics: AnalyticsManager,
    private val logger: LoggerInterface
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "AdServicesConfigManager"
        private const val APP_ID = "ca-app-pub-2587938308176637~6448560139"
        private const val NATIVE_STORY_AD_UNIT_ID = "ca-app-pub-2587938308176637/4265820740"
        private const val PREFS_NAME = "ad_services_prefs"
        private const val KEY_CONSENT_STATUS = "consent_status"
        private const val KEY_TOPICS_ENABLED = "topics_enabled"
        private const val KEY_PROTECTED_AUDIENCE = "protected_audience"
        private const val AD_CACHE_DIR = "ad_cache"
        private const val NON_ESSENTIAL_CACHE_DIR = "non_essential_ad_cache"
        private const val RETENTION_PERIOD_DAYS = 30
        private const val MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024
        private const val MAX_FILE_AGE_HOURS = 24L
        private const val MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024
        private const val MAX_VIDEO_SIZE_BYTES = 20 * 1024 * 1024
        private const val MAX_AUDIO_SIZE_BYTES = 10 * 1024 * 1024
        private const val LOW_RESOLUTION_THRESHOLD = 720

        private val NON_ESSENTIAL_PATTERNS = listOf(
            "temp_", "preview_", "thumbnail_", "draft_", "backup_"
        )
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
        private val VIDEO_EXTENSIONS = setOf("mp4", "webm", "mkv", "mov")
        private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "ogg", "m4a")
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val consentInformation by lazy {
        UserMessagingPlatform.getConsentInformation(context)
    }

    private val adCacheDir: File by lazy {
        File(context.cacheDir, AD_CACHE_DIR).apply { mkdirs() }
    }

    private val nonEssentialCacheDir: File by lazy {
        File(context.cacheDir, NON_ESSENTIAL_CACHE_DIR).apply { mkdirs() }
    }

    init {
        // Llamamos a initializeAdServices desde el init para asegurar su uso
        scope.launch {
            // Usamos una actividad dummy o esperamos una actividad real desde el contexto de uso
            val dummyActivity = context as? Activity
            if (dummyActivity != null) {
                initializeAdServices(dummyActivity)
            } else {
                logger.logEvent(TAG, "No activity available for AdServices initialization in init", LogLevel.WARNING)
            }
        }
    }

    suspend fun clearAdCache() = withContext(Dispatchers.IO) {
        try {
            var bytesCleared = 0L
            adCacheDir.listFiles()?.forEach { file ->
                if (file.isFile && isFileWithinLimits(file)) {
                    bytesCleared += file.length()
                    file.delete()
                }
            }
            val params = Bundle().apply {
                putLong("bytes_cleared", bytesCleared)
            }
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Ad cache cleared: ${bytesCleared / 1024}KB freed", LogLevel.INFO)
                analytics.logEvent("ad_cache_cleared", params)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Error clearing ad cache", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
        }
    }

    suspend fun clearNonEssentialAdCache() = withContext(Dispatchers.IO) {
        try {
            var bytesCleared = 0L
            nonEssentialCacheDir.listFiles()?.forEach { file ->
                if (file.isFile && isNonEssentialFile(file)) {
                    bytesCleared += file.length()
                    file.delete()
                }
            }
            val params = Bundle().apply {
                putLong("bytes_cleared", bytesCleared)
            }
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Non-essential ad cache cleared: ${bytesCleared / 1024}KB freed", LogLevel.INFO)
                analytics.logEvent("non_essential_ad_cache_cleared", params)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Error clearing non-essential ad cache", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
        }
    }

    suspend fun clearAllAdCaches() = withContext(Dispatchers.IO) {
        try {
            var totalBytesCleared = 0L
            totalBytesCleared += clearDirectoryContents(adCacheDir)
            totalBytesCleared += clearDirectoryContents(nonEssentialCacheDir)

            val params = Bundle().apply {
                putLong("bytes_cleared", totalBytesCleared)
            }
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "All ad caches cleared: ${totalBytesCleared / 1024}KB freed", LogLevel.INFO)
                analytics.logEvent("all_ad_caches_cleared", params)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Error clearing all ad caches", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
        }
    }

    suspend fun initializeAdServices(activity: Activity) = withContext(Dispatchers.IO) {
        try {
            MobileAds.initialize(context) { initializationStatus ->
                scope.launch {
                    initializationStatus.adapterStatusMap.forEach { (adapter, status) ->
                        logger.logEvent(
                            TAG,
                            "Adapter: $adapter Status: ${status.description} (Latency: ${status.latency}ms)",
                            LogLevel.DEBUG
                        )
                        analytics.logAdapterStatus(adapter, status)
                    }
                }
            }
            prefs.edit { putString("app_id", APP_ID) } // Usar APP_ID
            logger.logEvent(TAG, "Using Native Ad Unit ID: ${getNativeStoryAdUnitId()}", LogLevel.INFO)

            setupPrivacySettings(activity)
            setupAttributionReporting()
            setupTopics()
            setupProtectedAudience()
            cleanupOldFiles()
            cleanupNonEssentialFiles() // Nueva función para usar RETENTION_PERIOD_DAYS
            validateCacheDirs()

            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Ad services initialized successfully", LogLevel.INFO)
                analytics.logEvent("ad_services_initialized", Bundle())
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Failed to initialize ad services", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
            throw e
        }
    }

    private suspend fun setupPrivacySettings(activity: Activity) = withContext(Dispatchers.IO) {
        try {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("TEST-DEVICE-ID")
                .build()

            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(debugSettings)
                .build()

            withContext(Dispatchers.Main) {
                consentInformation.requestConsentInfoUpdate(
                    activity,
                    params,
                    {
                        scope.launch {
                            prefs.edit {
                                putInt(
                                    KEY_CONSENT_STATUS,
                                    consentInformation.consentStatus
                                )
                            }
                            if (consentInformation.isConsentFormAvailable) {
                                showConsentForm(activity)
                            } else {
                                updateAdConfigBasedOnConsent(consentInformation.consentStatus)
                            }
                        }
                    },
                    { error ->
                        scope.launch {
                            logger.logEvent(TAG, "Error updating consent: ${error.message}", LogLevel.ERROR)
                            analytics.logError(TAG, Exception(error.message))
                        }
                    }
                )
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Error setting up privacy settings", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
        }
    }

    private suspend fun showConsentForm(activity: Activity) = withContext(Dispatchers.Main) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
            scope.launch {
                if (formError != null) {
                    logger.logEvent(TAG, "Consent form error: ${formError.message}", LogLevel.ERROR)
                    analytics.logError(TAG, Exception(formError.message))
                }
                updateAdConfigBasedOnConsent(consentInformation.consentStatus)
            }
        }
    }

    private suspend fun updateAdConfigBasedOnConsent(consentStatus: Int) = withContext(Dispatchers.Main) {
        val config = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(
                if (consentStatus == ConsentInformation.ConsentStatus.OBTAINED)
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
                else
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
            )
            .build()
        MobileAds.setRequestConfiguration(config)
    }

    private suspend fun setupAttributionReporting() = withContext(Dispatchers.IO) {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            val params = Bundle().apply {
                putString("ad_id", adInfo.id)
                putBoolean("tracking_enabled", !adInfo.isLimitAdTrackingEnabled)
            }
            withContext(Dispatchers.Main) {
                analytics.logEvent("attribution_configured", params)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Error setting up attribution reporting", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
        }
    }

    private suspend fun setupTopics() = withContext(Dispatchers.IO) {
        try {
            val topicsEnabled = prefs.getBoolean(KEY_TOPICS_ENABLED, true)
            if (topicsEnabled) {
                val topics = listOf("chat", "social", "messaging")
                prefs.edit { putStringSet("topics", topics.toSet()) }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Error setting up topics", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
        }
    }

    private suspend fun setupProtectedAudience() = withContext(Dispatchers.IO) {
        try {
            if (prefs.getBoolean(KEY_PROTECTED_AUDIENCE, true)) {
                val segments = listOf("active_users", "chat_enthusiasts")
                prefs.edit { putStringSet("segments", segments.toSet()) }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                logger.logEvent(TAG, "Error setting up protected audience", LogLevel.ERROR, e)
                analytics.logError(TAG, e)
            }
        }
    }

    private suspend fun cleanupOldFiles() = withContext(Dispatchers.IO) {
        try {
            val maxAgeMillis = MAX_FILE_AGE_HOURS * 60 * 60 * 1000L
            val now = System.currentTimeMillis()

            fun cleanDirectory(dir: File): Long {
                var cleared = 0L
                dir.listFiles()?.forEach { file ->
                    if (now - file.lastModified() > maxAgeMillis) {
                        cleared += file.length()
                        file.delete()
                    }
                }
                return cleared
            }

            val totalCleared = cleanDirectory(adCacheDir) + cleanDirectory(nonEssentialCacheDir)
            if (totalCleared > 0) {
                logger.logEvent(TAG, "Cleaned ${totalCleared / 1024}KB of old files", LogLevel.INFO)
            }
        } catch (e: Exception) {
            logger.logEvent(TAG, "Error cleaning up old files", LogLevel.ERROR, e)
            analytics.logError(TAG, e)
        }
    }

    private suspend fun cleanupNonEssentialFiles() = withContext(Dispatchers.IO) {
        try {
            val maxAgeMillis = RETENTION_PERIOD_DAYS * 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()
            nonEssentialCacheDir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > maxAgeMillis) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            logger.logEvent(TAG, "Error cleaning up non-essential files", LogLevel.ERROR, e)
            analytics.logError(TAG, e)
        }
    }

    private fun validateCacheDirs() {
        if (!adCacheDir.exists()) adCacheDir.mkdirs()
        if (!nonEssentialCacheDir.exists()) nonEssentialCacheDir.mkdirs()
    }

    private fun isNonEssentialFile(file: File): Boolean {
        return NON_ESSENTIAL_PATTERNS.any { file.name.startsWith(it) } ||
                file.length() > MAX_CACHE_SIZE_BYTES
    }

    private fun isFileWithinLimits(file: File): Boolean {
        val extension = file.extension.lowercase()
        return when {
            IMAGE_EXTENSIONS.contains(extension) -> {
                val isLowResolution = checkImageResolution(file) <= LOW_RESOLUTION_THRESHOLD
                file.length() <= MAX_IMAGE_SIZE_BYTES || isLowResolution
            }
            VIDEO_EXTENSIONS.contains(extension) -> file.length() <= MAX_VIDEO_SIZE_BYTES
            AUDIO_EXTENSIONS.contains(extension) -> file.length() <= MAX_AUDIO_SIZE_BYTES
            else -> true
        }
    }

    private fun checkImageResolution(file: File): Int {
        // Implementación simulada; en realidad, se debería leer la resolución de la imagen
        return 1080 // Valor de ejemplo
    }

    private fun clearDirectoryContents(directory: File): Long {
        var bytesCleared = 0L
        directory.listFiles()?.forEach { file ->
            val fileSize = file.length() // Uso explícito del parámetro file
            if (file.delete()) {
                bytesCleared += fileSize
            }
        }
        return bytesCleared
    }

    fun getNativeStoryAdUnitId(): String = NATIVE_STORY_AD_UNIT_ID
}