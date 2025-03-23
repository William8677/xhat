package com.williamfq.xhat.utils.metrics

import android.content.Context
import com.williamfq.domain.model.ReactionType
import com.williamfq.xhat.utils.analytics.Analytics
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface StoryMetricsTracker {
    suspend fun initializeTracking(userId: String)
    suspend fun trackStoryView(storyId: Int, viewDuration: Long)
    suspend fun trackEngagement(storyId: Int, engagementType: EngagementType)
    suspend fun trackCompletion(storyId: Int)
    suspend fun generateReport(storyId: Int): StoryMetricsReport
}

@Singleton
class StoryMetricsTrackerImpl @Inject constructor(
    private val context: Context,
    private val analytics: Analytics
) : StoryMetricsTracker {
    private val mutex = Mutex()
    private val storyMetrics = mutableMapOf<Int, StoryMetricsReport>()
    private lateinit var userId: String

    override suspend fun initializeTracking(userId: String) {
        this.userId = userId
    }

    override suspend fun trackStoryView(storyId: Int, viewDuration: Long) {
        mutex.withLock {
            val report = storyMetrics[storyId] ?: StoryMetricsReport(
                viewCount = 0,
                uniqueViewers = 0,
                averageViewDuration = 0.0,
                completionRate = 0.0,
                engagementRate = 0.0,
                topReactions = emptyMap(),
                viewerRetention = emptyList(),
                peakViewingTimes = emptyList()
            )
            val newViewCount = report.viewCount + 1
            val newAverageViewDuration = (report.averageViewDuration * report.viewCount + viewDuration) / newViewCount
            storyMetrics[storyId] = report.copy(
                viewCount = newViewCount,
                averageViewDuration = newAverageViewDuration
            )
        }
    }

    override suspend fun trackEngagement(storyId: Int, engagementType: EngagementType) {
        mutex.withLock {
            val report = storyMetrics[storyId] ?: StoryMetricsReport(
                viewCount = 0,
                uniqueViewers = 0,
                averageViewDuration = 0.0,
                completionRate = 0.0,
                engagementRate = 0.0,
                topReactions = emptyMap(),
                viewerRetention = emptyList(),
                peakViewingTimes = emptyList()
            )
            storyMetrics[storyId] = report.copy(
                engagementRate = report.engagementRate + 1.0
            )
        }
    }

    override suspend fun trackCompletion(storyId: Int) {
        mutex.withLock {
            val report = storyMetrics[storyId] ?: StoryMetricsReport(
                viewCount = 0,
                uniqueViewers = 0,
                averageViewDuration = 0.0,
                completionRate = 0.0,
                engagementRate = 0.0,
                topReactions = emptyMap(),
                viewerRetention = emptyList(),
                peakViewingTimes = emptyList()
            )
            storyMetrics[storyId] = report.copy(completionRate = 1.0)
        }
    }

    override suspend fun generateReport(storyId: Int): StoryMetricsReport {
        return mutex.withLock {
            storyMetrics[storyId] ?: StoryMetricsReport(
                viewCount = 0,
                uniqueViewers = 0,
                averageViewDuration = 0.0,
                completionRate = 0.0,
                engagementRate = 0.0,
                topReactions = emptyMap(),
                viewerRetention = emptyList(),
                peakViewingTimes = emptyList()
            )
        }
    }
}

enum class EngagementType {
    VIEW,
    REACTION,
    COMMENT,
    SHARE,
    SAVE,
    POLL_VOTE,
    SWIPE_UP,
    AR_INTERACTION,
    REPORT,
    REPLY,
    QUIZ_ANSWER,
    LINK_CLICK,
    CUSTOM_ACTION
}

data class StoryMetricsReport(
    val viewCount: Int,
    val uniqueViewers: Int,
    val averageViewDuration: Double,
    val completionRate: Double,
    val engagementRate: Double,
    val topReactions: Map<ReactionType, Int>,
    val viewerRetention: List<Double>,
    val peakViewingTimes: List<Long>
)