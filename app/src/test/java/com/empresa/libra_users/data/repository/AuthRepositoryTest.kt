package com.empresa.libra_users.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        authRepository = AuthRepository()
    }

    @Test
    fun `login con credenciales válidas de demo debería retornar USER`() = runTest {
        // Act
        val result = authRepository.login("demo@duoc.cl", "Demo123!")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("USER", result.getOrNull())
    }

    @Test
    fun `login con credenciales válidas de admin debería retornar ADMIN`() = runTest {
        // Act
        val result = authRepository.login("admin123@gmail.com", "admin12345678")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("ADMIN", result.getOrNull())
    }

    @Test
    fun `login con credenciales inválidas debería retornar error`() = runTest {
        // Act
        val result = authRepository.login("invalid@test.com", "wrongpassword")

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("inválidas") == true)
    }

    @Test
    fun `login con email correcto pero contraseña incorrecta debería retornar error`() = runTest {
        // Act
        val result = authRepository.login("demo@duoc.cl", "wrongpassword")

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `register con nuevo usuario debería retornar éxito`() = runTest {
        // Act
        val result = authRepository.register(
            DemoUser(
                name = "New User",
                email = "newuser@test.com",
                phone = "987654321",
                pass = "NewPass123!"
            )
        )

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `register con email duplicado debería retornar error`() = runTest {
        // Act - Intentar registrar con email existente
        val result = authRepository.register(
            DemoUser(
                name = "Another User",
                email = "demo@duoc.cl",  // Email ya existe
                phone = "111111111",
                pass = "AnotherPass123!"
            )
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("ya está en uso") == true)
    }

    @Test
    fun `login debería ser case insensitive para email`() = runTest {
        // Act
        val result = authRepository.login("DEMO@DUOC.CL", "Demo123!")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("USER", result.getOrNull())
    }
}

