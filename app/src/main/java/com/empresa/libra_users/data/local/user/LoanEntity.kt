// com/empresa/libra_users/data/local/user/LoanEntity.kt
package com.empresa.libra_users.data.local.user

import androidx.room.*

@Entity(
    tableName = "loans",
    indices = [Index("userId"), Index("bookId")],   //  índices recomendados
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val bookId: Long,
    val loanDate: String,        // o Long (epoch) si prefieres
    val dueDate: String,
    val returnDate: String?,     // nullable hasta que devuelva
    val status: String           // "Active", "Returned", "Overdue"
)
