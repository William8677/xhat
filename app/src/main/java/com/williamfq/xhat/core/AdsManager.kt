package com.williamfq.xhat.core

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.williamfq.domain.model.MediaType
import com.williamfq.domain.models.Location
import com.williamfq.xhat.core.models.AdBudget
import com.williamfq.xhat.core.models.AdFrequency
import com.williamfq.xhat.core.models.AdMetrics
import com.williamfq.xhat.core.models.AdSchedule
import com.williamfq.xhat.core.models.AdStatus
import com.williamfq.xhat.core.models.AdTarget
import com.williamfq.xhat.core.models.Advertisement
import com.williamfq.xhat.core.models.AgeRange
import com.williamfq.xhat.core.models.Gender
import com.williamfq.xhat.core.models.LocationTarget
import com.williamfq.xhat.core.models.LocationType
import com.williamfq.xhat.core.models.TimeRange
import com.williamfq.xhat.core.models.UserBehavior
import com.williamfq.xhat.domain.model.UserProfile
import com.williamfq.xhat.utils.analytics.AdEventType
import com.williamfq.xhat.utils.analytics.AnalyticsManager
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.user.UserProfileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileManager: UserProfileManager,
    private val analyticsManager: AnalyticsManager,
    private val logger: LoggerInterface
) {
    private val _currentAd = MutableStateFlow<Advertisement?>(null)
    val currentAd: StateFlow<Advertisement?> = _currentAd.asStateFlow()

    private val _activeAds = MutableStateFlow<List<Advertisement>>(emptyList())
    val activeAds: StateFlow<List<Advertisement>> = _activeAds.asStateFlow()

    private val _adMetrics = MutableStateFlow<Map<String, AdMetrics>>(emptyMap())
    val adMetrics: StateFlow<Map<String, AdMetrics>> = _adMetrics.asStateFlow()

    private val _adQueue = MutableStateFlow<List<Advertisement>>(emptyList())
    private val adRequestBuilder = AdRequest.Builder()

    companion object {
        private const val MIN_HOURS_BETWEEN_ADS = 1
        private const val MAX_DAILY_ADS = 8
        private const val DEFAULT_ENGAGEMENT_SCORE = 1.0
        private const val TARGETING_MATCH_SCORE = 50.0
        private const val ENGAGEMENT_MULTIPLIER = 10.0
        private const val BUDGET_SCORE_MULTIPLIER = 20.0
        private const val PRIME_TIME_SCORE = 15.0
        private const val BEHAVIOR_MATCH_MULTIPLIER = 25.0
        private val PRIME_TIME_RANGES = listOf(
            LocalTime.of(8, 0)..LocalTime.of(10, 0),
            LocalTime.of(12, 0)..LocalTime.of(14, 0),
            LocalTime.of(18, 0)..LocalTime.of(22, 0)
        )
    }

    init {
        initializeAds()
        startAdScheduler()
        setupAdMonitoring()
    }

    private fun convertGender(userGender: Gender): Gender {
        return when (userGender) {
            Gender.MALE -> Gender.MALE
            Gender.FEMALE -> Gender.FEMALE
            Gender.OTHER -> Gender.OTHER
            Gender.UNSPECIFIED -> Gender.UNSPECIFIED
            Gender.ALL -> Gender.ALL
        }
    }

    private fun convertUserBehavior(behavior: UserBehavior): UserBehavior {
        return when (behavior) {
            UserBehavior.FREQUENT_CHATTER -> UserBehavior.FREQUENT_CHATTER
            UserBehavior.STORY_CREATOR -> UserBehavior.STORY_CREATOR
            UserBehavior.SHOPPER -> UserBehavior.SHOPPER
            UserBehavior.GAMER -> UserBehavior.GAMER
            UserBehavior.TRAVELER -> UserBehavior.TRAVELER
            UserBehavior.MUSIC_LOVER -> UserBehavior.MUSIC_LOVER
            else -> UserBehavior.VIEWER
        }
    }

    private fun initializeAds() {
        try {
            MobileAds.initialize(context) { initializationStatus ->
                CoroutineScope(Dispatchers.Main).launch {
                    initializationStatus.adapterStatusMap.forEach { (adapter, status) ->
                        analyticsManager.logAdapterStatus(adapter, status)
                    }
                    logger.logEvent("AdsManager", "MobileAds initialized successfully", LogLevel.INFO)
                    analyticsManager.logAdEvent("system", AdEventType.LOADED)
                }
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                logger.logEvent("AdsManager", "Error initializing MobileAds", LogLevel.ERROR, e)
                analyticsManager.logAdEvent("system", AdEventType.FAILED_TO_LOAD)
                analyticsManager.logError("AdsManager", e)
            }
        }
    }

    private fun canShowAd(ad: Advertisement): Boolean {
        val now = LocalDateTime.now()
        val currentHour = now.hour
        val inScheduledTime = ad.schedule.showTimes.any { timeRange ->
            currentHour in timeRange.startHour..timeRange.endHour
        }
        val withinFrequencyLimits = isWithinFrequencyLimits(ad)
        val dailyImpressions = _adMetrics.value[ad.id]?.getDailyImpressions() ?: 0
        val withinDailyLimit = dailyImpressions < MAX_DAILY_ADS
        val dailySpend = _adMetrics.value[ad.id]?.getDailySpend() ?: 0.0
        val withinDailyBudget = dailySpend < ad.budget.dailyBudget
        return inScheduledTime &&
                withinFrequencyLimits &&
                withinDailyLimit &&
                withinDailyBudget &&
                ad.status == AdStatus.ACTIVE
    }

    private fun setupAdMonitoring() {
        CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                try {
                    monitorAdPerformance()
                    optimizeAdDelivery()
                    updateGlobalMetrics()
                    delay(300000)
                } catch (e: Exception) {
                    logger.logEvent("AdsManager", "Error in ad monitoring", LogLevel.ERROR, e)
                }
            }
        }
    }

    private suspend fun monitorAdPerformance() {
        _activeAds.value.forEach { ad ->
            val performance = calculateAdPerformance(ad.metrics)
            when {
                performance < 0.3 -> pauseAd(ad.id)
                performance > 0.8 -> boostAd(ad.id)
            }
        }
    }

    private fun calculateAdPerformance(metrics: AdMetrics): Double {
        return (metrics.completionRate * 0.3 +
                metrics.conversionRate * 0.3 +
                metrics.interactionRate * 0.2 +
                (metrics.engagement / 10.0) * 0.2)
    }

    private fun calculateEngagementScore(metrics: AdMetrics): Double {
        return (metrics.clicks * 0.2 +
                metrics.completionRate * 0.2 +
                metrics.interactionRate * 0.2 +
                metrics.conversionRate * 0.2 +
                (metrics.viewTime / 60000.0) * 0.1 +
                (metrics.shareCount * 0.05) +
                (metrics.reactionCount * 0.05) +
                (metrics.revenue / 100.0) * 0.1)
    }

    private fun optimizeAdDelivery() {
        val totalBudget = _activeAds.value.sumOf { it.budget.totalBudget }
        val remainingBudget = _activeAds.value.sumOf { it.budget.totalBudget - it.metrics.spend }
        if (remainingBudget < totalBudget * 0.2) {
            _activeAds.value.forEach { ad ->
                if (ad.metrics.engagement < DEFAULT_ENGAGEMENT_SCORE) {
                    pauseAd(ad.id)
                }
            }
        }
    }

    private suspend fun updateGlobalMetrics() {
        _activeAds.value.forEach { ad ->
            val metrics = _adMetrics.value[ad.id]
            if (metrics != null) {
                updateAdMetrics(ad.id, metrics)
            }
        }
    }

    private fun startAdScheduler() {
        CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                try {
                    updateAdQueue()
                    processNextAd()
                    delay(MIN_HOURS_BETWEEN_ADS * 3600000L)
                } catch (e: Exception) {
                    logger.logEvent("AdsManager", "Error in ad scheduler", LogLevel.ERROR, e)
                }
            }
        }
    }

    private suspend fun updateAdQueue() {
        val userProfile = userProfileManager.getUserProfile()
        val sortedAds = _activeAds.value
            .filter { ad -> isAdEligible(ad) }
            .sortedByDescending { ad -> calculateRelevanceScore(ad, userProfile) }
        _adQueue.value = sortedAds
    }

    private suspend fun processNextAd() {
        _adQueue.value.firstOrNull()?.let { ad ->
            if (canShowAd(ad)) {
                _currentAd.value = ad
                trackAdImpression(ad)
            }
        }
    }

    private fun isAdEligible(ad: Advertisement): Boolean {
        val now = LocalDateTime.now()
        return ad.status == AdStatus.ACTIVE &&
                now.isAfter(ad.schedule.startDate) &&
                now.isBefore(ad.schedule.endDate) &&
                ad.metrics.spend < ad.budget.totalBudget &&
                isAdPerformingWell(ad)
    }

    private fun isAdPerformingWell(ad: Advertisement): Boolean {
        return ad.metrics.engagement >= DEFAULT_ENGAGEMENT_SCORE &&
                ad.metrics.conversionRate >= 0.001 &&
                ad.metrics.completionRate >= 0.3
    }

    private fun calculateRelevanceScore(ad: Advertisement, userProfile: UserProfile): Double {
        var score = 0.0
        if (matchesTargeting(ad.targetingOptions, userProfile)) {
            score += TARGETING_MATCH_SCORE
        }
        score += calculateEngagementScore(ad.metrics) * ENGAGEMENT_MULTIPLIER
        val budgetRemaining = ad.budget.totalBudget - ad.metrics.spend
        score += (budgetRemaining / ad.budget.dailyBudget) * BUDGET_SCORE_MULTIPLIER
        if (isInPrimeTime(LocalDateTime.now())) {
            score += PRIME_TIME_SCORE
        }
        return score
    }

    private fun matchesTargeting(target: AdTarget, userProfile: UserProfile): Boolean {
        return matchesAge(target.ageRange, userProfile.age) &&
                matchesGender(target.gender, convertGender(userProfile.gender)) &&
                matchesLocation(target.location, userProfile.location) &&
                matchesLanguages(target.languages, userProfile.languages) &&
                matchesUserBehavior(target.userBehavior, userProfile.behaviors.map { convertUserBehavior(it) }.toSet())
    }

    private fun matchesAge(targetRange: AgeRange, userAge: Int): Boolean {
        return userAge in targetRange.minAge..targetRange.maxAge
    }

    private fun matchesGender(targetGenders: Set<Gender>, userGender: Gender): Boolean {
        return targetGenders.isEmpty() ||
                targetGenders.contains(Gender.ALL) ||
                targetGenders.contains(userGender)
    }

    private fun matchesLocation(target: LocationTarget, userLocation: Location): Boolean {
        return when (target.type) {
            LocationType.RADIUS -> checkRadiusLocation(target, userLocation)
            LocationType.CITY -> target.city?.equals(userLocation.city, ignoreCase = true) ?: false
            LocationType.STATE -> target.state?.equals(userLocation.state, ignoreCase = true) ?: false
            LocationType.COUNTRY -> target.country?.equals(userLocation.country, ignoreCase = true) ?: false
            LocationType.CONTINENT -> target.continent?.equals(userLocation.continent, ignoreCase = true) ?: false
            LocationType.WORLDWIDE -> true
        }
    }

    private fun matchesLanguages(targetLanguages: Set<String>, userLanguages: Set<String>): Boolean {
        return targetLanguages.isEmpty() || targetLanguages.intersect(userLanguages).isNotEmpty()
    }

    private fun matchesUserBehavior(targetBehaviors: Set<UserBehavior>, userBehaviors: Set<UserBehavior>): Boolean {
        return targetBehaviors.isEmpty() || targetBehaviors.intersect(userBehaviors).isNotEmpty()
    }

    private fun checkRadiusLocation(target: LocationTarget, userLocation: Location): Boolean {
        if (target.lat == null || target.lng == null || target.radius == null) return false
        val distance = calculateDistance(
            Location(target.lat, target.lng),
            userLocation
        )
        return distance <= target.radius
    }

    private fun calculateDistance(location1: Location, location2: Location): Double {
        val R = 6371.0
        val lat1 = Math.toRadians(location1.latitude)
        val lat2 = Math.toRadians(location2.latitude)
        val dLat = Math.toRadians(location2.latitude - location1.latitude)
        val dLon = Math.toRadians(location2.longitude - location1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun isInPrimeTime(time: LocalDateTime): Boolean {
        val currentTime = time.toLocalTime()
        return PRIME_TIME_RANGES.any { range -> currentTime in range }
    }

    private fun isWithinFrequencyLimits(ad: Advertisement): Boolean {
        val metrics = _adMetrics.value[ad.id] ?: return true
        val lastShown = metrics.lastShown ?: return true
        val now = LocalDateTime.now()
        val minutesSinceLastShow = java.time.Duration.between(lastShown, now).toMinutes()
        return minutesSinceLastShow >= ad.schedule.frequency.intervalBetweenShows
    }

    private fun pauseAd(adId: String) {
        updateAdStatus(adId, AdStatus.PAUSED)
        analyticsManager.logAdEvent(adId, AdEventType.CLOSED)
    }

    private suspend fun boostAd(adId: String) {
        logger.logEvent("AdsManager", "Boosting ad: $adId", LogLevel.INFO)
    }

    private fun updateAdStatus(adId: String, status: AdStatus) {
        _activeAds.value = _activeAds.value.map { ad ->
            if (ad.id == adId) ad.copy(status = status) else ad
        }
    }

    private suspend fun trackAdImpression(ad: Advertisement) {
        val currentMetrics = ad.metrics.copy(
            impressions = ad.metrics.impressions + 1,
            lastShown = LocalDateTime.now()
        )
        updateAdMetrics(ad.id, currentMetrics)
        analyticsManager.logAdEvent(ad.id, AdEventType.IMPRESSION)
    }

    private fun updateMetricsWithEngagement(adId: String, update: (AdMetrics) -> AdMetrics) {
        val currentMetrics = _adMetrics.value[adId] ?: AdMetrics()
        val updatedMetrics = update(currentMetrics)
        updateAdMetrics(adId, updatedMetrics)
    }

    private fun updateAdMetrics(adId: String, metrics: AdMetrics) {
        _adMetrics.value = _adMetrics.value + (adId to metrics)
        updateAdvertisementMetrics(adId, metrics)
    }

    private fun updateAdvertisementMetrics(adId: String, metrics: AdMetrics) {
        _activeAds.value = _activeAds.value.map { ad ->
            if (ad.id == adId) ad.copy(metrics = metrics) else ad
        }
    }

    fun createAd(
        advertiserId: String,
        title: String,
        description: String,
        content: String,
        mediaUrl: String?,
        mediaType: MediaType,
        targetingOptions: AdTarget,
        budget: AdBudget,
        schedule: AdSchedule
    ): Advertisement {
        return Advertisement(
            id = generateAdId(),
            advertiserId = advertiserId,
            title = title,
            description = description,
            content = content,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            targetingOptions = targetingOptions,
            budget = budget,
            schedule = schedule
        ).also {
            _activeAds.value += it
            analyticsManager.logAdEvent(it.id, AdEventType.LOADED)
        }
    }

    private fun generateAdId(): String = "ad_${UUID.randomUUID()}"

    fun addAd(advertisement: Advertisement) {
        _activeAds.value = _activeAds.value + advertisement
        analyticsManager.logAdEvent(advertisement.id, AdEventType.LOADED)
    }

    fun removeAd(adId: String) {
        _activeAds.value = _activeAds.value.filter { it.id != adId }
        _adMetrics.value = _adMetrics.value - adId
    }

    fun clearExpiredAds() {
        val now = LocalDateTime.now()
        _activeAds.value = _activeAds.value.filter { ad ->
            !now.isAfter(ad.schedule.endDate)
        }
    }

    fun getAdById(adId: String): Advertisement? {
        return _activeAds.value.find { it.id == adId }
    }

    fun getAdMetrics(adId: String): AdMetrics? {
        return _adMetrics.value[adId]
    }

    fun forecastAdRevenue(ad: Advertisement): Double {
        val remainingBudget = ad.budget.totalBudget - ad.metrics.spend
        return remainingBudget * (ad.metrics.conversionRate + 0.1)
    }

    fun calculateOptimalPlacementScore(ad: Advertisement, userProfile: UserProfile): Double {
        return calculateRelevanceScore(ad, userProfile) + (ad.metrics.engagement * 5)
    }

    suspend fun logAdDiagnostics(adId: String) {
        val ad = getAdById(adId)
        val metrics = getAdMetrics(adId)
        logger.logEvent("AdsManager", "Diagnostics for ad $adId: ad = $ad, metrics = $metrics", LogLevel.DEBUG)
    }
}
