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
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      TESTS DE MainViewModel                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ MainViewModel es el CEREBRO de la app. Controla:                              ║
 * ║ • 🛒 Carrito de compras (agregar, eliminar, modificar días de préstamo)       ║
 * ║ • 🔍 Búsqueda de libros                                                       ║
 * ║ • 🔐 Login y Registro de usuarios                                            ║
 * ║ • 🌙 Modo oscuro                                                              ║
 * ║ • 👤 Datos del usuario actual                                                 ║
 * ║                                                                               ║
 * ║ ESTRATEGIA DE TESTING:                                                        ║
 * ║ • Usamos MockK para simular los repositorios (no usar datos reales)          ║
 * ║ • Cada test es independiente (setup limpia todo)                             ║
 * ║ • Probamos la lógica del ViewModel, no los repositorios                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
class MainViewModelTest {

    // ─────────────────────────────────────────────────────────────────────────
    // MOCKS: Simulaciones de las dependencias del ViewModel
    // ─────────────────────────────────────────────────────────────────────────
    private lateinit var mockUserRepository: UserRepository          // Maneja usuarios
    private lateinit var mockBookRepository: BookRepository          // Maneja libros
    private lateinit var mockLoanRepository: LoanRepository          // Maneja préstamos
    private lateinit var mockNotificationRepository: NotificationRepository  // Notificaciones
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository // Preferencias
    
    // El ViewModel que vamos a probar
    private lateinit var viewModel: MainViewModel

    /**
     * SETUP: Se ejecuta ANTES de cada @Test
     * 
     * Crea mocks "relaxed" (retornan valores por defecto si no se configura nada)
     * y configura el estado inicial del ViewModel
     */
    @Before
    fun setup() {
        // Crear mocks de todos los repositorios
        mockUserRepository = mockk(relaxed = true)
        mockBookRepository = mockk(relaxed = true)
        mockLoanRepository = mockk(relaxed = true)
        mockNotificationRepository = mockk(relaxed = true)
        mockUserPreferencesRepository = mockk(relaxed = true)

        // Configurar valores por defecto para evitar errores
        every { mockUserPreferencesRepository.userEmail } returns flowOf(null)  // Usuario no logueado
        every { mockBookRepository.getAllBooks() } returns flowOf(emptyList())  // Sin libros
        coEvery { mockBookRepository.count() } returns 0                         // 0 libros en BD

        // Crear el ViewModel con todas las dependencias mockeadas
        viewModel = MainViewModel(
            mockUserRepository,
            mockBookRepository,
            mockLoanRepository,
            mockNotificationRepository,
            mockUserPreferencesRepository
        )
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                    🛒 TESTS DE CARRITO DE COMPRAS                       ║
    // ║  El carrito permite al usuario seleccionar libros antes de confirmar    ║
    // ║  el préstamo. Cada item tiene: libro + días de préstamo                 ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Agregar libro al carrito
     * 
     * ESCENARIO: Usuario encuentra un libro interesante y lo agrega al carrito
     * RESULTADO: El libro aparece en la lista del carrito con 7 días por defecto
     */
    @Test
    fun `addToCart agrega un libro al carrito`() = runTest {
        // Arrange - Crear un libro de ejemplo
        val book = createTestBook(id = 1L, title = "El Quijote")

        // Act - Agregar al carrito
        viewModel.addToCart(book)

        // Assert - Verificar que el libro está en el carrito
        val cart = viewModel.cart.value
        assertEquals(1, cart.size)
        assertEquals(book.id, cart[0].book.id)
    }

    /**
     * TEST: No duplicar libros en el carrito
     * 
     * ESCENARIO: Usuario hace clic en "Agregar" dos veces por accidente
     * RESULTADO: El libro solo aparece una vez (previene duplicados)
     * 
     * ⚠️ REGLA DE NEGOCIO: Un usuario solo puede tener 1 copia de cada libro
     */
    @Test
    fun `addToCart no agrega el mismo libro dos veces`() = runTest {
        // Arrange - Crear un libro
        val book = createTestBook(id = 1L, title = "El Quijote")

        // Act - Agregar el mismo libro DOS veces
        viewModel.addToCart(book)
        viewModel.addToCart(book)

        // Assert - Verificar que solo está UNA vez
        val cart = viewModel.cart.value
        assertEquals(1, cart.size)
    }

    /**
     * TEST: Eliminar libro del carrito
     * 
     * ESCENARIO: Usuario se arrepiente y quita un libro del carrito
     * RESULTADO: El libro desaparece del carrito
     */
    @Test
    fun `removeFromCart elimina un libro del carrito`() = runTest {
        // Arrange - Agregar un libro primero
        val book = createTestBook(id = 1L, title = "El Quijote")
        viewModel.addToCart(book)

        // Act - Eliminar del carrito
        viewModel.removeFromCart(book.id)

        // Assert - El carrito debe estar vacío
        val cart = viewModel.cart.value
        assertTrue(cart.isEmpty())
    }

    /**
     * TEST: Modificar días de préstamo
     * 
     * ESCENARIO: Usuario quiere el libro por 14 días en vez de 7
     * RESULTADO: Los días se actualizan y el precio se recalcula
     * 
     * NOTA: Precio = días × $0.15
     */
    @Test
    fun `updateLoanDays actualiza los días de préstamo en el carrito`() = runTest {
        // Arrange - Agregar un libro (por defecto viene con 7 días)
        val book = createTestBook(id = 1L, title = "El Quijote")
        viewModel.addToCart(book)

        // Act - Cambiar a 14 días
        viewModel.updateLoanDays(book.id, 14)

        // Assert - Verificar que los días cambiaron
        val cart = viewModel.cart.value
        assertEquals(14, cart[0].loanDays)
    }

    /**
     * TEST: Validación de rango de días (1-30)
     * 
     * ESCENARIO: Usuario intenta poner valores inválidos (0, 50, -5, etc.)
     * RESULTADO: El sistema limita los valores entre 1 y 30
     * 
     * ⚠️ REGLA DE NEGOCIO: 
     * • Mínimo 1 día (no tiene sentido 0 días)
     * • Máximo 30 días (política de la biblioteca)
     */
    @Test
    fun `updateLoanDays limita los días entre 1 y 30`() = runTest {
        // Arrange - Agregar un libro
        val book = createTestBook(id = 1L, title = "El Quijote")
        viewModel.addToCart(book)

        // Act & Assert - Intentar 50 días → debe quedar en 30 (máximo)
        viewModel.updateLoanDays(book.id, 50)
        val cart1 = viewModel.cart.value
        assertEquals(30, cart1[0].loanDays)

        // Act & Assert - Intentar 0 días → debe quedar en 1 (mínimo)
        viewModel.updateLoanDays(book.id, 0)
        val cart2 = viewModel.cart.value
        assertEquals(1, cart2[0].loanDays)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                     🔍 TESTS DE BÚSQUEDA DE LIBROS                      ║
    // ║  La búsqueda tiene debounce (espera 500ms antes de buscar)             ║
    // ║  para no hacer muchas llamadas mientras el usuario escribe             ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Actualizar texto de búsqueda
     * 
     * ESCENARIO: Usuario escribe "El Quijote" en el buscador
     * RESULTADO: El estado se actualiza inmediatamente
     * 
     * NOTA: La búsqueda real se ejecuta después del debounce (500ms)
     */
    @Test
    fun `onSearchQueryChange actualiza la query de búsqueda`() = runTest {
        // Act - Usuario escribe en el buscador
        viewModel.onSearchQueryChange("El Quijote")

        // Assert - El texto se guardó en el estado
        val searchState = viewModel.search.value
        assertEquals("El Quijote", searchState.query)
    }

    /**
     * TEST: Limpiar búsqueda
     * 
     * ESCENARIO: Usuario hace clic en "X" para limpiar el buscador
     * RESULTADO: La query se borra y los resultados desaparecen
     */
    @Test
    fun `clearSearchResults limpia los resultados de búsqueda`() = runTest {
        // Arrange - Simular que hay una búsqueda activa
        viewModel.onSearchQueryChange("test")

        // Act - Limpiar todo
        viewModel.clearSearchResults()

        // Assert - Todo debe estar vacío
        val searchState = viewModel.search.value
        assertEquals("", searchState.query)
        assertTrue(searchState.results.isEmpty())
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                       🔐 TESTS DE LOGIN                                 ║
    // ║  El login valida credenciales y guarda la sesión del usuario           ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Actualizar campo de email en login
     * 
     * ESCENARIO: Usuario escribe su email en el formulario
     * RESULTADO: El valor se guarda en el estado
     */
    @Test
    fun `onLoginEmailChange actualiza el email`() = runTest {
        // Act - Usuario escribe su email
        viewModel.onLoginEmailChange("usuario@gmail.com")

        // Assert - El email se guardó
        val loginState = viewModel.login.value
        assertEquals("usuario@gmail.com", loginState.email)
    }

    /**
     * TEST: Validación de formato de email
     * 
     * ESCENARIO: Usuario escribe un email sin "@"
     * RESULTADO: Aparece mensaje de error "Correo inválido"
     * 
     * VALIDACIÓN: Debe contener "@" para ser válido
     */
    @Test
    fun `onLoginEmailChange valida el formato del email`() = runTest {
        // Act - Email sin @ es inválido
        viewModel.onLoginEmailChange("email-invalido")

        // Assert - Debe haber un error
        val loginState = viewModel.login.value
        assertNotNull(loginState.emailError)
    }

    /**
     * TEST: Actualizar campo de contraseña
     * 
     * ESCENARIO: Usuario escribe su contraseña
     * RESULTADO: El valor se guarda (sin mostrar en pantalla)
     */
    @Test
    fun `onLoginPassChange actualiza la contraseña`() = runTest {
        // Act
        viewModel.onLoginPassChange("password123")

        // Assert
        val loginState = viewModel.login.value
        assertEquals("password123", loginState.pass)
    }

    /**
     * TEST: Limpiar resultado de login
     * 
     * ESCENARIO: Después de un login (exitoso o fallido), limpiar el estado
     * RESULTADO: success = false, errorMsg = null
     * 
     * USO: Cuando el usuario navega a otra pantalla después del login
     */
    @Test
    fun `clearLoginResult limpia el resultado del login`() = runTest {
        // Act - Limpiar cualquier resultado previo
        viewModel.clearLoginResult()

        // Assert - El estado debe estar "limpio"
        val loginState = viewModel.login.value
        assertFalse(loginState.success)
        assertNull(loginState.errorMsg)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                      📝 TESTS DE REGISTRO                               ║
    // ║  El registro valida todos los campos antes de crear la cuenta          ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Actualizar nombre en registro
     * 
     * ESCENARIO: Usuario escribe su nombre completo
     * RESULTADO: El nombre se guarda en el estado
     */
    @Test
    fun `onRegisterNameChange actualiza el nombre`() = runTest {
        // Act
        viewModel.onRegisterNameChange("Juan Pérez")

        // Assert
        val registerState = viewModel.register.value
        assertEquals("Juan Pérez", registerState.name)
    }

    /**
     * TEST: Actualizar email en registro
     * 
     * ESCENARIO: Usuario escribe su email para registrarse
     * RESULTADO: El email se guarda y se valida el formato
     */
    @Test
    fun `onRegisterEmailChange actualiza el email`() = runTest {
        // Act
        viewModel.onRegisterEmailChange("usuario@gmail.com")

        // Assert
        val registerState = viewModel.register.value
        assertEquals("usuario@gmail.com", registerState.email)
    }

    /**
     * TEST: Actualizar teléfono en registro
     * 
     * ESCENARIO: Usuario escribe su número de teléfono
     * RESULTADO: El teléfono se guarda (solo dígitos permitidos)
     */
    @Test
    fun `onRegisterPhoneChange actualiza el teléfono`() = runTest {
        // Act
        viewModel.onRegisterPhoneChange("12345678")

        // Assert
        val registerState = viewModel.register.value
        assertEquals("12345678", registerState.phone)
    }

    /**
     * TEST: Actualizar contraseña en registro
     * 
     * ESCENARIO: Usuario crea una contraseña segura
     * RESULTADO: La contraseña se guarda y se valida su fortaleza
     * 
     * REGLAS: Mínimo 8 caracteres, 1 mayúscula, 1 número, 1 símbolo
     */
    @Test
    fun `onRegisterPassChange actualiza la contraseña`() = runTest {
        // Act - Contraseña que cumple todas las reglas
        viewModel.onRegisterPassChange("Password123!")

        // Assert
        val registerState = viewModel.register.value
        assertEquals("Password123!", registerState.pass)
    }

    /**
     * TEST: Confirmar contraseña
     * 
     * ESCENARIO: Usuario repite la contraseña para confirmar
     * RESULTADO: Se valida que ambas contraseñas coincidan
     */
    @Test
    fun `onRegisterConfirmChange actualiza la confirmación`() = runTest {
        // Arrange - Primero establecer la contraseña original
        viewModel.onRegisterPassChange("Password123!")

        // Act - Escribir la misma contraseña en confirmación
        viewModel.onRegisterConfirmChange("Password123!")

        // Assert - Deben coincidir
        val registerState = viewModel.register.value
        assertEquals("Password123!", registerState.confirm)
    }

    /**
     * TEST: Limpiar resultado de registro
     * 
     * ESCENARIO: Después de registrarse, limpiar el estado
     * RESULTADO: success = false, errorMsg = null
     */
    @Test
    fun `clearRegisterResult limpia el resultado del registro`() = runTest {
        // Act
        viewModel.clearRegisterResult()

        // Assert
        val registerState = viewModel.register.value
        assertFalse(registerState.success)
        assertNull(registerState.errorMsg)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                      🌙 TESTS DE MODO OSCURO                            ║
    // ║  El modo oscuro cambia el tema de toda la aplicación                    ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Alternar modo oscuro
     * 
     * ESCENARIO: Usuario hace clic en el botón de modo oscuro
     * RESULTADO: El tema cambia de claro a oscuro (o viceversa)
     * 
     * COMPORTAMIENTO: Es un toggle (cada clic invierte el estado)
     */
    @Test
    fun `toggleDarkMode cambia el estado del modo oscuro`() = runTest {
        // Arrange - Guardar el estado inicial (false = modo claro)
        val initialState = viewModel.isDarkMode.value

        // Act - Hacer clic en toggle
        viewModel.toggleDarkMode()

        // Assert - El estado debe ser el opuesto
        val newState = viewModel.isDarkMode.value
        assertEquals(!initialState, newState)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                    🛠️ FUNCIONES AUXILIARES (HELPERS)                    ║
    // ║  Estas funciones crean objetos de prueba para los tests                 ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * HELPER: Crea un libro de prueba con valores por defecto
     * 
     * Esta función facilita la creación de libros para los tests
     * sin tener que especificar todos los campos cada vez.
     * 
     * @param id - ID del libro (default: 1)
     * @param title - Título (default: "Test Book")
     * @param author - Autor (default: "Test Author")
     * @param status - Estado: "Available", "Loaned", etc.
     * @return BookEntity configurado para testing
     * 
     * EJEMPLO DE USO:
     *   val libro = createTestBook(id = 1L, title = "El Quijote")
     *   val libroDisponible = createTestBook(status = "Available")
     */
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

// ════════════════════════════════════════════════════════════════════════════════
// RESUMEN DE TESTS EN ESTE ARCHIVO:
// ────────────────────────────────────────────────────────────────────────────────
// 🛒 CARRITO (5 tests):
//    • addToCart - Agregar libro
//    • addToCart duplicado - No permite duplicados
//    • removeFromCart - Eliminar libro
//    • updateLoanDays - Cambiar días de préstamo
//    • updateLoanDays límites - Valida rango 1-30
//
// 🔍 BÚSQUEDA (2 tests):
//    • onSearchQueryChange - Actualiza texto de búsqueda
//    • clearSearchResults - Limpia búsqueda
//
// 🔐 LOGIN (4 tests):
//    • onLoginEmailChange - Actualiza email
//    • onLoginEmailChange validación - Valida formato
//    • onLoginPassChange - Actualiza contraseña
//    • clearLoginResult - Limpia resultado
//
// 📝 REGISTRO (6 tests):
//    • onRegisterNameChange - Actualiza nombre
//    • onRegisterEmailChange - Actualiza email
//    • onRegisterPhoneChange - Actualiza teléfono
//    • onRegisterPassChange - Actualiza contraseña
//    • onRegisterConfirmChange - Confirma contraseña
//    • clearRegisterResult - Limpia resultado
//
// 🌙 DARK MODE (1 test):
//    • toggleDarkMode - Alterna modo oscuro
//
// TOTAL: 18 tests
// ════════════════════════════════════════════════════════════════════════════════

