package com.empresa.libra_users.data.remote.mapper

import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.remote.dto.LoanDto
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests simples para el mapeo de LoanDto a LoanEntity.
 */
class LoanMapperTest {

    @Test
    fun `toEntity convierte LoanDto a LoanEntity correctamente`() {
        // Arrange: Crear un LoanDto de ejemplo
        val loanDto = LoanDto(
            id = "1",
            userId = "10",
            bookId = "20",
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = null,
            status = "Active",
            loanDays = 14,
            fineAmount = null,
            extensionsCount = 0,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        // Act: Convertir a Entity usando la función de extensión dentro del contexto de LoanMapper
        val loanEntity = with(LoanMapper) {
            loanDto.toEntity()
        }

        // Assert: Verificar que los campos se mapearon correctamente
        assertEquals(1L, loanEntity.id)
        assertEquals(10L, loanEntity.userId)
        assertEquals(20L, loanEntity.bookId)
        assertEquals("2024-01-01", loanEntity.loanDate)
        assertEquals("2024-01-15", loanEntity.dueDate)
        assertNull(loanEntity.returnDate)
        assertEquals("Active", loanEntity.status)
    }

    @Test
    fun `toEntity maneja préstamo devuelto correctamente`() {
        val loanDto = LoanDto(
            id = "2",
            userId = "11",
            bookId = "21",
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = "2024-01-10",
            status = "Returned",
            loanDays = 14,
            fineAmount = 0.0,
            extensionsCount = 0,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-10T00:00:00"
        )

        val loanEntity = with(LoanMapper) {
            loanDto.toEntity()
        }

        assertEquals("2024-01-10", loanEntity.returnDate)
        assertEquals("Returned", loanEntity.status)
    }

    @Test
    fun `toEntity maneja IDs inválidos correctamente`() {
        val loanDto = LoanDto(
            id = "invalid",
            userId = "invalid",
            bookId = "invalid",
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = null,
            status = "Active",
            loanDays = 14,
            fineAmount = null,
            extensionsCount = 0,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        val loanEntity = with(LoanMapper) {
            loanDto.toEntity()
        }

        assertEquals(0L, loanEntity.id) // ID inválido se convierte a 0
        assertEquals(0L, loanEntity.userId) // UserID inválido se convierte a 0
        assertEquals(0L, loanEntity.bookId) // BookID inválido se convierte a 0
    }

    @Test
    fun `toEntity mapea préstamo vencido correctamente`() {
        val loanDto = LoanDto(
            id = "3",
            userId = "12",
            bookId = "22",
            loanDate = "2024-01-01",
            dueDate = "2024-01-15",
            returnDate = null,
            status = "Overdue",
            loanDays = 14,
            fineAmount = 5.50,
            extensionsCount = 1,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-16T00:00:00"
        )

        val loanEntity = with(LoanMapper) {
            loanDto.toEntity()
        }

        assertEquals("Overdue", loanEntity.status)
        assertNull(loanEntity.returnDate) // Aún no devuelto
    }
}

