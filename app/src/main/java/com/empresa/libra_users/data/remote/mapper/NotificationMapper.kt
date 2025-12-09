package com.empresa.libra_users.data.remote.mapper

import com.empresa.libra_users.data.local.user.NotificationEntity
import com.empresa.libra_users.data.remote.dto.NotificationDto

fun NotificationDto.toEntity(): NotificationEntity {
    // Intentar parsear createdAt como timestamp (epoch millis) o ISO string
    val createdAtMillis = try {
        createdAt.toLongOrNull() ?: run {
            // Si es un string ISO, intentar parsearlo
            // Por ahora, usar el timestamp actual si no se puede parsear
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
    
    return NotificationEntity(
        id = id.toLongOrNull() ?: 0L,
        userId = userId.toLongOrNull() ?: 0L,
        loanId = null, // El DTO no tiene loanId, se puede agregar si es necesario
        title = title,
        message = message,
        type = type,
        createdAt = createdAtMillis,
        readAt = if (read) System.currentTimeMillis() else null,
        isRead = read
    )
}

