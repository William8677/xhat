package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.StoryEntity

// Interfaz DAO para gestionar operaciones de base de datos de historias en Xhat.
// Incluye índices para optimizar consultas frecuentes.
@Dao
interface StoryDao {

    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getStoryById(id: Int): StoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteStoryById(id: Int)

    @Query("DELETE FROM stories")
    suspend fun deleteAllStories()

    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    suspend fun getAllStories(): List<StoryEntity>

    @Query("SELECT * FROM stories WHERE isActive = 1 ORDER BY timestamp DESC")
    suspend fun getActiveStories(): List<StoryEntity>

    @Query("SELECT * FROM stories WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchStories(query: String): List<StoryEntity>

    @Query("UPDATE stories SET views = views + 1 WHERE id = :id")
    suspend fun incrementStoryViews(id: Int)

    @Query("SELECT * FROM stories WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getStoriesByUser(userId: String): List<StoryEntity>

    @Query("SELECT * FROM stories WHERE ',' || tags || ',' LIKE '%,' || :tag || ',%' ORDER BY timestamp DESC")
    suspend fun getStoriesByTag(tag: String): List<StoryEntity>

    @Query("SELECT SUM(views) AS totalViews, SUM(views) AS totalReactions FROM stories")
    suspend fun getStoryStats(): StoryStats

    @Query("SELECT * FROM stories ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getStoriesWithOffset(offset: Int, limit: Int): List<StoryEntity>
}

data class StoryStats(
    val totalViews: Int = 0,
    val totalReactions: Int = 0
)