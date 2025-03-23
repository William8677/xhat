// SubtitleEvent.kt
package com.williamfq.xhat.service.events

data class SubtitleEvent(
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)