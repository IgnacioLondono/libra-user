package com.empresa.libra_users.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      TESTS DE AuthRepository                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ Este archivo contiene pruebas unitarias para el repositorio de autenticación ║
 * ║                                                                               ║
 * ║ ¿QUÉ SE PRUEBA?                                                              ║
 * ║ • Login de usuarios (demo y admin)                                           ║
 * ║ • Registro de nuevos usuarios                                                ║
 * ║ • Validación de credenciales                                                 ║
 * ║ • Manejo de errores (contraseña incorrecta, email duplicado, etc.)          ║
 * ║                                                                               ║
 * ║ PATRÓN UTILIZADO: AAA (Arrange-Act-Assert)                                   ║
 * ║ • Arrange: Preparar los datos necesarios                                     ║
 * ║ • Act: Ejecutar la acción a probar                                           ║
 * ║ • Assert: Verificar el resultado esperado                                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
class AuthRepositoryTest {

    // Repositorio que vamos a probar
    private lateinit var authRepository: AuthRepository

    /**
     * SETUP: Se ejecuta ANTES de cada test.
     * Crea una nueva instancia del repositorio para que cada test
     * comience con un estado limpio y no se afecten entre sí.
     */
    @Before
    fun setup() {
        authRepository = AuthRepository()
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                          TESTS DE LOGIN                                 ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Login exitoso con usuario demo
     * 
     * ESCENARIO: Un usuario normal intenta iniciar sesión con credenciales válidas
     * RESULTADO ESPERADO: El login retorna "USER" indicando que es un usuario regular
     */
    @Test
    fun `login con credenciales válidas de demo debería retornar USER`() = runTest {
        // Act - Ejecutar el login con las credenciales del usuario demo
        val result = authRepository.login("demo@duoc.cl", "Demo123!")

        // Assert - Verificar que el login fue exitoso y retorna rol "USER"
        assertTrue(result.isSuccess)
        assertEquals("USER", result.getOrNull())
    }

    /**
     * TEST: Login exitoso con usuario administrador
     * 
     * ESCENARIO: El administrador intenta iniciar sesión
     * RESULTADO ESPERADO: El login retorna "ADMIN" para dar acceso al panel de admin
     */
    @Test
    fun `login con credenciales válidas de admin debería retornar ADMIN`() = runTest {
        // Act - Ejecutar el login con las credenciales del administrador
        val result = authRepository.login("admin123@gmail.com", "admin12345678")

        // Assert - Verificar que retorna rol "ADMIN"
        assertTrue(result.isSuccess)
        assertEquals("ADMIN", result.getOrNull())
    }

    /**
     * TEST: Login fallido con credenciales inválidas
     * 
     * ESCENARIO: Alguien intenta entrar con un email y contraseña que no existen
     * RESULTADO ESPERADO: El login falla y muestra mensaje de "credenciales inválidas"
     */
    @Test
    fun `login con credenciales inválidas debería retornar error`() = runTest {
        // Act - Intentar login con credenciales que no existen
        val result = authRepository.login("invalid@test.com", "wrongpassword")

        // Assert - Verificar que falló y el mensaje contiene "inválidas"
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("inválidas") == true)
    }

    /**
     * TEST: Login fallido por contraseña incorrecta
     * 
     * ESCENARIO: El email existe pero la contraseña es incorrecta
     * RESULTADO ESPERADO: El login debe fallar (no permitir acceso)
     */
    @Test
    fun `login con email correcto pero contraseña incorrecta debería retornar error`() = runTest {
        // Act - Email correcto (demo@duoc.cl) pero contraseña incorrecta
        val result = authRepository.login("demo@duoc.cl", "wrongpassword")

        // Assert - Debe fallar aunque el email exista
        assertTrue(result.isFailure)
    }

    /**
     * TEST: Login es case-insensitive para emails
     * 
     * ESCENARIO: El usuario escribe el email en MAYÚSCULAS
     * RESULTADO ESPERADO: El login funciona igual (los emails no distinguen mayúsculas)
     */
    @Test
    fun `login debería ser case insensitive para email`() = runTest {
        // Act - Email en MAYÚSCULAS
        val result = authRepository.login("DEMO@DUOC.CL", "Demo123!")

        // Assert - Debe funcionar igual que con minúsculas
        assertTrue(result.isSuccess)
        assertEquals("USER", result.getOrNull())
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                        TESTS DE REGISTRO                                ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Registro exitoso de nuevo usuario
     * 
     * ESCENARIO: Un nuevo usuario se registra con datos válidos
     * RESULTADO ESPERADO: El registro es exitoso y el usuario queda guardado
     */
    @Test
    fun `register con nuevo usuario debería retornar éxito`() = runTest {
        // Act - Registrar un usuario nuevo con email que no existe
        val result = authRepository.register(
            DemoUser(
                name = "New User",
                email = "newuser@test.com",
                phone = "987654321",
                pass = "NewPass123!"
            )
        )

        // Assert - El registro debe ser exitoso
        assertTrue(result.isSuccess)
    }

    /**
     * TEST: Registro fallido por email duplicado
     * 
     * ESCENARIO: Alguien intenta registrarse con un email que ya existe
     * RESULTADO ESPERADO: El registro falla con mensaje "ya está en uso"
     * 
     * NOTA: Esto previene que dos usuarios tengan el mismo email
     */
    @Test
    fun `register con email duplicado debería retornar error`() = runTest {
        // Act - Intentar registrar con email que ya existe (demo@duoc.cl)
        val result = authRepository.register(
            DemoUser(
                name = "Another User",
                email = "demo@duoc.cl",  // ⚠️ Este email ya existe en el sistema
                phone = "111111111",
                pass = "AnotherPass123!"
            )
        )

        // Assert - Debe fallar porque el email está duplicado
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("ya está en uso") == true)
    }
}

