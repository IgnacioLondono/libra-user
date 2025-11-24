package com.empresa.libra_users.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Insertar un usuario
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    // Obtener un usuario por email (para login)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    // Contar el número de usuarios en la tabla
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    /**
     * Counts all users in the users table.
     * Added for the admin dashboard as per specific requirements.
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    // Obtener todos los usuarios
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAll(): Flow<List<UserEntity>>

    // Obtener un usuario por su ID
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    // Actualizar un usuario
    @Update
    suspend fun update(user: UserEntity)

    // Eliminar un usuario
    @Delete
    suspend fun delete(user: UserEntity)
}
