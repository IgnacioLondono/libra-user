package com.empresa.libra_users.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Insert
    suspend fun insert(loan: LoanEntity): Long

    @Query("SELECT * FROM loans ORDER BY loanDate DESC")
    fun getAllLoansFlow(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans ORDER BY loanDate DESC")
    suspend fun getAllLoans(): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: Long): LoanEntity?

    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY loanDate DESC")
    fun getLoansByUser(userId: Long): Flow<List<LoanEntity>>

    @Update
    suspend fun update(loan: LoanEntity)

    @Query("SELECT COUNT(*) FROM loans WHERE status = 'Active'")
    suspend fun countActiveLoans(): Int

    @Query("SELECT COUNT(*) FROM loans")
    suspend fun countAllLoans(): Int

    // Filtros para préstamos
    @Query("SELECT * FROM loans WHERE status = :status ORDER BY loanDate DESC")
    suspend fun getLoansByStatus(status: String): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE userId = :userId AND status = :status ORDER BY loanDate DESC")
    suspend fun getLoansByUserAndStatus(userId: Long, status: String): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE bookId = :bookId ORDER BY loanDate DESC")
    suspend fun getLoansByBook(bookId: Long): List<LoanEntity>

    // Verificar si un usuario ya tiene un préstamo activo de un libro
    @Query("SELECT COUNT(*) FROM loans WHERE userId = :userId AND bookId = :bookId AND status = 'Active'")
    suspend fun hasActiveLoan(userId: Long, bookId: Long): Int

    // Préstamos vencidos (fecha de devolución pasada y no devueltos)
    @Query("SELECT * FROM loans WHERE dueDate < :today AND status = 'Active'")
    suspend fun getOverdueLoans(today: String): List<LoanEntity>

    // Filtro por rango de fechas
    @Query("SELECT * FROM loans WHERE loanDate >= :fechaInicio AND loanDate <= :fechaFin ORDER BY loanDate DESC")
    suspend fun getLoansByDateRange(fechaInicio: String, fechaFin: String): List<LoanEntity>
}
