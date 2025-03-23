package com.williamfq.domain.models

data class PanicAlert(
    val id: Long = 0,
    val message: String,
    val userId: String,
    val location: Location?,
    val timestamp: Long = System.currentTimeMillis(),
    val status: PanicAlertStatus = PanicAlertStatus.SENDING,
    val priority: AlertPriority = AlertPriority.HIGH
)


enum class AlertPriority {
    LOW,
    MEDIUM,
    HIGH
}