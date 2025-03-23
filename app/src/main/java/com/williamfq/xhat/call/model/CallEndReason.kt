/*
 * Updated: 2025-01-27 03:57:57
 * Author: William8677
 */

package com.williamfq.xhat.call.model

enum class CallEndReason {
    NORMAL,
    ERROR,
    TIMEOUT,
    REMOTE_ENDED,
    NO_ANSWER,
    DECLINED,
    BUSY,
    UNKNOWN,
    USER_HUNG_UP,
    CONNECTION_LOST,
    CALL_FAILED,
    HANGUP;

    fun toAnalyticsString(): String = name.lowercase()

    fun isUserInitiated(): Boolean = this in listOf(NORMAL, DECLINED)
}