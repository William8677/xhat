package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Response

@Dao
interface PollResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPollResponse(response: Response)

    @Query("SELECT * FROM poll_responses WHERE pollId = :pollId")
    suspend fun getResponsesByPoll(pollId: Int): List<Response>

    @Query("SELECT * FROM poll_responses WHERE userId = :userId AND pollId = :pollId")
    suspend fun getResponseByUserAndPoll(userId: String, pollId: Int): Response?

    @Query("DELETE FROM poll_responses WHERE pollId = :pollId")
    suspend fun deleteResponsesByPoll(pollId: Int)

    @Query("DELETE FROM poll_responses")
    suspend fun clearAllResponses()
}
