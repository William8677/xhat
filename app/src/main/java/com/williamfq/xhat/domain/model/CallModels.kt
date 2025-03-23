/*
 * Updated: 2025-01-22 01:29:38
 * Author: William8677
 */

package com.williamfq.xhat.domain.model

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    MISSED,
    ANSWERED,
    REJECTED,
    BUSY
}

data class CallRecord(
    val id: String = "",
    val callType: CallType = CallType.VOICE,
    val status: CallStatus = CallStatus.MISSED,
    val callerUserId: String = "",
    val callerUsername: String = "",
    val callerName: String = "",
    val callerPhoneNumber: String? = null,
    val callerProfileImage: String? = null,
    val receiverUserId: String = "",
    val receiverUsername: String = "",
    val receiverName: String = "",
    val receiverPhoneNumber: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val duration: Long = 0 // en segundos
)