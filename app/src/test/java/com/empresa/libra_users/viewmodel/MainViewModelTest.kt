package com.empresa.libra_users.viewmodel

import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests simples para MainViewModel.
 * Estos tests verifican la lógica de negocio de forma fácil de entender.
 */
class MainViewModelTest {

    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockBookRepository: BookRepository
    private lateinit var mockLoanRepository: LoanRepository
    private lateinit var mockNotificationRepository: NotificationRepository
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        // Crear mocks de todos los repositorios
        mockUserRepository = mockk(relaxed = true)
        mockBookRepository = mockk(relaxed = true)
        mockLoanRepository = mockk(relaxed = true)
        mockNotificationRepository = mockk(relaxed = true)
        mockUserPreferencesRepository = mockk(relaxed = true)

        // Configurar valores por defecto
        every { mockUserPreferencesRepository.userEmail } returns flowOf(null)
        every { mockBookRepository.getAllBooks() } returns flowOf(emptyList())
        coEvery { mockBookRepository.count() } returns 0

        // Crear el ViewModel
        viewModel = MainViewModel(
            mockUserRepository,
            mockBookRepository,
            mockLoanRepository,
            mockNotificationRepository,
            mockUserPreferencesRepository
        )
    }

    // ==================== TESTS DE CARRITO ====================

    @Test
    fun `addToCart agrega un libro al carrito`() = runTest {
        // Arrange: Crear un libro de ejemplo
        val book = createTestBook(id = 1L, title = "El Quijote")

        // Act: Agregar al carrito
        viewModel.addToCart(book)

        // Assert: Verificar que el libro está en el carrito
        val cart = viewModel.cart.value
        assertEquals(1, cart.size)
        assertEquals(book.id, cart[0].book.id)
    }

    @Test
    fun `addToCart no agrega el mismo libro dos veces`() = runTest {
        // Arrange: Crear un libro
        val book = createTestBook(id = 1L, title = "El Quijote")

        // Act: Agregar el mismo libro dos veces
        viewModel.addToCart(book)
        viewModel.addToCart(book)

        // Assert: Verificar que solo está una vez
        val cart = viewModel.cart.value
        assertEquals(1, cart.size)
    }

    @Test
    fun `removeFromCart elimina un libro del carrito`() = runTest {
        // Arrange: Agregar un libro al carrito
        val book = createTestBook(id = 1L, title = "El Quijote")
        viewModel.addToCart(book)

        // Act: Eliminar del carrito
        viewModel.removeFromCart(book.id)

        // Assert: Verificar que el carrito está vacío
        val cart = viewModel.cart.value
        assertTrue(cart.isEmpty())
    }

    @Test
    fun `updateLoanDays actualiza los días de préstamo en el carrito`() = runTest {
        // Arrange: Agregar un libro al carrito
        val book = createTestBook(id = 1L, title = "El Quijote")
        viewModel.addToCart(book)

        // Act: Actualizar los días de préstamo
        viewModel.updateLoanDays(book.id, 14)

        // Assert: Verificar que los días se actualizaron
        val cart = viewModel.cart.value
        assertEquals(14, cart[0].loanDays)
    }

    @Test
    fun `updateLoanDays limita los días entre 1 y 30`() = runTest {
        // Arrange: Agregar un libro al carrito
        val book = createTestBook(id = 1L, title = "El Quijote")
        viewModel.addToCart(book)

        // Act: Intentar establecer días fuera del rango
        viewModel.updateLoanDays(book.id, 50) // Debería limitarse a 30
        val cart1 = viewModel.cart.value
        assertEquals(30, cart1[0].loanDays)

        viewModel.updateLoanDays(book.id, 0) // Debería limitarse a 1
        val cart2 = viewModel.cart.value
        assertEquals(1, cart2[0].loanDays)
    }

    // ==================== TESTS DE BÚSQUEDA ====================

    @Test
    fun `onSearchQueryChange actualiza la query de búsqueda`() = runTest {
        // Act: Cambiar la query
        viewModel.onSearchQueryChange("El Quijote")

        // Assert: Verificar que la query se actualizó
        val searchState = viewModel.search.value
        assertEquals("El Quijote", searchState.query)
    }

    @Test
    fun `clearSearchResults limpia los resultados de búsqueda`() = runTest {
        // Arrange: Establecer una búsqueda
        viewModel.onSearchQueryChange("test")

        // Act: Limpiar resultados
        viewModel.clearSearchResults()

        // Assert: Verificar que se limpió
        val searchState = viewModel.search.value
        assertEquals("", searchState.query)
        assertTrue(searchState.results.isEmpty())
    }

    // ==================== TESTS DE LOGIN ====================

    @Test
    fun `onLoginEmailChange actualiza el email`() = runTest {
        // Act: Cambiar el email
        viewModel.onLoginEmailChange("usuario@gmail.com")

        // Assert: Verificar que el email se actualizó
        val loginState = viewModel.login.value
        assertEquals("usuario@gmail.com", loginState.email)
    }

    @Test
    fun `onLoginEmailChange valida el formato del email`() = runTest {
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
    fun `clearLoginResult limpia el resultado del login`() = runTest {
        // Arrange: Simular un login exitoso (modificando directamente el estado)
        // Nota: En un test real, esto se haría a través de submitLogin

        // Act: Limpiar resultado
        viewModel.clearLoginResult()

        // Assert: Verificar que se limpió
        val loginState = viewModel.login.value
        assertFalse(loginState.success)
        assertNull(loginState.errorMsg)
    }

    // ==================== TESTS DE REGISTRO ====================

    @Test
    fun `onRegisterNameChange actualiza el nombre`() = runTest {
        // Act: Cambiar el nombre
        viewModel.onRegisterNameChange("Juan Pérez")

        // Assert: Verificar que el nombre se actualizó
        val registerState = viewModel.register.value
        assertEquals("Juan Pérez", registerState.name)
    }

    @Test
    fun `onRegisterEmailChange actualiza el email`() = runTest {
        // Act: Cambiar el email
        viewModel.onRegisterEmailChange("usuario@gmail.com")

        // Assert: Verificar que el email se actualizó
        val registerState = viewModel.register.value
        assertEquals("usuario@gmail.com", registerState.email)
    }

    @Test
    fun `onRegisterPhoneChange actualiza el teléfono`() = runTest {
        // Act: Cambiar el teléfono
        viewModel.onRegisterPhoneChange("12345678")

        // Assert: Verificar que el teléfono se actualizó
        val registerState = viewModel.register.value
        assertEquals("12345678", registerState.phone)
    }

    @Test
    fun `onRegisterPassChange actualiza la contraseña`() = runTest {
        // Act: Cambiar la contraseña
        viewModel.onRegisterPassChange("Password123!")

        // Assert: Verificar que la contraseña se actualizó
        val registerState = viewModel.register.value
        assertEquals("Password123!", registerState.pass)
    }

    @Test
    fun `onRegisterConfirmChange actualiza la confirmación`() = runTest {
        // Arrange: Establecer una contraseña
        viewModel.onRegisterPassChange("Password123!")

        // Act: Cambiar la confirmación
        viewModel.onRegisterConfirmChange("Password123!")

        // Assert: Verificar que la confirmación se actualizó
        val registerState = viewModel.register.value
        assertEquals("Password123!", registerState.confirm)
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

    // ==================== TESTS DE DARK MODE ====================

    @Test
    fun `toggleDarkMode cambia el estado del modo oscuro`() = runTest {
        // Arrange: Obtener el estado inicial
        val initialState = viewModel.isDarkMode.value

        // Act: Cambiar el modo oscuro
        viewModel.toggleDarkMode()

        // Assert: Verificar que cambió
        val newState = viewModel.isDarkMode.value
        assertEquals(!initialState, newState)
    }

    // ==================== FUNCIONES AUXILIARES ====================

    private fun createTestBook(
        id: Long = 1L,
        title: String = "Test Book",
        author: String = "Test Author",
        isbn: String = "1234567890",
        categoryId: Long = 1L,
        categoria: String = "Ficción",
        publisher: String = "Test Publisher",
        publishDate: String = "2020-01-01",
        anio: Int = 2020,
        status: String = "Available",
        inventoryCode: String = "TEST-001",
        stock: Int = 10,
        disponibles: Int = 5
    ): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            author = author,
            isbn = isbn,
            categoryId = categoryId,
            categoria = categoria,
            publisher = publisher,
            publishDate = publishDate,
            anio = anio,
            status = status,
            inventoryCode = inventoryCode,
            stock = stock,
            disponibles = disponibles
        )
    }
}

