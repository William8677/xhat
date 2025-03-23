/*
 * Updated: 2025-01-27 04:38:45
 * Author: William8677
 */

package com.williamfq.xhat.call.model

enum class CallQuality(val description: String) {
    EXCELLENT("Calidad excelente"),
    GOOD("Buena calidad"),
    FAIR("Calidad aceptable"),
    POOR("Calidad baja");

    fun toAnalyticsString(): String = name.lowercase()

    fun getIssues(stats: CallStats): Set<CallQualityIssue> {
        val issues = mutableSetOf<CallQualityIssue>()

        // Verifica la latencia - crítico para llamadas en tiempo real
        if (stats.latency > LATENCY_THRESHOLD_POOR) {
            issues.add(CallQualityIssue.HIGH_LATENCY)
        }

        // Verifica pérdida de paquetes - afecta la calidad del audio/video
        if (stats.packetLoss > PACKET_LOSS_THRESHOLD_POOR) {
            issues.add(CallQualityIssue.PACKET_LOSS)
        }

        // Verifica el bitrate - importante para video llamadas
        if (stats.bitrate < MIN_BITRATE_VIDEO) {
            issues.add(CallQualityIssue.LOW_BITRATE)
        }

        return issues
    }

    companion object {
        // Umbrales optimizados para llamadas móviles
        private const val LATENCY_THRESHOLD_EXCELLENT = 150  // ms
        private const val LATENCY_THRESHOLD_GOOD = 250      // ms
        private const val LATENCY_THRESHOLD_FAIR = 400      // ms
        private const val LATENCY_THRESHOLD_POOR = 500      // ms

        private const val PACKET_LOSS_THRESHOLD_EXCELLENT = 1.0f  // %
        private const val PACKET_LOSS_THRESHOLD_GOOD = 3.0f      // %
        private const val PACKET_LOSS_THRESHOLD_FAIR = 5.0f      // %
        private const val PACKET_LOSS_THRESHOLD_POOR = 7.0f      // %

        private const val MIN_BITRATE_VIDEO = 150     // Kbps (Para video llamadas)
        private const val MIN_BITRATE_AUDIO = 30      // Kbps (Para llamadas de voz)

        fun fromStats(stats: CallStats): CallQuality {
            return when {
                // Calidad Excelente
                stats.packetLoss <= PACKET_LOSS_THRESHOLD_EXCELLENT &&
                        stats.latency <= LATENCY_THRESHOLD_EXCELLENT &&
                        stats.bitrate >= MIN_BITRATE_VIDEO -> EXCELLENT

                // Buena Calidad
                stats.packetLoss <= PACKET_LOSS_THRESHOLD_GOOD &&
                        stats.latency <= LATENCY_THRESHOLD_GOOD &&
                        stats.bitrate >= MIN_BITRATE_AUDIO -> GOOD

                // Calidad Aceptable
                stats.packetLoss <= PACKET_LOSS_THRESHOLD_FAIR &&
                        stats.latency <= LATENCY_THRESHOLD_FAIR -> FAIR

                // Calidad Baja
                else -> POOR
            }
        }
    }
}