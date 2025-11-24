package com.empresa.libra_users.data.local.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Notificaciones ligadas a un usuario y opcionalmente a un préstamo.
 * Útil para avisar creación de préstamo, recordatorios de devolución, multas, etc.
 */
@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("userId"),
        Index("loanId"),
        Index(value = ["isRead"]), // consultas rápidas por leídas / no leídas
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val userId: Long,                 // FK -> users.id (obligatorio)
    val loanId: Long? = null,         // FK -> loans.id (opcional: notificación vinculada a un préstamo)

    val title: String,                // Título corto (p.ej. "Préstamo creado")
    val message: String,              // Mensaje descriptivo
    val type: String = "INFO",        // INFO | REMINDER | ALERT | WARNING (string para evitar TypeConverters)

    val createdAt: Long = System.currentTimeMillis(), // epoch millis
    val readAt: Long? = null,        // epoch millis cuando se marca como leída (null si no)
    val isRead: Boolean = false,     // redundante para filtrar rápido
)


