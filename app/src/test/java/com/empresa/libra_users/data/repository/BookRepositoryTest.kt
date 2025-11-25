package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.BookDao
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.remote.dto.BookApi
import com.empresa.libra_users.data.remote.dto.BookDto
import com.empresa.libra_users.data.remote.dto.BookPageResponseDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                      TESTS DE BookRepository                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ Este archivo prueba el repositorio de libros que maneja:                      ║
 * ║ • Operaciones CRUD (Crear, Leer, Actualizar, Borrar)                         ║
 * ║ • Sincronización entre API remota (Backend) y base de datos local (Room)     ║
 * ║ • Estrategia de fallback: si la API falla, usa datos locales                 ║
 * ║                                                                               ║
 * ║ TECNOLOGÍA DE MOCKING: MockK                                                 ║
 * ║ • mockk(relaxed = true) - Crea mocks que no fallan si no se configura algo  ║
 * ║ • coEvery - Configura comportamiento de funciones suspend                    ║
 * ║ • every - Configura comportamiento de funciones normales                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
class BookRepositoryTest {

    // DAO local (Room) - mockeado para no usar base de datos real
    private lateinit var bookDao: BookDao
    // API remota (Retrofit) - mockeada para no hacer llamadas HTTP reales
    private lateinit var bookApi: BookApi
    // Repositorio a probar
    private lateinit var bookRepository: BookRepository

    /**
     * SETUP: Configura los mocks antes de cada test
     * 
     * relaxed = true significa que los mocks retornan valores por defecto
     * si no se configura explícitamente un comportamiento
     */
    @Before
    fun setup() {
        bookDao = mockk(relaxed = true)
        bookApi = mockk(relaxed = true)
        bookRepository = BookRepository(bookDao, bookApi)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                     TESTS DE OBTENCIÓN DE LIBROS                        ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Obtener todos los libros como Flow
     * 
     * ESCENARIO: La app necesita observar cambios en la lista de libros
     * COMPORTAMIENTO: Room retorna un Flow que emite la lista actualizada
     * 
     * Flow es reactivo: si los datos cambian, la UI se actualiza automáticamente
     */
    @Test
    fun `getAllBooks debería retornar Flow de libros`() = runTest {
        // Arrange - Configurar el DAO para retornar un libro
        val books = listOf(
            createTestBookEntity(id = 1L)
        )
        every { bookDao.getAllBooks() } returns flowOf(books)

        // Act - Llamar al repositorio
        val result = bookRepository.getAllBooks()

        // Assert - Verificar que el Flow contiene los libros
        result.collect { collectedBooks ->
            assertEquals(1, collectedBooks.size)
        }
    }

    /**
     * TEST: Contar libros en la base de datos
     * 
     * ESCENARIO: Necesitamos saber si hay libros cargados
     * USO: Al iniciar la app, verificamos si hay que cargar datos iniciales
     */
    @Test
    fun `count debería retornar número de libros`() = runTest {
        // Arrange - El DAO retorna 10 libros
        coEvery { bookDao.count() } returns 10

        // Act
        val result = bookRepository.count()

        // Assert
        assertEquals(10, result)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                      TESTS DE INSERCIÓN                                 ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Insertar libro NUEVO (id = 0) intenta crear en backend
     * 
     * FLUJO:
     * 1. Si id = 0, es un libro nuevo → intentar crear en API
     * 2. Si la API responde OK → guardar en Room con el ID del backend
     * 3. Retornar el ID generado
     */
    @Test
    fun `insert con libro nuevo debería intentar crear en backend`() = runTest {
        // Arrange - Libro nuevo (id = 0) y respuesta exitosa del API
        val book = createTestBookEntity(id = 0L)
        val bookDto = BookDto(
            id = "1",
            title = book.title,
            author = book.author,
            isbn = book.isbn,
            category = book.categoria,
            publisher = book.publisher,
            year = book.anio,
            description = book.descripcion,
            coverUrl = book.coverUrl,
            status = book.status,
            totalCopies = book.stock,
            availableCopies = book.disponibles,
            price = null,
            featured = false,
            createdAt = "",
            updatedAt = ""
        )
        val response = Response.success(bookDto)
        coEvery { bookApi.createBook(any()) } returns response
        coEvery { bookDao.insert(any()) } returns 1L

        // Act
        val result = bookRepository.insert(book)

        // Assert - Debería retornar el ID del libro insertado
        assertEquals(1L, result)
    }

    /**
     * TEST: Insertar libro EXISTENTE (id > 0) solo guarda localmente
     * 
     * ESCENARIO: El libro ya existe en el backend (tiene ID)
     * COMPORTAMIENTO: No hace llamada a la API, solo guarda en Room
     */
    @Test
    fun `insert con libro existente debería insertar solo local`() = runTest {
        // Arrange - Libro con id = 1 (ya existe)
        val book = createTestBookEntity(id = 1L)
        coEvery { bookDao.insert(book) } returns 1L

        // Act
        val result = bookRepository.insert(book)

        // Assert
        assertEquals(1L, result)
    }

    /**
     * TEST: Fallback cuando la red falla
     * 
     * ESCENARIO: El backend no está disponible (sin internet, servidor caído)
     * COMPORTAMIENTO: Guarda solo localmente para sincronizar después
     * 
     * ⚠️ IMPORTANTE: La app debe funcionar offline
     */
    @Test
    fun `insert con error de red debería hacer fallback a Room`() = runTest {
        // Arrange - La API falla con IOException
        val book = createTestBookEntity(id = 0L)
        coEvery { bookApi.createBook(any()) } throws java.io.IOException("Network error")
        coEvery { bookDao.insert(book) } returns 1L

        // Act - A pesar del error de red, debe guardar localmente
        val result = bookRepository.insert(book)

        // Assert - El libro se guardó localmente
        assertEquals(1L, result)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                     TESTS DE BÚSQUEDA POR ID                            ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Obtener libro por ID desde la API
     * 
     * FLUJO:
     * 1. Buscar en la API (datos más actualizados)
     * 2. Si encuentra, guardar en Room (cache)
     * 3. Retornar el libro
     */
    @Test
    fun `getBookById con respuesta exitosa debería retornar libro`() = runTest {
        // Arrange - API retorna el libro
        val bookDto = createTestBookDto(id = "1")
        val response = Response.success(bookDto)
        coEvery { bookApi.getBookById("1") } returns response
        coEvery { bookDao.insert(any()) } returns 1L

        // Act
        val result = bookRepository.getBookById(1L)

        // Assert - Debe encontrar el libro
        assertNotNull(result)
    }

    /**
     * TEST: Fallback a Room cuando la API falla
     * 
     * ESCENARIO: Sin conexión a internet
     * COMPORTAMIENTO: Busca en la base de datos local (cache)
     */
    @Test
    fun `getBookById con error debería hacer fallback a Room`() = runTest {
        // Arrange - API falla, pero Room tiene el libro en cache
        val book = createTestBookEntity(id = 1L)
        coEvery { bookApi.getBookById("1") } throws Exception("Error")
        coEvery { bookDao.getBookById(1L) } returns book

        // Act
        val result = bookRepository.getBookById(1L)

        // Assert - Debe retornar el libro desde Room
        assertNotNull(result)
        assertEquals(1L, result!!.id)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                       TESTS DE BÚSQUEDA                                 ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TEST: Buscar libros por texto
     * 
     * ESCENARIO: El usuario busca "Quijote" en el catálogo
     * COMPORTAMIENTO: Busca en la API y retorna resultados paginados
     */
    @Test
    fun `searchBooks debería retornar lista de libros`() = runTest {
        // Arrange - API retorna resultados paginados
        val bookDto = createTestBookDto(id = "1")
        val pageResponse = BookPageResponseDto(
            content = listOf(bookDto),
            totalElements = 1L,
            totalPages = 1,
            size = 10,
            number = 0
        )
        val response = Response.success(pageResponse)
        coEvery { bookApi.searchBooks(any(), any(), any()) } returns response
        coEvery { bookDao.insert(any()) } returns 1L

        // Act
        val result = bookRepository.searchBooks("test")

        // Assert - Debe encontrar resultados
        assertTrue(result.isNotEmpty())
    }

    /**
     * TEST: Búsqueda offline usa datos locales
     * 
     * ESCENARIO: Usuario busca sin conexión
     * COMPORTAMIENTO: Busca en Room con los libros cacheados
     */
    @Test
    fun `searchBooks con error debería hacer fallback a Room`() = runTest {
        // Arrange - API falla, pero Room tiene libros
        val books = listOf(createTestBookEntity(id = 1L))
        coEvery { bookApi.searchBooks(any(), any(), any()) } throws Exception("Error")
        coEvery { bookDao.searchBooks("test") } returns books

        // Act
        val result = bookRepository.searchBooks("test")

        // Assert - Retorna libros desde Room
        assertEquals(1, result.size)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ║                    FUNCIONES AUXILIARES (HELPERS)                       ║
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un BookEntity de prueba con valores por defecto
     * 
     * @param id - El ID del libro (0 = nuevo, >0 = existente)
     * @return BookEntity con datos de prueba
     */
    private fun createTestBookEntity(id: Long = 0L): BookEntity {
        return BookEntity(
            id = id,
            title = "Test Book",
            author = "Test Author",
            isbn = "1234567890",
            categoryId = 1L,
            categoria = "Fiction",
            publisher = "Test Publisher",
            publishDate = "2024-01-01",
            anio = 2024,
            status = "Available",
            inventoryCode = "TEST001",
            stock = 10,
            disponibles = 5,
            descripcion = "Test description",
            coverUrl = "",
            homeSection = "None"
        )
    }

    /**
     * Crea un BookDto de prueba (formato del API/Backend)
     * 
     * @param id - El ID como String (formato del backend)
     * @return BookDto con datos de prueba
     */
    private fun createTestBookDto(id: String = "0"): BookDto {
        return BookDto(
            id = id,
            title = "Test Book",
            author = "Test Author",
            isbn = "1234567890",
            category = "Fiction",
            publisher = "Test Publisher",
            year = 2024,
            description = "Test description",
            coverUrl = "",
            status = "Available",
            totalCopies = 10,
            availableCopies = 5,
            price = null,
            featured = false,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )
    }
}

