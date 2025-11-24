package com.empresa.libra_users.data.local.user

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests simples para LoanEntity.
 * Verificamos que los data classes funcionan correctamente.
 */
class LoanEntityTest {

    @Test
    fun `LoanEntity se crea correctamente con todos los campos`() {
        // Arrange & Act: Crear una entidad de préstamo
        val loan = LoanEntity(
            id = 1L,
            userId = 10L,
            bookId = 20L,
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = "2024-01-10",
            status = "Returned"
        )

        // Assert: Verificar que todos los campos se asignaron correctamente
        assertEquals(1L, loan.id)
        assertEquals(10L, loan.userId)
        assertEquals(20L, loan.bookId)
        assertEquals("2024-01-01", loan.loanDate)
        assertEquals("2024-01-15", loan.dueDate)
        assertEquals("2024-01-10", loan.returnDate)
        assertEquals("Returned", loan.status)
    }

    @Test
    fun `LoanEntity puede tener returnDate nulo cuando está activo`() {
        // Arrange & Act: Crear un préstamo activo (sin devolver)
        val loan = LoanEntity(
            id = 2L,
            userId = 11L,
            bookId = 21L,
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = null,
            status = "Active"
        )

        // Assert: Verificar que returnDate es nulo
        assertNull(loan.returnDate)
        assertEquals("Active", loan.status)
    }

    @Test
    fun `LoanEntity puede tener diferentes estados`() {
        val estados = listOf("Active", "Returned", "Overdue")

        estados.forEach { estado ->
            val loan = LoanEntity(
                id = 3L,
                userId = 12L,
                bookId = 22L,
                loanDate = "2024-01-01",
                dueDate = "2024-01-15",
                returnDate = if (estado == "Returned") "2024-01-10" else null,
                status = estado
            )

            assertEquals(estado, loan.status)
        }
    }

    @Test
    fun `LoanEntity puede tener diferentes fechas`() {
        val loan = LoanEntity(
            id = 4L,
            userId = 13L,
            bookId = 23L,
            loanDate = "2024-02-01",
            dueDate = "2024-02-20",
            returnDate = "2024-02-15",
            status = "Returned"
        )

        assertEquals("2024-02-01", loan.loanDate)
        assertEquals("2024-02-20", loan.dueDate)
        assertEquals("2024-02-15", loan.returnDate)
    }

    @Test
    fun `LoanEntity puede relacionar usuario y libro correctamente`() {
        val userId = 100L
        val bookId = 200L

        val loan = LoanEntity(
            id = 5L,
            userId = userId,
            bookId = bookId,
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = null,
            status = "Active"
        )

        assertEquals(userId, loan.userId)
        assertEquals(bookId, loan.bookId)
    }

    @Test
    fun `LoanEntity puede tener préstamo vencido sin devolver`() {
        val loan = LoanEntity(
            id = 6L,
            userId = 14L,
            bookId = 24L,
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = null, // Aún no devuelto
            status = "Overdue" // Pero vencido
        )

        assertEquals("Overdue", loan.status)
        assertNull(loan.returnDate) // No devuelto aún
    }
}

