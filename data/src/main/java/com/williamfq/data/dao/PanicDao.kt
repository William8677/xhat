package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.PanicAlertEntity
import com.williamfq.data.entities.PanicAlertStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PanicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanicAlert(alert: PanicAlertEntity): Long

    @Query("""
        SELECT * FROM panic_alerts 
        WHERE user_id = :userId 
        ORDER BY alert_timestamp DESC
    """)
    suspend fun getPanicAlertsByUserId(userId: String): List<PanicAlertEntity>

    @Query("""
        UPDATE panic_alerts 
        SET alert_status = :status 
        WHERE alert_id = :alertId
    """)
    suspend fun updatePanicAlertStatus(alertId: Long, status: PanicAlertStatusEntity)

    @Query("SELECT * FROM panic_alerts ORDER BY alert_timestamp DESC")
    fun getAllPanicAlerts(): Flow<List<PanicAlertEntity>>

    @Query("""
        SELECT * FROM panic_alerts 
        WHERE alert_status = :status 
        ORDER BY alert_timestamp DESC
    """)
    suspend fun getPanicAlertsByStatus(status: PanicAlertStatusEntity): List<PanicAlertEntity>

    @Delete
    suspend fun deletePanicAlert(alert: PanicAlertEntity)

    @Query("DELETE FROM panic_alerts WHERE user_id = :userId")
    suspend fun deleteAllPanicAlertsByUser(userId: String)
}