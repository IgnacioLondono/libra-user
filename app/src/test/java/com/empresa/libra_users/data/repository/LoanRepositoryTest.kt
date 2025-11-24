package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.LoanDao
import com.empresa.libra_users.data.local.user.LoanEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LoanRepositoryTest {

    private lateinit var loanDao: LoanDao
    private lateinit var loanRepository: LoanRepository

    @Before
    fun setup() {
        loanDao = mockk(relaxed = true)
        loanRepository = LoanRepository(loanDao)
    }

    @Test
    fun `insert debería llamar a loanDao insert`() = runTest {
        // Arrange
        val loan = LoanEntity(
            id = 1L,
            userId = 1L,
            bookId = 1L,
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = "2024-01-15",
            status = "active"
        )
        coEvery { loanDao.insert(loan) } returns 1L

        // Act
        val result = loanRepository.insert(loan)

        // Assert
        assertEquals(1L, result)
        coEvery { loanDao.insert(loan) }
    }

    @Test
    fun `getAllLoansFlow debería retornar Flow de loans`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active"),
            LoanEntity(id = 2L, userId = 2L, bookId = 2L, loanDate = "2024-01-02", dueDate = "2024-01-16", returnDate = "2024-01-16", status = "active")
        )
        every { loanDao.getAllLoansFlow() } returns flowOf(loans)

        // Act
        val result = loanRepository.getAllLoansFlow()

        // Assert
        assertNotNull(result)
        result.collect { collectedLoans ->
            assertEquals(2, collectedLoans.size)
        }
    }

    @Test
    fun `getAllLoans debería retornar lista de loans`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active")
        )
        coEvery { loanDao.getAllLoans() } returns loans

        // Act
        val result = loanRepository.getAllLoans()

        // Assert
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun `getLoanById debería retornar loan específico`() = runTest {
        // Arrange
        val loan = LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active")
        coEvery { loanDao.getLoanById(1L) } returns loan

        // Act
        val result = loanRepository.getLoanById(1L)

        // Assert
        assertNotNull(result)
        assertEquals(1L, result!!.id)
    }

    @Test
    fun `getLoansByUser debería retornar Flow de loans del usuario`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active")
        )
        every { loanDao.getLoansByUser(1L) } returns flowOf(loans)

        // Act
        val result = loanRepository.getLoansByUser(1L)

        // Assert
        result.collect { collectedLoans ->
            assertEquals(1, collectedLoans.size)
            assertEquals(1L, collectedLoans[0].userId)
        }
    }

    @Test
    fun `update debería llamar a loanDao update`() = runTest {
        // Arrange
        val loan = LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "returned")
        coEvery { loanDao.update(loan) } returns Unit

        // Act
        loanRepository.update(loan)

        // Assert
        coEvery { loanDao.update(loan) }
    }

    @Test
    fun `countActiveLoans debería retornar número de loans activos`() = runTest {
        // Arrange
        coEvery { loanDao.countActiveLoans() } returns 5

        // Act
        val result = loanRepository.countActiveLoans()

        // Assert
        assertEquals(5, result)
    }

    @Test
    fun `countAllLoans debería retornar número total de loans`() = runTest {
        // Arrange
        coEvery { loanDao.countAllLoans() } returns 10

        // Act
        val result = loanRepository.countAllLoans()

        // Assert
        assertEquals(10, result)
    }

    @Test
    fun `getLoansByStatus debería retornar loans por estado`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active")
        )
        coEvery { loanDao.getLoansByStatus("active") } returns loans

        // Act
        val result = loanRepository.getLoansByStatus("active")

        // Assert
        assertEquals(1, result.size)
        assertEquals("active", result[0].status)
    }

    @Test
    fun `getLoansByUserAndStatus debería retornar loans filtrados`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active")
        )
        coEvery { loanDao.getLoansByUserAndStatus(1L, "active") } returns loans

        // Act
        val result = loanRepository.getLoansByUserAndStatus(1L, "active")

        // Assert
        assertEquals(1, result.size)
        assertEquals(1L, result[0].userId)
        assertEquals("active", result[0].status)
    }

    @Test
    fun `getLoansByBook debería retornar loans por libro`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active")
        )
        coEvery { loanDao.getLoansByBook(1L) } returns loans

        // Act
        val result = loanRepository.getLoansByBook(1L)

        // Assert
        assertEquals(1, result.size)
        assertEquals(1L, result[0].bookId)
    }

    @Test
    fun `hasActiveLoan debería retornar true si existe loan activo`() = runTest {
        // Arrange
        coEvery { loanDao.hasActiveLoan(1L, 1L) } returns 1

        // Act
        val result = loanRepository.hasActiveLoan(1L, 1L)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `hasActiveLoan debería retornar false si no existe loan activo`() = runTest {
        // Arrange
        coEvery { loanDao.hasActiveLoan(1L, 1L) } returns 0

        // Act
        val result = loanRepository.hasActiveLoan(1L, 1L)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `getOverdueLoans debería retornar loans vencidos`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-01", dueDate = "2024-01-15", returnDate = null, status = "overdue")
        )
        coEvery { loanDao.getOverdueLoans("2024-01-20") } returns loans

        // Act
        val result = loanRepository.getOverdueLoans("2024-01-20")

        // Assert
        assertEquals(1, result.size)
    }

    @Test
    fun `getLoansByDateRange debería retornar loans en rango de fechas`() = runTest {
        // Arrange
        val loans = listOf(
            LoanEntity(id = 1L, userId = 1L, bookId = 1L, loanDate = "2024-01-10", dueDate = "2024-01-15", returnDate = "2024-01-15", status = "active")
        )
        coEvery { loanDao.getLoansByDateRange("2024-01-01", "2024-01-31") } returns loans

        // Act
        val result = loanRepository.getLoansByDateRange("2024-01-01", "2024-01-31")

        // Assert
        assertEquals(1, result.size)
    }
}

