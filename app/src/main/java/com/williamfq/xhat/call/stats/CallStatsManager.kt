/*
 * Updated: 2025-02-13 01:05:09
 * Author: William8677
 */

package com.williamfq.xhat.call.stats

import com.williamfq.xhat.call.model.CallQuality
import com.williamfq.xhat.call.model.CallQualityIssue
import com.williamfq.xhat.call.model.CallStats
import com.williamfq.xhat.call.model.RtcStats
import com.williamfq.xhat.utils.analytics.Analytics
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallStatsManager @Inject constructor(
    private val analytics: Analytics,
    private val logger: LoggerInterface
) {
    private val _stats = MutableStateFlow<CallStats?>(null)
    val stats: StateFlow<CallStats?> = _stats
    private val statsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var startTime: Long = 0
    private var lastQualityWarning: Long = 0
    private val qualityWarningThreshold = 10_000L // 10 segundos entre advertencias

    companion object {
        private const val TAG = "CallStatsManager"
    }

    fun onCallStart() {
        startTime = System.currentTimeMillis()
        _stats.value = CallStats.createEmpty()

        statsScope.launch {
            logger.logEvent(
                tag = TAG,
                message = "Started monitoring call statistics",
                level = LogLevel.INFO
            )
        }
    }

    fun updateStats(rtcStats: RtcStats) {
        statsScope.launch {
            try {
                val duration = System.currentTimeMillis() - startTime
                val currentStats = _stats.value ?: CallStats.createEmpty()

                _stats.value = currentStats.copy(
                    duration = duration,
                    bitrate = rtcStats.txKBitRate + rtcStats.rxKBitRate,
                    packetLoss = (rtcStats.txPacketLossRate + rtcStats.rxPacketLossRate) / 2f,
                    latency = rtcStats.lastmileDelay,
                    quality = CallQuality.fromStats(currentStats)
                )

                analyzeCallQuality(_stats.value)
                logger.logEvent(
                    tag = TAG,
                    message = "Stats updated: ${_stats.value}",
                    level = LogLevel.DEBUG
                )
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error updating stats",
                    level = LogLevel.ERROR,
                    throwable = e
                )
            }
        }
    }

    fun updateVideoStats(width: Int, height: Int) {
        statsScope.launch {
            try {
                _stats.value = _stats.value?.copy(
                    resolution = "${width}x${height}"
                )
                logger.logEvent(
                    tag = TAG,
                    message = "Video resolution updated: ${width}x${height}",
                    level = LogLevel.DEBUG
                )
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error updating video stats",
                    level = LogLevel.ERROR,
                    throwable = e
                )
            }
        }
    }

    private fun analyzeCallQuality(stats: CallStats?) {
        if (stats == null) return

        val now = System.currentTimeMillis()
        if (now - lastQualityWarning < qualityWarningThreshold) return

        statsScope.launch {
            try {
                val issues = buildList {
                    // Verificar latencia
                    if (stats.latency > CallStats.MAX_LATENCY) {
                        add(CallQualityIssue.HIGH_LATENCY)
                        logger.logEvent(
                            tag = TAG,
                            message = "High latency detected: ${stats.latency}ms",
                            level = LogLevel.WARNING
                        )
                    }

                    // Verificar pérdida de paquetes
                    if (stats.packetLoss > CallStats.MAX_PACKET_LOSS) {
                        add(CallQualityIssue.PACKET_LOSS)
                        logger.logEvent(
                            tag = TAG,
                            message = "Packet loss detected: ${stats.packetLoss}%",
                            level = LogLevel.WARNING
                        )
                    }

                    // Verificar bitrate
                    if (stats.bitrate < CallStats.MIN_BITRATE) {
                        add(CallQualityIssue.LOW_BITRATE)
                        logger.logEvent(
                            tag = TAG,
                            message = "Low bitrate detected: ${stats.bitrate}Kbps",
                            level = LogLevel.WARNING
                        )
                    }
                }

                if (issues.isNotEmpty()) {
                    lastQualityWarning = now

                    // Ordenar problemas por prioridad
                    val sortedIssues = issues.sortedByDescending {
                        CallQualityIssue.getNotificationPriority(it)
                    }

                    analytics.trackEvent("call_quality_issues_detected")

                    val recommendations = sortedIssues.joinToString("\n") { issue ->
                        "  • ${issue.getRecommendation()} (${issue.userMessage})"
                    }

                    logger.logEvent(
                        tag = TAG,
                        message = """
                            Quality issues detected:
                            - Current quality: ${stats.quality.description}
                            - Stats:
                              • Latency: ${stats.latency}ms
                              • Packet loss: ${stats.packetLoss}%
                              • Bitrate: ${stats.bitrate}Kbps
                              • Resolution: ${stats.resolution ?: "N/A"}
                            - Issues detected (${sortedIssues.size}):
                            $recommendations
                        """.trimIndent(),
                        level = LogLevel.WARNING
                    )
                }
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error analyzing call quality",
                    level = LogLevel.ERROR,
                    throwable = e
                )
            }
        }
    }
}