package com.williamfq.data.repository

import com.williamfq.data.dao.UserActivityDao
import com.williamfq.domain.repository.UserActivityRepository
import com.williamfq.domain.models.UserActivityStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class UserActivityRepositoryImpl @Inject constructor(
    private val userActivityDao: UserActivityDao
) : UserActivityRepository {

    private val periodFormatter = SimpleDateFormat("yyyy-MM", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun incrementTextMessageCount(userId: String, period: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                require(userId.isNotBlank()) { "userId cannot be blank" }
                require(period.isNotBlank()) { "period cannot be blank" }

                userActivityDao.incrementMessageCount(userId, period)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun incrementCallCount(userId: String, isVideoCall: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                require(userId.isNotBlank()) { "userId cannot be blank" }

                val period = periodFormatter.format(Date())
                userActivityDao.incrementCallCount(userId, period, isVideoCall)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun logReaction(userId: String, reaction: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                require(userId.isNotBlank()) { "userId cannot be blank" }
                require(reaction.isNotBlank()) { "reaction cannot be blank" }

                val messageId = "msg_${System.currentTimeMillis()}"

                // Log reaction (define logReaction in DAO)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getUserActivityStats(userId: String): Result<UserActivityStats> =
        withContext(Dispatchers.IO) {
            try {
                require(userId.isNotBlank()) { "userId cannot be blank" }

                val dataStats = userActivityDao.getUserActivityStats(userId)

                val domainStats = UserActivityStats(
                    messagesText = dataStats.messagesText,
                    callsMade = dataStats.callsMade,
                    lastUpdated = dataStats.lastUpdated
                )

                Result.success(domainStats)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
