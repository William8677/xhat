package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.LocationEntity
import com.williamfq.data.entities.LocationHistoryEntity

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationUpdate(location: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationHistory(location: LocationHistoryEntity)

    @Query("""
        SELECT * FROM locations 
        WHERE user_id = :userId 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun getLastKnownLocation(userId: String): LocationEntity?

    @Query("""
        SELECT * FROM location_history 
        WHERE user_id = :userId 
        ORDER BY timestamp DESC
    """)
    suspend fun getLocationHistory(userId: String): List<LocationHistoryEntity>

    @Query("DELETE FROM locations WHERE user_id = :userId")
    suspend fun deleteActiveLocation(userId: String)

    @Query("DELETE FROM location_history WHERE user_id = :userId")
    suspend fun clearLocationHistory(userId: String)

    @Query("SELECT DISTINCT user_id FROM locations")
    suspend fun getSharingContacts(): List<String>
}