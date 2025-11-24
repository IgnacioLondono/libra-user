package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.NotificationDao
import com.empresa.libra_users.data.local.user.NotificationEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NotificationRepositoryTest {

    private lateinit var notificationDao: NotificationDao
    private lateinit var notificationRepository: NotificationRepository

    @Before
    fun setup() {
        notificationDao = mockk(relaxed = true)
        notificationRepository = NotificationRepository(notificationDao)
    }

    @Test
    fun `create debería insertar notificación y retornar ID`() = runTest {
        // Arrange
        val notification = NotificationEntity(
            id = 0L,
            userId = 1L,
            loanId = null,
            title = "Test",
            message = "Test message",
            type = "INFO",
            isRead = false
        )
        coEvery { notificationDao.insert(any()) } returns 1L

        // Act
        val result = notificationRepository.create(
            userId = 1L,
            title = "Test",
            message = "Test message",
            type = "INFO"
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `notifyLoanCreated debería crear notificación de préstamo`() = runTest {
        // Arrange
        coEvery { notificationDao.insert(any()) } returns 1L

        // Act
        val result = notificationRepository.notifyLoanCreated(userId = 1L, loanId = 10L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `notifyLoanReminder debería crear notificación de recordatorio`() = runTest {
        // Arrange
        coEvery { notificationDao.insert(any()) } returns 1L

        // Act
        val result = notificationRepository.notifyLoanReminder(userId = 1L, loanId = 10L, daysLeft = 3)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `notifyLoanOverdue debería crear notificación de vencimiento`() = runTest {
        // Arrange
        coEvery { notificationDao.insert(any()) } returns 1L

        // Act
        val result = notificationRepository.notifyLoanOverdue(userId = 1L, loanId = 10L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `getAllByUser debería retornar todas las notificaciones del usuario`() = runTest {
        // Arrange
        val notifications = listOf(
            NotificationEntity(id = 1L, userId = 1L, title = "Test 1", message = "Message 1", type = "INFO", isRead = false),
            NotificationEntity(id = 2L, userId = 1L, title = "Test 2", message = "Message 2", type = "ALERT", isRead = false)
        )
        coEvery { notificationDao.getAllByUser(1L) } returns notifications

        // Act
        val result = notificationRepository.getAllByUser(1L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getUnreadByUser debería retornar notificaciones no leídas`() = runTest {
        // Arrange
        val notifications = listOf(
            NotificationEntity(id = 1L, userId = 1L, title = "Test 1", message = "Message 1", type = "INFO", isRead = false)
        )
        coEvery { notificationDao.getUnreadByUser(1L) } returns notifications

        // Act
        val result = notificationRepository.getUnreadByUser(1L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `countUnread debería retornar número de no leídas`() = runTest {
        // Arrange
        coEvery { notificationDao.countUnreadByUser(1L) } returns 5

        // Act
        val result = notificationRepository.countUnread(1L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull())
    }

    @Test
    fun `markAsRead debería marcar notificación como leída`() = runTest {
        // Arrange
        coEvery { notificationDao.markAsRead(1L) } returns 1

        // Act
        val result = notificationRepository.markAsRead(1L)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `markAllAsRead debería marcar todas como leídas`() = runTest {
        // Arrange
        coEvery { notificationDao.markAllAsRead(1L) } returns 2

        // Act
        val result = notificationRepository.markAllAsRead(1L)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `delete debería eliminar notificación`() = runTest {
        // Arrange
        coEvery { notificationDao.deleteById(1L) } returns 1

        // Act
        val result = notificationRepository.delete(1L)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteAllForUser debería eliminar todas las notificaciones del usuario`() = runTest {
        // Arrange
        coEvery { notificationDao.deleteAllByUser(1L) } returns 5

        // Act
        val result = notificationRepository.deleteAllForUser(1L)

        // Assert
        assertTrue(result.isSuccess)
    }
}

