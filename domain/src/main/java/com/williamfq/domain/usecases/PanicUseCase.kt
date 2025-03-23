// domain/src/main/java/com/williamfq/domain/usecases/PanicUseCase.kt
package com.williamfq.domain.usecases

import com.williamfq.domain.models.PanicAlert
import com.williamfq.domain.models.PanicAlertStatus

/**
 * Interfaz para la lógica de casos de uso
 * relacionados a PanicAlert. Retorna `Result<T>` si deseas
 * manejar éxito/error sin lanzar excepciones.
 */
interface PanicUseCase {
    suspend fun sendPanicAlert(panicAlert: PanicAlert): Result<Unit>
    suspend fun getPanicAlerts(userId: String): Result<List<PanicAlert>>
    suspend fun updatePanicAlertStatus(alertId: Long, status: PanicAlertStatus): Result<Unit>
}
