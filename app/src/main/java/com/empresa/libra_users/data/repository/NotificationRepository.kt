package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.NotificationEntity
import com.empresa.libra_users.data.remote.dto.NotificationApi
import com.empresa.libra_users.data.remote.mapper.toEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi
) {
    
    // Estado en memoria para las notificaciones (sin Room)
    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    suspend fun create(
        userId: Long,
        title: String,
        message: String,
        type: String = "INFO",
        loanId: Long? = null
    ): Result<Long> {
        return try {
            // Las notificaciones se crean en el backend cuando ocurren eventos
            // Este método puede ser usado para crear notificaciones locales si es necesario
            val notification = NotificationEntity(
                id = 0L,
                userId = userId,
                loanId = loanId,
                title = title,
                message = message,
                type = type,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            // Agregar al estado en memoria
            val currentNotifications = _notifications.value.toMutableList()
            currentNotifications.add(notification)
            _notifications.value = currentNotifications
            Result.success(notification.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun notifyLoanCreated(userId: Long, loanId: Long): Result<Long> =
        create(
            userId = userId,
            loanId = loanId,
            title = "Préstamo creado",
            message = "Se ha registrado tu préstamo #$loanId.",
            type = "INFO"
        )

    suspend fun notifyLoanReminder(userId: Long, loanId: Long, daysLeft: Int): Result<Long> =
        create(
            userId = userId,
            loanId = loanId,
            title = "Recordatorio de devolución",
            message = "Quedan $daysLeft día(s) para devolver el libro del préstamo #$loanId.",
            type = "REMINDER"
        )

    suspend fun notifyLoanOverdue(userId: Long, loanId: Long): Result<Long> =
        create(
            userId = userId,
            loanId = loanId,
            title = "Préstamo vencido",
            message = "Tu préstamo #$loanId está vencido. Devuelve el libro cuanto antes.",
            type = "ALERT"
        )

    /**
     * Carga todas las notificaciones de un usuario desde el API.
     */
    suspend fun loadUserNotifications(userId: Long): Result<Int> {
        return try {
            val response = notificationApi.getUserNotifications(userId.toString())
            if (response.isSuccessful && response.body() != null) {
                val notificationsDto = response.body()!!
                val notificationsEntity = notificationsDto.map { it.toEntity() }
                // Actualizar estado en memoria (solo notificaciones de este usuario)
                val otherNotifications = _notifications.value.filter { it.userId != userId }
                _notifications.value = otherNotifications + notificationsEntity
                Result.success(notificationsEntity.size)
            } else {
                Result.failure(IllegalArgumentException("Error al cargar notificaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllByUser(userId: Long): Result<List<NotificationEntity>> {
        return try {
            // Cargar desde API si no están en memoria
            if (_notifications.value.none { it.userId == userId }) {
                loadUserNotifications(userId)
            }
            val userNotifications = _notifications.value.filter { it.userId == userId }
            Result.success(userNotifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUnreadByUser(userId: Long): Result<List<NotificationEntity>> {
        return try {
            if (_notifications.value.none { it.userId == userId }) {
                loadUserNotifications(userId)
            }
            val unreadNotifications = _notifications.value.filter { 
                it.userId == userId && !it.isRead 
            }
            Result.success(unreadNotifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun countUnread(userId: Long): Result<Int> {
        return try {
            if (_notifications.value.none { it.userId == userId }) {
                loadUserNotifications(userId)
            }
            val count = _notifications.value.count { 
                it.userId == userId && !it.isRead 
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: Long): Result<Unit> {
        return try {
            val response = notificationApi.markNotificationAsRead(id.toString())
            if (response.isSuccessful) {
                // Actualizar estado en memoria
                val currentNotifications = _notifications.value.toMutableList()
                val index = currentNotifications.indexOfFirst { it.id == id }
                if (index >= 0) {
                    currentNotifications[index] = currentNotifications[index].copy(isRead = true)
                    _notifications.value = currentNotifications
                }
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Error al marcar como leída"))
            }
        } catch (e: Exception) {
            // Actualizar estado en memoria aunque falle el API
            val currentNotifications = _notifications.value.toMutableList()
            val index = currentNotifications.indexOfFirst { it.id == id }
            if (index >= 0) {
                currentNotifications[index] = currentNotifications[index].copy(isRead = true)
                _notifications.value = currentNotifications
            }
            Result.success(Unit)
        }
    }
    
    suspend fun markAllAsRead(userId: Long): Result<Unit> {
        return try {
            val response = notificationApi.markAllAsRead(userId.toString())
            if (response.isSuccessful) {
                // Actualizar estado en memoria
                val currentNotifications = _notifications.value.toMutableList()
                currentNotifications.forEachIndexed { index, notification ->
                    if (notification.userId == userId && !notification.isRead) {
                        currentNotifications[index] = notification.copy(isRead = true)
                    }
                }
                _notifications.value = currentNotifications
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Error al marcar todas como leídas"))
            }
        } catch (e: Exception) {
            // Actualizar estado en memoria aunque falle el API
            val currentNotifications = _notifications.value.toMutableList()
            currentNotifications.forEachIndexed { index, notification ->
                if (notification.userId == userId && !notification.isRead) {
                    currentNotifications[index] = notification.copy(isRead = true)
                }
            }
            _notifications.value = currentNotifications
            Result.success(Unit)
        }
    }

    suspend fun delete(id: Long): Result<Unit> {
        return try {
            val response = notificationApi.deleteNotification(id.toString())
            if (response.isSuccessful) {
                // Eliminar del estado en memoria
                _notifications.value = _notifications.value.filter { it.id != id }
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Error al eliminar notificación"))
            }
        } catch (e: Exception) {
            // Eliminar del estado en memoria aunque falle el API
            _notifications.value = _notifications.value.filter { it.id != id }
            Result.success(Unit)
        }
    }
    
    suspend fun deleteAllForUser(userId: Long): Result<Unit> {
        return try {
            val response = notificationApi.deleteAllNotifications(userId.toString())
            if (response.isSuccessful) {
                // Eliminar del estado en memoria
                _notifications.value = _notifications.value.filter { it.userId != userId }
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Error al eliminar todas las notificaciones"))
            }
        } catch (e: Exception) {
            // Eliminar del estado en memoria aunque falle el API
            _notifications.value = _notifications.value.filter { it.userId != userId }
            Result.success(Unit)
        }
    }
}
