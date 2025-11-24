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

class BookRepositoryTest {

    private lateinit var bookDao: BookDao
    private lateinit var bookApi: BookApi
    private lateinit var bookRepository: BookRepository

    @Before
    fun setup() {
        bookDao = mockk(relaxed = true)
        bookApi = mockk(relaxed = true)
        bookRepository = BookRepository(bookDao, bookApi)
    }

    @Test
    fun `getAllBooks debería retornar Flow de libros`() = runTest {
        // Arrange
        val books = listOf(
            createTestBookEntity(id = 1L)
        )
        every { bookDao.getAllBooks() } returns flowOf(books)

        // Act
        val result = bookRepository.getAllBooks()

        // Assert
        result.collect { collectedBooks ->
            assertEquals(1, collectedBooks.size)
        }
    }

    @Test
    fun `count debería retornar número de libros`() = runTest {
        // Arrange
        coEvery { bookDao.count() } returns 10

        // Act
        val result = bookRepository.count()

        // Assert
        assertEquals(10, result)
    }

    @Test
    fun `insert con libro nuevo debería intentar crear en backend`() = runTest {
        // Arrange
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

        // Assert
        assertEquals(1L, result)
    }

    @Test
    fun `insert con libro existente debería insertar solo local`() = runTest {
        // Arrange
        val book = createTestBookEntity(id = 1L)
        coEvery { bookDao.insert(book) } returns 1L

        // Act
        val result = bookRepository.insert(book)

        // Assert
        assertEquals(1L, result)
    }

    @Test
    fun `insert con error de red debería hacer fallback a Room`() = runTest {
        // Arrange
        val book = createTestBookEntity(id = 0L)
        coEvery { bookApi.createBook(any()) } throws java.io.IOException("Network error")
        coEvery { bookDao.insert(book) } returns 1L

        // Act
        val result = bookRepository.insert(book)

        // Assert
        assertEquals(1L, result)
    }

    @Test
    fun `getBookById con respuesta exitosa debería retornar libro`() = runTest {
        // Arrange
        val bookDto = createTestBookDto(id = "1")
        val response = Response.success(bookDto)
        coEvery { bookApi.getBookById("1") } returns response
        coEvery { bookDao.insert(any()) } returns 1L

        // Act
        val result = bookRepository.getBookById(1L)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `getBookById con error debería hacer fallback a Room`() = runTest {
        // Arrange
        val book = createTestBookEntity(id = 1L)
        coEvery { bookApi.getBookById("1") } throws Exception("Error")
        coEvery { bookDao.getBookById(1L) } returns book

        // Act
        val result = bookRepository.getBookById(1L)

        // Assert
        assertNotNull(result)
        assertEquals(1L, result!!.id)
    }

    @Test
    fun `searchBooks debería retornar lista de libros`() = runTest {
        // Arrange
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

        // Assert
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `searchBooks con error debería hacer fallback a Room`() = runTest {
        // Arrange
        val books = listOf(createTestBookEntity(id = 1L))
        coEvery { bookApi.searchBooks(any(), any(), any()) } throws Exception("Error")
        coEvery { bookDao.searchBooks("test") } returns books

        // Act
        val result = bookRepository.searchBooks("test")

        // Assert
        assertEquals(1, result.size)
    }

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

