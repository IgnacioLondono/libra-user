package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.LoanDao
import com.empresa.libra_users.data.local.user.LoanEntity
import kotlinx.coroutines.flow.Flow

class LoanRepository(private val loanDao: LoanDao) {
    suspend fun insert(loan: LoanEntity): Long = loanDao.insert(loan)
    fun getAllLoansFlow(): Flow<List<LoanEntity>> = loanDao.getAllLoansFlow()
    suspend fun getAllLoans(): List<LoanEntity> = loanDao.getAllLoans()
    suspend fun getLoanById(id: Long) = loanDao.getLoanById(id)
    fun getLoansByUser(userId: Long): Flow<List<LoanEntity>> = loanDao.getLoansByUser(userId)
    suspend fun update(loan: LoanEntity) = loanDao.update(loan)
    suspend fun countActiveLoans(): Int = loanDao.countActiveLoans()

    /**
     * Counts all loans. For the admin dashboard.
     */
    suspend fun countAllLoans(): Int {
        return loanDao.countAllLoans()
    }

    suspend fun getLoansByStatus(status: String): List<LoanEntity> {
        return loanDao.getLoansByStatus(status)
    }

    suspend fun getLoansByUserAndStatus(userId: Long, status: String): List<LoanEntity> {
        return loanDao.getLoansByUserAndStatus(userId, status)
    }

    suspend fun getLoansByBook(bookId: Long): List<LoanEntity> {
        return loanDao.getLoansByBook(bookId)
    }

    suspend fun hasActiveLoan(userId: Long, bookId: Long): Boolean {
        return loanDao.hasActiveLoan(userId, bookId) > 0
    }

    suspend fun getOverdueLoans(today: String): List<LoanEntity> {
        return loanDao.getOverdueLoans(today)
    }

    suspend fun getLoansByDateRange(fechaInicio: String, fechaFin: String): List<LoanEntity> {
        return loanDao.getLoansByDateRange(fechaInicio, fechaFin)
    }
}
