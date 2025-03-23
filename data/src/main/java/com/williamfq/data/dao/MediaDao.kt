package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Media

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: Media)

    @Query("SELECT * FROM media WHERE messageId = :messageId")
    suspend fun getMediaByMessage(messageId: String): List<Media>

    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: Int)

    @Query("DELETE FROM media")
    suspend fun clearAllMedia()
}
