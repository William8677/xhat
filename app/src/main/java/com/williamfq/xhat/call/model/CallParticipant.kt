/*
 * Updated: 2025-01-27 03:50:56
 * Author: William8677
 */

package com.williamfq.xhat.call.model

data class CallParticipant(
    val id: String,
    val name: String,
    val isMuted: Boolean = false,
    val hasVideo: Boolean = true,
    val isScreenSharing: Boolean = false
) {
    fun toAnalytics(): Map<String, Any> {
        return mapOf(
            "participant_id" to id,
            "is_muted" to isMuted,
            "has_video" to hasVideo,
            "is_screen_sharing" to isScreenSharing
        )
    }
}