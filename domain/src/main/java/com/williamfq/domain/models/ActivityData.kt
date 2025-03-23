// domain/src/main/java/com/williamfq/xhat/domain/models/ActivityData.kt

package com.williamfq.domain.models

/**
 * Modelo de dominio para datos de actividad.
 * @author William8677
 * @since 2025-01-05 05:50:39
 */
data class ActivityData(
    val userId: String,
    val activities: List<Activity>,
    val stats: Map<String, Int>,
    val period: String,
    val metadata: Map<String, Any>? = null
)

data class Activity(
    val type: String,
    val count: Int,
    val createdAt: Long,
    val metadata: Map<String, Any>? = null
)
