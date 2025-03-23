// data/src/main/java/com/williamfq/data/entities/PanicAlertStatusEntity.kt
package com.williamfq.data.entities

/**
 * Estado de una alerta de pánico en la base de datos.
 */
enum class PanicAlertStatusEntity {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    ERROR,
    CANCELLED,
    DELETED,

}
