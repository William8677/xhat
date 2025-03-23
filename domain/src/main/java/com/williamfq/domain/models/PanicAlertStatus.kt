// domain/src/main/java/com/williamfq/domain/models/PanicAlertStatus.kt
package com.williamfq.domain.models

/**
 * Estado de una alerta de pánico.
 */
enum class PanicAlertStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    ERROR,
    CANCELLED,
    DELETED
}
