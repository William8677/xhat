/*
 * Updated: 2025-01-27 03:07:30
 * Author: William8677
 */

package com.williamfq.xhat.call.model

data class RtcStats(
    val txKBitRate: Int = 0,
    val rxKBitRate: Int = 0,
    val txPacketLossRate: Float = 0f,
    val rxPacketLossRate: Float = 0f,
    val lastmileDelay: Int = 0,
    val cpuAppUsage: Double = 0.0,
    val cpuTotalUsage: Double = 0.0,
    val memoryAppUsageInKbytes: Long = 0L,
    val memoryTotalUsageInKbytes: Long = 0L,
    val memoryAppUsageInPercentage: Double = 0.0
) {
    fun toCallStats(duration: Long, resolution: String?): CallStats {
        return CallStats(
            duration = duration,
            bitrate = txKBitRate + rxKBitRate,
            packetLoss = (txPacketLossRate + rxPacketLossRate) / 2f,
            latency = lastmileDelay,
            resolution = resolution
        )
    }

    override fun toString(): String {
        return "RtcStats(" +
                "tx=${txKBitRate}Kbps, " +
                "rx=${rxKBitRate}Kbps, " +
                "txLoss=$txPacketLossRate%, " +
                "rxLoss=$rxPacketLossRate%, " +
                "delay=${lastmileDelay}ms, " +
                "cpuApp=$cpuAppUsage%, " +
                "cpuTotal=$cpuTotalUsage%, " +
                "memApp=${memoryAppUsageInKbytes}KB, " +
                "memTotal=${memoryTotalUsageInKbytes}KB, " +
                "memPercentage=$memoryAppUsageInPercentage%)"
    }
}