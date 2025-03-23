package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.UserActivity
import com.williamfq.data.models.UserActivityStats

@Dao
interface UserActivityDao {

    @Transaction
    suspend fun incrementMessageCount(userId: String, period: String) {
        val existing = getUserActivity(userId, period)
        if (existing == null) {
            insertUserActivity(
                UserActivity(
                    userId = userId,
                    period = period,
                    messagesText = 1,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } else {
            updateMessageCount(userId, period, System.currentTimeMillis())
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserActivity(activity: UserActivity)

    @Query("""
        UPDATE user_activity 
        SET messages_text = messages_text + 1,
            last_updated = :lastUpdated
        WHERE user_id = :userId AND period = :period
    """)
    suspend fun updateMessageCount(userId: String, period: String, lastUpdated: Long)

    @Transaction
    suspend fun incrementCallCount(userId: String, period: String, isVideoCall: Boolean) {
        val existing = getUserActivity(userId, period)
        if (existing == null) {
            insertUserActivity(
                UserActivity(
                    userId = userId,
                    period = period,
                    callsMade = if (!isVideoCall) 1 else 0,
                    videoCalls = if (isVideoCall) 1 else 0,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } else {
            updateCallCount(userId, period, isVideoCall.toInt(), System.currentTimeMillis())
        }
    }

    @Query("""
        UPDATE user_activity 
        SET calls_made = CASE 
                WHEN :isVideoCall = 0 THEN calls_made + 1 
                ELSE calls_made 
            END,
            video_calls = CASE 
                WHEN :isVideoCall = 1 THEN video_calls + 1 
                ELSE video_calls 
            END,
            last_updated = :lastUpdated
        WHERE user_id = :userId AND period = :period
    """)
    suspend fun updateCallCount(
        userId: String,
        period: String,
        isVideoCall: Int,
        lastUpdated: Long
    )

    @Query("""
        SELECT 
            COALESCE(SUM(messages_text), 0) as messagesText,
            COALESCE(SUM(messages_voice), 0) as messagesVoice,
            COALESCE(SUM(calls_made), 0) as callsMade,
            COALESCE(SUM(video_calls), 0) as videoCalls,
            MAX(last_updated) as lastUpdated
        FROM user_activity 
        WHERE user_id = :userId
    """)
    suspend fun getUserActivityStats(userId: String): UserActivityStats

    @Query("""
        SELECT * FROM user_activity 
        WHERE user_id = :userId 
        AND period = :period
    """)
    suspend fun getUserActivity(userId: String, period: String): UserActivity?

    @Query("""
        SELECT * FROM user_activity 
        WHERE user_id = :userId 
        ORDER BY last_updated DESC 
        LIMIT 1
    """)
    suspend fun getLatestUserActivity(userId: String): UserActivity?

    @Query("DELETE FROM user_activity WHERE user_id = :userId AND period = :period")
    suspend fun deleteUserActivity(userId: String, period: String)

    @Query("DELETE FROM user_activity")
    suspend fun clearAllUserActivities()

    @Query("""
        SELECT * FROM user_activity 
        WHERE user_id = :userId 
        AND period BETWEEN :startPeriod AND :endPeriod
        ORDER BY period ASC
    """)
    suspend fun getUserActivitiesInRange(
        userId: String,
        startPeriod: String,
        endPeriod: String
    ): List<UserActivity>

    private fun Boolean.toInt() = if (this) 1 else 0
}
