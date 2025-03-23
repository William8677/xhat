package com.williamfq.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.williamfq.data.entities.WalkieTalkieAudioEntity

@Dao
interface WalkieTalkieAudioDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: WalkieTalkieAudioEntity)

    @Query("SELECT * FROM walkie_talkie_audio WHERE chatId = :chatId")
    suspend fun getAudioForChat(chatId: String): List<WalkieTalkieAudioEntity>
}
