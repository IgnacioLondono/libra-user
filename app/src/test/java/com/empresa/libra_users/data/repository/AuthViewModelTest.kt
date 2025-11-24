package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests simples para AuthViewModel.
 * Estos tests verifican la lógica de autenticación de forma fácil de entender.
 */
class AuthViewModelTest {

    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        // Configurar el test dispatcher para viewModelScope
        Dispatchers.setMain(StandardTestDispatcher())
        
        // Crear mock del repositorio
        mockAuthRepository = mockk(relaxed = true)
        viewModel = AuthViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        // Limpiar el dispatcher después de cada test
        Dispatchers.resetMain()
    }

    // ==================== TESTS DE LOGIN ====================

    @Test
    fun `onLoginEmailChange actualiza el email y valida`() = runTest {
        // Act: Cambiar el email
        viewModel.onLoginEmailChange("usuario@gmail.com")

        // Assert: Verificar que el email se actualizó
        val loginState = viewModel.login.value
        assertEquals("usuario@gmail.com", loginState.email)
        assertNull(loginState.emailError) // Email válido no debe tener error
    }

    @Test
    fun `onLoginEmailChange muestra error cuando el email es inválido`() = runTest {
        // Act: Cambiar a un email inválido
        viewModel.onLoginEmailChange("email-invalido")

        // Assert: Verificar que hay un error
        val loginState = viewModel.login.value
        assertNotNull(loginState.emailError)
    }

    @Test
    fun `onLoginPassChange actualiza la contraseña`() = runTest {
        // Act: Cambiar la contraseña
        viewModel.onLoginPassChange("password123")

        // Assert: Verificar que la contraseña se actualizó
        val loginState = viewModel.login.value
        assertEquals("password123", loginState.pass)
    }

    @Test
    fun `recomputeLoginCanSubmit habilita el botón cuando los campos son válidos`() = runTest {
        // Arrange: Establecer email y contraseña válidos
        viewModel.onLoginEmailChange("usuario@gmail.com")
        viewModel.onLoginPassChange("password123")

        // Assert: Verificar que canSubmit es true
        val loginState = viewModel.login.value
        assertTrue(loginState.canSubmit)
    }

    @Test
    fun `recomputeLoginCanSubmit deshabilita el botón cuando hay errores`() = runTest {
        // Arrange: Establecer email inválido
        viewModel.onLoginEmailChange("email-invalido")
        viewModel.onLoginPassChange("password123")

        // Assert: Verificar que canSubmit es false
        val loginState = viewModel.login.value
        assertFalse(loginState.canSubmit)
    }

    @Test
    fun `submitLogin llama al repositorio con las credenciales correctas`() = runTest {
        // Arrange: Configurar el mock para que retorne éxito (sin delay para tests rápidos)
        coEvery { mockAuthRepository.login(any(), any()) } returns Result.success("user")

        // Arrange: Establecer credenciales válidas
        viewModel.onLoginEmailChange("usuario@gmail.com")
        viewModel.onLoginPassChange("password123")

        // Act: Intentar hacer login
        viewModel.submitLogin()

        // Avanzar el tiempo hasta que todas las corrutinas terminen
        advanceUntilIdle()

        // Assert: Verificar que el login fue exitoso
        val loginState = viewModel.login.value
        assertTrue("El login debería ser exitoso", loginState.success)
        assertNull("No debería haber mensaje de error", loginState.errorMsg)
    }

    @Test
    fun `submitLogin muestra error cuando las credenciales son inválidas`() = runTest {
        // Arrange: Configurar el mock para que retorne error (sin delay para tests rápidos)
        coEvery { mockAuthRepository.login(any(), any()) } returns Result.failure(Exception("Credenciales inválidas"))

        // Arrange: Establecer credenciales
        viewModel.onLoginEmailChange("usuario@gmail.com")
        viewModel.onLoginPassChange("password123")

        // Act: Intentar hacer login
        viewModel.submitLogin()

        // Avanzar el tiempo hasta que todas las corrutinas terminen
        advanceUntilIdle()

        // Assert: Verificar que hay un error
        val loginState = viewModel.login.value
        assertFalse("El login no debería ser exitoso", loginState.success)
        assertNotNull("Debería haber un mensaje de error", loginState.errorMsg)
        assertEquals("Credenciales inválidas", loginState.errorMsg)
    }

    @Test
    fun `clearLoginResult limpia el resultado del login`() = runTest {
        // Act: Limpiar resultado
        viewModel.clearLoginResult()

        // Assert: Verificar que se limpió
        val loginState = viewModel.login.value
        assertFalse(loginState.success)
        assertNull(loginState.errorMsg)
    }

    // ==================== TESTS DE REGISTRO ====================

    @Test
    fun `onNameChange actualiza el nombre y filtra caracteres inválidos`() = runTest {
        // Act: Cambiar el nombre con números (debería filtrarlos)
        viewModel.onNameChange("Juan123 Pérez")

        // Assert: Verificar que el nombre se actualizó y los números fueron filtrados
        val registerState = viewModel.register.value
        assertEquals("Juan Pérez", registerState.name) // Los números deberían ser filtrados
    }

    @Test
    fun `onRegisterEmailChange actualiza el email y valida`() = runTest {
        // Act: Cambiar el email
        viewModel.onRegisterEmailChange("usuario@gmail.com")

        // Assert: Verificar que el email se actualizó
        val registerState = viewModel.register.value
        assertEquals("usuario@gmail.com", registerState.email)
    }

    @Test
    fun `onPhoneChange filtra solo dígitos`() = runTest {
        // Act: Cambiar el teléfono con letras (debería filtrarlas)
        viewModel.onPhoneChange("123abc456")

        // Assert: Verificar que solo quedaron los dígitos
        val registerState = viewModel.register.value
        assertEquals("123456", registerState.phone)
    }

    @Test
    fun `onRegisterPassChange actualiza la contraseña y valida`() = runTest {
        // Act: Cambiar la contraseña
        viewModel.onRegisterPassChange("Password123!")

        // Assert: Verificar que la contraseña se actualizó
        val registerState = viewModel.register.value
        assertEquals("Password123!", registerState.pass)
    }

    @Test
    fun `onConfirmChange valida que las contraseñas coincidan`() = runTest {
        // Arrange: Establecer una contraseña
        viewModel.onRegisterPassChange("Password123!")

        // Act: Cambiar la confirmación con la misma contraseña
        viewModel.onConfirmChange("Password123!")

        // Assert: Verificar que no hay error
        val registerState = viewModel.register.value
        assertNull(registerState.confirmError)
    }

    @Test
    fun `onConfirmChange muestra error cuando las contraseñas no coinciden`() = runTest {
        // Arrange: Establecer una contraseña
        viewModel.onRegisterPassChange("Password123!")

        // Act: Cambiar la confirmación con una contraseña diferente
        viewModel.onConfirmChange("Password456!")

        // Assert: Verificar que hay un error
        val registerState = viewModel.register.value
        assertNotNull(registerState.confirmError)
    }

    @Test
    fun `recomputeRegisterCanSubmit habilita el botón cuando todos los campos son válidos`() = runTest {
        // Arrange: Establecer todos los campos válidos
        viewModel.onNameChange("Juan Pérez")
        viewModel.onRegisterEmailChange("usuario@gmail.com")
        viewModel.onPhoneChange("12345678")
        viewModel.onRegisterPassChange("Password123!")
        viewModel.onConfirmChange("Password123!")

        // Assert: Verificar que canSubmit es true
        val registerState = viewModel.register.value
        assertTrue(registerState.canSubmit)
    }

    @Test
    fun `recomputeRegisterCanSubmit deshabilita el botón cuando hay errores`() = runTest {
        // Arrange: Establecer campos con errores
        viewModel.onNameChange("") // Nombre vacío
        viewModel.onRegisterEmailChange("email-invalido")
        viewModel.onPhoneChange("123")
        viewModel.onRegisterPassChange("pass") // Contraseña muy corta
        viewModel.onConfirmChange("pass2") // Contraseñas no coinciden

        // Assert: Verificar que canSubmit es false
        val registerState = viewModel.register.value
        assertFalse(registerState.canSubmit)
    }

    @Test
    fun `clearRegisterResult limpia el resultado del registro`() = runTest {
        // Act: Limpiar resultado
        viewModel.clearRegisterResult()

        // Assert: Verificar que se limpió
        val registerState = viewModel.register.value
        assertFalse(registerState.success)
        assertNull(registerState.errorMsg)
    }
}

