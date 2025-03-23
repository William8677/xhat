package com.williamfq.domain.repository

import com.williamfq.domain.models.UserActivityStats


interface UserActivityRepository {

    suspend fun incrementTextMessageCount(userId: String, period: String): Result<Unit>

    suspend fun incrementCallCount(userId: String, isVideoCall: Boolean): Result<Unit>

    suspend fun logReaction(userId: String, reaction: String): Result<Unit>

    suspend fun getUserActivityStats(userId: String): Result<UserActivityStats>
}
