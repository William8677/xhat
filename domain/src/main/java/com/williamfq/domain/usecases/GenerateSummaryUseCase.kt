package com.williamfq.domain.usecases

import com.williamfq.domain.models.ActivityData
import com.williamfq.domain.models.Summary
import javax.inject.Inject

/**
 * Use Case responsable de generar un resumen.
 */
class GenerateSummaryUseCase @Inject constructor() {

    /**
     * Genera un resumen basado en los datos de actividad y el período.
     *
     * @param data Datos de actividad.
     * @param period Período para el resumen (e.g., "weekly", "monthly").
     * @return Un objeto [Summary] con el contenido generado.
     */
    suspend operator fun invoke(data: ActivityData, period: String): Summary {
        val content = buildSummaryContent(data, period)
        return Summary(content = content)
    }

    /**
     * Construye el contenido del resumen basado en los datos de actividad.
     */
    private fun buildSummaryContent(activityData: ActivityData, period: String): String {
        val messagesText = activityData.stats["messages_text"] ?: 0
        val callsMade = activityData.stats["calls_made"] ?: 0
        val reactions = activityData.stats["reactions"] ?: 0

        return """
            Resumen $period:
            - Mensajes de texto enviados: $messagesText
            - Llamadas realizadas: $callsMade
            - Reacciones registradas: $reactions
        """.trimIndent()
    }
}