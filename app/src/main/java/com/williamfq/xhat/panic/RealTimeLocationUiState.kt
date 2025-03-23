package com.williamfq.xhat.panic

import com.williamfq.domain.models.Location

data class RealTimeLocationUiState(
    val isSharing: Boolean = false,
    val currentLocation: Location? = null,
    val lastUpdateTime: Long = 0L,
    val sharingStartTime: Long = 0L,
    val sharingDuration: Long = 0L,
    val selectedContacts: List<String> = emptyList(),
    val error: String? = null
)