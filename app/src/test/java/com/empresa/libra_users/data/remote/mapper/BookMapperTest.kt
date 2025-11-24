package com.empresa.libra_users.data.remote.mapper

import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.remote.dto.BookDto
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests simples para el mapeo de BookDto a BookEntity y viceversa.
 */
class BookMapperTest {

    @Test
    fun `toEntity convierte BookDto a BookEntity correctamente`() {
        // Arrange: Crear un BookDto de ejemplo
        val bookDto = BookDto(
            id = "1",
            title = "El Quijote",
            author = "Miguel de Cervantes",
            isbn = "1234567890",
            category = "Literatura",
            publisher = "Editorial XYZ",
            year = 1605,
            description = "Una novela clásica",
            coverUrl = "https://example.com/cover.jpg",
            status = "Available",
            totalCopies = 10,
            availableCopies = 5,
            price = 29.99,
            featured = true,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        // Act: Convertir a Entity
        val bookEntity = bookDto.toEntity()

        // Assert: Verificar que los campos se mapearon correctamente
        assertEquals(1L, bookEntity.id)
        assertEquals("El Quijote", bookEntity.title)
        assertEquals("Miguel de Cervantes", bookEntity.author)
        assertEquals("1234567890", bookEntity.isbn)
        assertEquals("Literatura", bookEntity.categoria)
        assertEquals("Editorial XYZ", bookEntity.publisher)
        assertEquals(1605, bookEntity.anio)
        assertEquals("Available", bookEntity.status)
        assertEquals(10, bookEntity.stock)
        assertEquals(5, bookEntity.disponibles)
        assertEquals("Una novela clásica", bookEntity.descripcion)
        assertEquals("https://example.com/cover.jpg", bookEntity.coverUrl)
        assertEquals("Featured", bookEntity.homeSection)
    }

    @Test
    fun `toEntity maneja valores nulos correctamente`() {
        // Arrange: BookDto con valores nulos
        val bookDto = BookDto(
            id = "2",
            title = "Libro Sin Datos",
            author = "Autor",
            isbn = "0987654321",
            category = "Ciencia",
            publisher = null,
            year = null,
            description = null,
            coverUrl = null,
            status = "Loaned",
            totalCopies = 5,
            availableCopies = 0,
            price = null,
            featured = false,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        // Act
        val bookEntity = bookDto.toEntity()

        // Assert: Verificar que los valores nulos se manejan correctamente
        assertEquals("", bookEntity.publisher)
        assertEquals("", bookEntity.descripcion)
        assertEquals("", bookEntity.coverUrl)
        assertEquals("None", bookEntity.homeSection)
    }

    @Test
    fun `toEntity mapea status Available cuando hay copias disponibles`() {
        val bookDto = BookDto(
            id = "3",
            title = "Libro Disponible",
            author = "Autor",
            isbn = "1111111111",
            category = "Ficción",
            publisher = "Editorial",
            year = 2020,
            description = "Descripción",
            coverUrl = "cover.jpg",
            status = "Any",
            totalCopies = 10,
            availableCopies = 3,
            price = 20.0,
            featured = false,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        val bookEntity = bookDto.toEntity()

        assertEquals("Available", bookEntity.status)
    }

    @Test
    fun `toEntity mapea status Retired correctamente`() {
        val bookDto = BookDto(
            id = "4",
            title = "Libro Retirado",
            author = "Autor",
            isbn = "2222222222",
            category = "Historia",
            publisher = "Editorial",
            year = 2010,
            description = "Descripción",
            coverUrl = "cover.jpg",
            status = "retired",
            totalCopies = 5,
            availableCopies = 0,
            price = 15.0,
            featured = false,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        val bookEntity = bookDto.toEntity()

        assertEquals("Retired", bookEntity.status)
    }

    @Test
    fun `toDto convierte BookEntity a BookDto correctamente`() {
        // Arrange: Crear un BookEntity
        val bookEntity = BookEntity(
            id = 1L,
            title = "El Quijote",
            author = "Miguel de Cervantes",
            isbn = "1234567890",
            categoryId = 1L,
            categoria = "Literatura",
            publisher = "Editorial XYZ",
            publishDate = "1605-01-01",
            anio = 1605,
            status = "Available",
            inventoryCode = "1",
            stock = 10,
            disponibles = 5,
            descripcion = "Una novela clásica",
            coverUrl = "https://example.com/cover.jpg",
            homeSection = "Featured"
        )

        // Act: Convertir a Dto
        val bookDto = bookEntity.toDto()

        // Assert: Verificar que los campos se mapearon correctamente
        assertEquals("1", bookDto.id)
        assertEquals("El Quijote", bookDto.title)
        assertEquals("Miguel de Cervantes", bookDto.author)
        assertEquals("1234567890", bookDto.isbn)
        assertEquals("Literatura", bookDto.category)
        assertEquals("Editorial XYZ", bookDto.publisher)
        assertEquals(1605, bookDto.year)
        assertEquals("Una novela clásica", bookDto.description)
        assertEquals("https://example.com/cover.jpg", bookDto.coverUrl)
        assertEquals("Available", bookDto.status)
        assertEquals(10, bookDto.totalCopies)
        assertEquals(5, bookDto.availableCopies)
        assertTrue(bookDto.featured)
    }

    @Test
    fun `toDto maneja valores vacíos correctamente`() {
        val bookEntity = BookEntity(
            id = 2L,
            title = "Libro Sin Datos",
            author = "Autor",
            isbn = "0987654321",
            categoryId = 2L,
            categoria = "Ciencia",
            publisher = "",
            publishDate = "2020-01-01",
            anio = 2020,
            status = "Loaned",
            inventoryCode = "2",
            stock = 5,
            disponibles = 0,
            descripcion = "",
            coverUrl = "",
            homeSection = "None"
        )

        val bookDto = bookEntity.toDto()

        assertNull(bookDto.publisher)
        assertNull(bookDto.description)
        assertNull(bookDto.coverUrl)
        assertFalse(bookDto.featured)
    }

    @Test
    fun `toDto mapea status correctamente desde Entity`() {
        val bookEntity = BookEntity(
            id = 3L,
            title = "Libro",
            author = "Autor",
            isbn = "3333333333",
            categoryId = 1L,
            categoria = "Ficción",
            publisher = "Editorial",
            publishDate = "2020-01-01",
            anio = 2020,
            status = "Retired",
            inventoryCode = "3",
            stock = 5,
            disponibles = 0,
            descripcion = "Descripción",
            coverUrl = "cover.jpg",
            homeSection = "None"
        )

        val bookDto = bookEntity.toDto()

        assertEquals("Retired", bookDto.status)
    }
}

