/*
 * Updated: 2025-01-27 03:57:57
 * Author: William8677
 */

package com.williamfq.xhat.call.model

enum class CallType {
    VOICE,
    VIDEO;

    fun toAnalyticsString(): String = name.lowercase()

    fun isVideo(): Boolean = this == VIDEO
}