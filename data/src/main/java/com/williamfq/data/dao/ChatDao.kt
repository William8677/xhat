package com.williamfq.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.williamfq.data.entities.MessageEntity
import com.williamfq.data.entities.WalkieTalkieAudioEntity
import com.williamfq.data.entities.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessageStatus(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE chat_id = :chatId ORDER BY timestamp DESC")
    suspend fun getMessagesByChatIdSync(chatId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE is_read = 0 ORDER BY timestamp DESC")
    fun getOfflineMessages(): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("UPDATE messages SET message_status = :status WHERE id = :messageId")
    suspend fun updateMessageStatusById(messageId: Long, status: String)

    @Query("UPDATE messages SET message_content = :newContent WHERE id = :messageId")
    suspend fun editMessage(messageId: Long, newContent: String)

    @Query("DELETE FROM messages WHERE chat_id = :chatId")
    suspend fun deleteChat(chatId: String)

    @Query("DELETE FROM messages WHERE chat_id = :chatId")
    suspend fun clearChat(chatId: String)

    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND message_content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(chatId: String, query: String): List<MessageEntity>

    // Cambiamos este método para que retorne ChatEntity
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatInfoEntity(chatId: String): ChatEntity

    @Insert
    suspend fun insertWalkieTalkieAudio(walkieTalkieAudio: WalkieTalkieAudioEntity)

    @Query("SELECT audiodata FROM walkie_talkie_audio WHERE chatId = :chatId")
    fun getWalkieTalkieAudio(chatId: String): Flow<ByteArray>

    @Query("UPDATE messages SET message_status = :status WHERE id = :messageId")
    suspend fun editMessageStatus(messageId: Long, status: String)
}
