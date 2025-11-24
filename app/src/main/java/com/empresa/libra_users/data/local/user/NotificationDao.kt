package com.empresa.libra_users.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotificationDao {

    // Crear
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(notifications: List<NotificationEntity>): List<Long>

    // Leer
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllByUser(userId: Long): List<NotificationEntity>

    @Query("""
        SELECT * FROM notifications 
        WHERE userId = :userId AND isRead = 0 
        ORDER BY createdAt DESC
    """)
    suspend fun getUnreadByUser(userId: Long): List<NotificationEntity>

    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE userId = :userId AND isRead = 0
    """)
    suspend fun countUnreadByUser(userId: Long): Int

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NotificationEntity?

    // Actualizar (marcar leída)
    @Query("""
        UPDATE notifications 
        SET isRead = 1, readAt = :readAt 
        WHERE id = :id
    """)
    suspend fun markAsRead(id: Long, readAt: Long = System.currentTimeMillis()): Int

    @Query("""
        UPDATE notifications 
        SET isRead = 1, readAt = :readAt
        WHERE userId = :userId AND isRead = 0
    """)
    suspend fun markAllAsRead(userId: Long, readAt: Long = System.currentTimeMillis()): Int

    // Editar (si necesitas cambiar título/mensaje/tipo)
    @Update
    suspend fun update(notification: NotificationEntity): Int

    // Borrar
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long): Int
}


