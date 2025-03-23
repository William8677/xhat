package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Notification

/**
 * DAO para gestionar notificaciones en la base de datos de Xhat.
 */
@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)

    @Update
    suspend fun updateNotification(notification: Notification)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    suspend fun getAllNotifications(): List<Notification>

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: Int): Notification?

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    suspend fun getUnreadNotifications(): List<Notification>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE id IN (:notificationIds)")
    suspend fun markNotificationsAsRead(notificationIds: List<Int>)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: Int)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getNotificationCount(): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    suspend fun getUnreadNotificationCount(): Int

    @Query("SELECT * FROM notifications WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getNotificationsByType(type: String): List<Notification>

    @Query("SELECT * FROM notifications WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getNotificationsByTimeRange(startTime: Long, endTime: Long): List<Notification>
}
