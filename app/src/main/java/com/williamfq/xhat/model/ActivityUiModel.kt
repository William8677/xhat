package com.williamfq.xhat.model

import android.content.Context
import android.text.format.DateUtils
import com.williamfq.domain.models.ActivityData
import com.williamfq.domain.models.Activity

/**
 * Modelo de UI para datos de actividad.
 * @author William8677
 * @since 2025-01-05 05:50:39
 */
data class ActivityUiModel(
    val userId: String,
    val activities: List<ActivityItemUiModel>,
    val stats: Map<String, Int>,
    val period: String
)

data class ActivityItemUiModel(
    val type: String,
    val count: Int,
    val createdAtFormatted: String
)

/**
 * Extensiones de mapeo para la UI
 */
fun ActivityData.toUiModel(context: Context): ActivityUiModel = ActivityUiModel(
    userId = userId,
    activities = activities.map { it.toUiModel(context) },
    stats = stats,
    period = period
)

fun Activity.toUiModel(context: Context): ActivityItemUiModel = ActivityItemUiModel(
    type = type,
    count = count,
    createdAtFormatted = DateUtils.formatDateTime(context, createdAt, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME)
)