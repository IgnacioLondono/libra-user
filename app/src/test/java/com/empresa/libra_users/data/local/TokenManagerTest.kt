package com.empresa.libra_users.data.local

import com.empresa.libra_users.data.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests simples para TokenManager usando mocks.
 * Estos tests son fáciles de entender para estudiantes.
 */
class TokenManagerTest {

    // Mock del repositorio (simulamos su comportamiento)
    private lateinit var mockRepository: UserPreferencesRepository
    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        // Crear un mock del repositorio antes de cada test (relaxed para evitar errores)
        mockRepository = mockk(relaxed = true)
        // Configurar el Flow por defecto para que no falle
        every { mockRepository.authToken } returns flowOf(null)
        tokenManager = TokenManager(mockRepository)
    }

    @Test
    fun `saveToken guarda el token correctamente`() = runTest {
        // Arrange: Configurar el mock para que no haga nada cuando se llame saveAuthToken
        coEvery { mockRepository.saveAuthToken(any()) } returns Unit

        // Act: Guardar un token
        tokenManager.saveToken("mi-token-123")

        // Assert: Verificar que se llamó al método del repositorio
        coVerify(exactly = 1) { mockRepository.saveAuthToken("mi-token-123") }
    }

    @Test
    fun `getToken retorna el token cuando existe`() = runTest {
        // Arrange: Configurar el mock para que retorne un token
        coEvery { mockRepository.getAuthToken() } returns "mi-token-123"

        // Act: Obtener el token
        val token = tokenManager.getToken()

        // Assert: Verificar que se obtuvo el token correcto
        assertEquals("mi-token-123", token)
        coVerify(exactly = 1) { mockRepository.getAuthToken() }
    }

    @Test
    fun `getToken retorna null cuando no existe`() = runTest {
        // Arrange: Configurar el mock para que retorne null
        coEvery { mockRepository.getAuthToken() } returns null

        // Act: Obtener el token
        val token = tokenManager.getToken()

        // Assert: Verificar que retorna null
        assertNull(token)
    }

    @Test
    fun `getBearerToken retorna el token con formato Bearer`() = runTest {
        // Arrange: Configurar el mock para que retorne un token
        coEvery { mockRepository.getBearerToken() } returns "Bearer mi-token-123"

        // Act: Obtener el token con formato Bearer
        val bearerToken = tokenManager.getBearerToken()

        // Assert: Verificar que tiene el formato correcto
        assertEquals("Bearer mi-token-123", bearerToken)
        coVerify(exactly = 1) { mockRepository.getBearerToken() }
    }

    @Test
    fun `getBearerToken retorna null cuando no hay token`() = runTest {
        // Arrange: Configurar el mock para que retorne null
        coEvery { mockRepository.getBearerToken() } returns null

        // Act: Obtener el token con formato Bearer
        val bearerToken = tokenManager.getBearerToken()

        // Assert: Verificar que retorna null
        assertNull(bearerToken)
    }

    @Test
    fun `clearToken limpia el token correctamente`() = runTest {
        // Arrange: Configurar el mock
        coEvery { mockRepository.clearAll() } returns Unit

        // Act: Limpiar el token
        tokenManager.clearToken()

        // Assert: Verificar que se llamó al método de limpieza
        coVerify(exactly = 1) { mockRepository.clearAll() }
    }

    @Test
    fun `hasToken retorna true cuando existe un token`() = runTest {
        // Arrange: Configurar el mock para que retorne un token
        coEvery { mockRepository.getAuthToken() } returns "mi-token-123"

        // Act: Verificar si hay token
        val hasToken = tokenManager.hasToken()

        // Assert: Verificar que retorna true
        assertTrue(hasToken)
    }

    @Test
    fun `hasToken retorna false cuando no existe un token`() = runTest {
        // Arrange: Configurar el mock para que retorne null
        coEvery { mockRepository.getAuthToken() } returns null

        // Act: Verificar si hay token
        val hasToken = tokenManager.hasToken()

        // Assert: Verificar que retorna false
        assertFalse(hasToken)
    }

    @Test
    fun `token Flow emite el token correctamente`() = runTest {
        // Arrange: Configurar el Flow del mock y recrear el TokenManager
        every { mockRepository.authToken } returns flowOf("mi-token-123")
        val manager = TokenManager(mockRepository)

        // Act: Obtener el Flow del token
        val tokenFlow = manager.token

        // Assert: Verificar que el Flow emite el token correcto
        val token = tokenFlow.first()
        assertEquals("mi-token-123", token)
    }

    @Test
    fun `token Flow emite null cuando no hay token`() = runTest {
        // Arrange: Configurar el Flow del mock para que emita null y recrear el TokenManager
        every { mockRepository.authToken } returns flowOf(null)
        val manager = TokenManager(mockRepository)

        // Act: Obtener el Flow del token
        val tokenFlow = manager.token

        // Assert: Verificar que el Flow emite null
        val token = tokenFlow.first()
        assertNull(token)
    }
}

