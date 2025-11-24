package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.NotificationDao
import com.empresa.libra_users.data.local.user.NotificationEntity

class NotificationRepository(
    private val dao: NotificationDao
) {

    // Crear notificación genérica
    suspend fun create(
        userId: Long,
        title: String,
        message: String,
        type: String = "INFO",
        loanId: Long? = null
    ): Result<Long> = runCatching {
        dao.insert(
            NotificationEntity(
                userId = userId,
                loanId = loanId,
                title = title,
                message = message,
                type = type
            )
        )
    }

    // Helpers de dominio: préstamos (ejemplos)
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

    // Lectura
    suspend fun getAllByUser(userId: Long) = runCatching { dao.getAllByUser(userId) }
    suspend fun getUnreadByUser(userId: Long) = runCatching { dao.getUnreadByUser(userId) }
    suspend fun countUnread(userId: Long) = runCatching { dao.countUnreadByUser(userId) }

    // Estado de lectura
    suspend fun markAsRead(id: Long) = runCatching { dao.markAsRead(id) }
    suspend fun markAllAsRead(userId: Long) = runCatching { dao.markAllAsRead(userId) }

    // Borrado
    suspend fun delete(id: Long) = runCatching { dao.deleteById(id) }
    suspend fun deleteAllForUser(userId: Long) = runCatching { dao.deleteAllByUser(userId) }
}

