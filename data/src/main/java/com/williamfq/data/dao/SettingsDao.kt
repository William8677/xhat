package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Settings

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: Settings)

    @Query("SELECT * FROM settings LIMIT 1")
    suspend fun getSettings(): Settings?

    @Update
    suspend fun updateSettings(settings: Settings)

    @Query("DELETE FROM settings")
    suspend fun clearSettings()
}
