package com.williamfq.domain.model

data class UserActivityData(
    val messagesText: Int = 0,
    val messagesVoice: Int = 0,
    val storiesCount: Int = 0,
    val storyReactions: Int = 0,
    val callTimeVoice: Int = 0,    // en minutos
    val callTimeVideo: Int = 0,    // en minutos
    val topContactName: String = "",
    val topContactMessageCount: Int = 0,
    val period: String = "",       // "weekly", "monthly", "yearly"
    val lastUpdated: Long = 0      // o Timestamp
)
