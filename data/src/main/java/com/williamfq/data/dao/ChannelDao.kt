package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Channel

@Dao
interface ChannelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createChannel(channel: Channel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createChannels(channels: List<Channel>)

    @Query("SELECT * FROM channels WHERE id = :channelId")
    suspend fun getChannelById(channelId: Int): Channel?

    @Query("SELECT * FROM channels WHERE createdBy = :creatorId")
    suspend fun getChannelsByCreator(creatorId: String): List<Channel>

    @Query("SELECT * FROM channels ORDER BY createdAt DESC")
    suspend fun getAllChannels(): List<Channel>

    @Update
    suspend fun updateChannel(channel: Channel)

    @Query("DELETE FROM channels WHERE id = :channelId")
    suspend fun deleteChannelById(channelId: Int)

    @Query("DELETE FROM channels")
    suspend fun clearChannels()
}
