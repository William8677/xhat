/*
 * Updated: 2025-02-10 01:55:20
 * Author: William8677
 */
package com.williamfq.xhat.core.models

import com.williamfq.domain.model.MediaType
import java.time.LocalDateTime

enum class UserBehavior {
    VIEWER,
    CREATOR,
    COMMENTER,
    SHARER,
    BUYER,
    SUBSCRIBER,
    PREMIUM,
    VIP,
    INFLUENCER,
    FREQUENT_CHATTER,
    STORY_CREATOR,
    SHOPPER,
    GAMER,
    TRAVELER,
    MUSIC_LOVER,
    ADVERTISER,
    DEVELOPER
}

enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    UNSPECIFIED,
    ALL
}

enum class LocationType {
    CITY,
    STATE,
    COUNTRY,
    CONTINENT,
    WORLDWIDE,
    RADIUS
}

data class AgeRange(
    val minAge: Int,
    val maxAge: Int
)

data class LocationTarget(
    val type: LocationType,
    val values: Set<String>,
    val lat: Double? = null,
    val lng: Double? = null,
    val radius: Double? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val continent: String? = null
)

data class AdTarget(
    val ageRange: AgeRange,
    val gender: Set<Gender>,
    val location: LocationTarget,
    val languages: Set<String>,
    val interests: Set<String>,
    val userBehavior: Set<UserBehavior>
)

data class AdMetrics(
    val impressions: Int = 0,
    val clicks: Int = 0,
    val engagement: Double = 0.0,
    val spend: Double = 0.0,
    val reach: Int = 0,
    val completionRate: Double = 0.0,
    val interactionRate: Double = 0.0,
    val conversionRate: Double = 0.0,
    val viewTime: Long = 0,
    val shareCount: Int = 0,
    val reactionCount: Int = 0,
    val revenue: Double = 0.0,
    val lastShown: LocalDateTime? = null
) {
    // Funciones añadidas para el cálculo de métricas diarias
    fun getDailyImpressions(): Int {
        val today = LocalDateTime.now().toLocalDate()
        return if (lastShown?.toLocalDate() == today) impressions else 0
    }

    fun getDailySpend(): Double {
        val today = LocalDateTime.now().toLocalDate()
        return if (lastShown?.toLocalDate() == today) spend else 0.0
    }
}

data class AdBudget(
    val dailyBudget: Double,
    val totalBudget: Double,
    val costPerImpression: Double,
    val costPerClick: Double,
    val currency: String = "USD"
)

data class TimeRange(
    val startHour: Int,
    val endHour: Int
)

data class AdFrequency(
    val maxImpressionPerUser: Int,
    val intervalBetweenShows: Int
)

data class AdSchedule(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val showTimes: List<TimeRange>,
    val frequency: AdFrequency
)

enum class AdStatus {
    PENDING_REVIEW,
    ACTIVE,
    PAUSED,
    COMPLETED,
    REJECTED
}

data class Advertisement(
    val id: String,
    val advertiserId: String,
    val title: String,
    val description: String,
    val content: String,
    val mediaUrl: String?,
    val mediaType: MediaType,
    val targetingOptions: AdTarget,
    val budget: AdBudget,
    val schedule: AdSchedule,
    val status: AdStatus = AdStatus.PENDING_REVIEW,
    val metrics: AdMetrics = AdMetrics(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)