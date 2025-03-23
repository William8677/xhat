/*
 * Updated: 2025-01-27 03:41:07
 * Author: William8677
 */

package com.williamfq.xhat.call.model

data class CallStats(
    val duration: Long,
    val bitrate: Int,
    val packetLoss: Float,
    val latency: Int,
    val resolution: String?,
    val bytesTransferred: Long = 0L,
    val quality: CallQuality = CallQuality.GOOD
) {
    companion object {
        fun createEmpty() = CallStats(
            duration = 0L,
            bitrate = 0,
            packetLoss = 0f,
            latency = 0,
            resolution = null,
            bytesTransferred = 0L
        )

        const val MIN_BITRATE = 50 // kbps
        const val MAX_LATENCY = 300 // ms
        const val MAX_PACKET_LOSS = 10f // %
    }

    fun isValid(): Boolean {
        return duration >= 0 &&
                bitrate >= 0 &&
                packetLoss >= 0f &&
                latency >= 0 &&
                bytesTransferred >= 0L
    }

    fun toAnalytics(): Map<String, Any> {
        return mapOf(
            "duration" to duration,
            "bitrate" to bitrate,
            "packet_loss" to packetLoss,
            "latency" to latency,
            "resolution" to (resolution ?: "unknown"),
            "bytes_transferred" to bytesTransferred,
            "quality" to quality.toAnalyticsString()
        )
    }

    override fun toString(): String {
        return "CallStats(" +
                "duration=${duration}ms, " +
                "bitrate=${bitrate}Kbps, " +
                "packetLoss=$packetLoss%, " +
                "latency=${latency}ms, " +
                "resolution=$resolution, " +
                "bytesTransferred=${bytesTransferred}B, " +
                "quality=${quality.description})"
    }
}