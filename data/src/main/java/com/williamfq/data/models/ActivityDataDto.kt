// data/src/main/java/com/williamfq/xhat/data/models/ActivityDataDto.kt

package com.williamfq.data.models

import com.google.gson.JsonElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.williamfq.domain.models.ActivityData
import com.williamfq.domain.models.Activity

/**
 * DTO para serialización de datos de actividad.
 * @author William8677
 * @since 2025-01-05 06:09:57
 */
@Serializable
data class ActivityDataDto(
    @SerialName("user_id")
    val userId: String,
    val activities: List<ActivityDto>,
    val stats: Map<String, Int>,
    val period: String,
    val metadata: Map<String, JsonElement>? = null
) {
    fun toDomain(): ActivityData = ActivityData(
        userId = userId,
        activities = activities.map { it.toDomain() },
        stats = stats,
        period = period,
        metadata = metadata?.mapValues { (_, value) -> value.toString() }
    )
}

@Serializable
data class ActivityDto(
    val type: String,
    val count: Int,
    @SerialName("created_at")
    val createdAt: Long,
    val metadata: Map<String, kotlinx.serialization.json.JsonElement>? = null
) {
    fun toDomain(): Activity = Activity(
        type = type,
        count = count,
        createdAt = createdAt,
        metadata = metadata?.mapValues { (_, value) -> value.toString() }
    )
}