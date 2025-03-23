package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.CallHistory

@Dao
interface CallHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallHistory)

    @Update
    suspend fun updateCall(call: CallHistory) // Añadido

    @Delete
    suspend fun deleteCall(call: CallHistory) // Añadido

    @Query("SELECT * FROM call_history WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getCallHistoryByUser(userId: String): List<CallHistory>

    @Query("DELETE FROM call_history")
    suspend fun clearCallHistory()
}

