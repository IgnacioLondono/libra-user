package com.empresa.libra_users.data.local.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "user",
    // Se añade ColumnInfo para la migración de la base de datos
    @ColumnInfo(defaultValue = "active") val status: String = "active",
    val profilePictureUri: String? = null
)
