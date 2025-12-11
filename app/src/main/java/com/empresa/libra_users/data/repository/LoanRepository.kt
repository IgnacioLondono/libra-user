package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.remote.dto.CreateLoanRequestDto
import com.empresa.libra_users.data.remote.dto.LoanApi
import com.empresa.libra_users.data.remote.mapper.toEntity
import com.empresa.libra_users.domain.validation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val loanApi: LoanApi
) {
    
    // Estado en memoria para los préstamos (sin Room)
    private val _loans = MutableStateFlow<List<LoanEntity>>(emptyList())
    val loans: StateFlow<List<LoanEntity>> = _loans.asStateFlow()
    
    /**
     * Crea un préstamo en la base de datos del microservicio.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     */
    suspend fun insert(loan: LoanEntity): Result<Long> {
        // Validaciones antes de llamar al API
        val userIdError = validateId(loan.userId)
        if (userIdError != null) {
            return Result.failure(IllegalArgumentException(userIdError))
        }
        
        val bookIdError = validateId(loan.bookId)
        if (bookIdError != null) {
            return Result.failure(IllegalArgumentException(bookIdError))
        }
        
        val dateError = validateLoanDates(loan.loanDate, loan.dueDate)
        if (dateError != null) {
            return Result.failure(IllegalArgumentException(dateError))
        }
        
        return try {
            // PRIMERO: Intentar crear en el API/base de datos
            val request = CreateLoanRequestDto(
                userId = loan.userId,
                bookId = loan.bookId,
                loanDate = loan.loanDate,
                dueDate = loan.dueDate
            )
            val response = loanApi.createLoan(request)
            
            if (response.isSuccessful && response.body() != null) {
                val loanDto = response.body()!!
                val loanEntity = loanDto.toEntity()
                // SOLO SI ES EXITOSO: Actualizar estado en memoria
                val currentLoans = _loans.value.toMutableList()
                currentLoans.add(loanEntity)
                _loans.value = currentLoans
                Result.success(loanEntity.id)
            } else {
                Result.failure(IllegalArgumentException("Error al crear préstamo en la base de datos"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }
    
    /**
     * Carga todos los préstamos de un usuario desde el API.
     */
    suspend fun loadUserLoans(userId: Long): Result<Int> {
        return try {
            val response = loanApi.getUserLoans(userId.toString())
            if (response.isSuccessful && response.body() != null) {
                val loansDto = response.body()!!
                val loansEntity = loansDto.map { it.toEntity() }
                // Actualizar estado en memoria (solo préstamos de este usuario)
                val otherLoans = _loans.value.filter { it.userId != userId }
                _loans.value = otherLoans + loansEntity
                Result.success(loansEntity.size)
            } else {
                Result.failure(IllegalArgumentException("Error al cargar préstamos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getAllLoansFlow(): StateFlow<List<LoanEntity>> = loans
    
    suspend fun getAllLoans(): List<LoanEntity> {
        return _loans.value
    }
    
    suspend fun getLoanById(id: Long): LoanEntity? {
        return _loans.value.find { it.id == id }
    }
    
    fun getLoansByUser(userId: Long): StateFlow<List<LoanEntity>> {
        // Filtrar del estado en memoria
        val filtered = MutableStateFlow(_loans.value.filter { it.userId == userId })
        
        // Cargar desde API si no hay préstamos en memoria para este usuario
        if (filtered.value.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                loadUserLoans(userId)
                filtered.value = _loans.value.filter { it.userId == userId }
            }
        }
        
        return filtered.asStateFlow()
    }
    
    /**
     * Actualiza un préstamo en la base de datos del microservicio.
     * Para operaciones como devolver o extender préstamo, se debe usar los métodos específicos del API.
     * Este método solo actualiza el estado en memoria (para casos especiales).
     */
    suspend fun update(loan: LoanEntity): Result<Unit> {
        return try {
            // Actualizar estado en memoria
            // NOTA: Para operaciones reales (devolver, extender), usar los métodos específicos del API
            val currentLoans = _loans.value.toMutableList()
            val index = currentLoans.indexOfFirst { it.id == loan.id }
            if (index >= 0) {
                currentLoans[index] = loan
                _loans.value = currentLoans
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Préstamo no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }
    
    /**
     * Devuelve un préstamo en la base de datos del microservicio.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     */
    suspend fun returnLoan(loanId: Long): Result<Unit> {
        return try {
            // PRIMERO: Intentar devolver en el API/base de datos
            val response = loanApi.returnLoan(loanId.toString())
            
            if (response.isSuccessful && response.body() != null) {
                val returnedLoanDto = response.body()!!
                val returnedLoan = returnedLoanDto.toEntity()
                // SOLO SI ES EXITOSO: Actualizar estado en memoria
                val currentLoans = _loans.value.toMutableList()
                val index = currentLoans.indexOfFirst { it.id == loanId }
                if (index >= 0) {
                    currentLoans[index] = returnedLoan
                    _loans.value = currentLoans
                }
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Error al devolver préstamo en la base de datos"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }
    
    /**
     * Extiende un préstamo en la base de datos del microservicio.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     */
    suspend fun extendLoan(loanId: Long): Result<Unit> {
        return try {
            // PRIMERO: Intentar extender en el API/base de datos
            val response = loanApi.extendLoan(loanId.toString())
            
            if (response.isSuccessful && response.body() != null) {
                val extendedLoanDto = response.body()!!
                val extendedLoan = extendedLoanDto.toEntity()
                // SOLO SI ES EXITOSO: Actualizar estado en memoria
                val currentLoans = _loans.value.toMutableList()
                val index = currentLoans.indexOfFirst { it.id == loanId }
                if (index >= 0) {
                    currentLoans[index] = extendedLoan
                    _loans.value = currentLoans
                }
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Error al extender préstamo en la base de datos"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }
    
    suspend fun countActiveLoans(): Int {
        return _loans.value.count { it.status == "Active" }
    }

    suspend fun countAllLoans(): Int {
        return _loans.value.size
    }

    suspend fun getLoansByStatus(status: String): List<LoanEntity> {
        return _loans.value.filter { it.status == status }
    }

    suspend fun getLoansByUserAndStatus(userId: Long, status: String): List<LoanEntity> {
        return _loans.value.filter { it.userId == userId && it.status == status }
    }

    suspend fun getLoansByBook(bookId: Long): List<LoanEntity> {
        return _loans.value.filter { it.bookId == bookId }
    }

    suspend fun hasActiveLoan(userId: Long, bookId: Long): Boolean {
        return _loans.value.any { 
            it.userId == userId && 
            it.bookId == bookId && 
            it.status == "Active" 
        }
    }

    suspend fun getOverdueLoans(today: String): List<LoanEntity> {
        return _loans.value.filter { 
            it.status == "Active" && 
            it.dueDate < today 
        }
    }

    suspend fun getLoansByDateRange(fechaInicio: String, fechaFin: String): List<LoanEntity> {
        return _loans.value.filter { 
            it.loanDate >= fechaInicio && 
            it.loanDate <= fechaFin 
        }
    }
}
