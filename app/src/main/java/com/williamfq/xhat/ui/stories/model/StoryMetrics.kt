package com.williamfq.xhat.ui.stories.model

data class StoryMetrics(
    val viewCount: Int = 0,
    val reactionCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val completionRate: Float = 0f,
    val averageViewDuration: Long = 0L,
    val engagementRate: Float = 0f,
    val lastViewed: Long = 0L,
    val uniqueViewers: Set<String> = emptySet(),
    val completionStatus: CompletionStatus = CompletionStatus.NOT_VIEWED,
    val interactionsByType: Map<InteractionType, Int> = emptyMap()
) {
    val totalInteractions: Int
        get() = reactionCount + commentCount + shareCount

    val isViewed: Boolean
        get() = viewCount > 0

    fun calculateEngagementRate(totalPossibleInteractions: Int): Float {
        return if (totalPossibleInteractions > 0) {
            totalInteractions.toFloat() / totalPossibleInteractions
        } else 0f
    }

    companion object {
        const val COMPLETE_VIEW_THRESHOLD = 0.95f
    }
}

enum class CompletionStatus {
    VIEWING,
    COMPLETE,
    PARTIAL,
    SKIPPED,
    ERROR,
    COMPLETED,
    NOT_VIEWED
}