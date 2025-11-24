package com.empresa.libra_users.data.local.user

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests simples para BookEntity.
 * Verificamos que los data classes funcionan correctamente.
 */
class BookEntityTest {

    @Test
    fun `BookEntity se crea correctamente con todos los campos`() {
        // Arrange & Act: Crear una entidad de libro
        val book = BookEntity(
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
            inventoryCode = "LIB-001",
            stock = 10,
            disponibles = 5,
            descripcion = "Una novela clásica",
            coverUrl = "https://example.com/cover.jpg",
            homeSection = "Featured"
        )

        // Assert: Verificar que todos los campos se asignaron correctamente
        assertEquals(1L, book.id)
        assertEquals("El Quijote", book.title)
        assertEquals("Miguel de Cervantes", book.author)
        assertEquals("1234567890", book.isbn)
        assertEquals(1L, book.categoryId)
        assertEquals("Literatura", book.categoria)
        assertEquals("Editorial XYZ", book.publisher)
        assertEquals("1605-01-01", book.publishDate)
        assertEquals(1605, book.anio)
        assertEquals("Available", book.status)
        assertEquals("LIB-001", book.inventoryCode)
        assertEquals(10, book.stock)
        assertEquals(5, book.disponibles)
        assertEquals("Una novela clásica", book.descripcion)
        assertEquals("https://example.com/cover.jpg", book.coverUrl)
        assertEquals("Featured", book.homeSection)
    }

    @Test
    fun `BookEntity usa valores por defecto correctamente`() {
        // Arrange & Act: Crear una entidad con valores mínimos
        val book = BookEntity(
            title = "Libro Simple",
            author = "Autor",
            isbn = "0987654321",
            categoryId = 2L,
            publisher = "Editorial",
            publishDate = "2020-01-01",
            status = "Loaned",
            inventoryCode = "LIB-002"
        )

        // Assert: Verificar que los valores por defecto se aplicaron
        assertEquals(0L, book.id) // Valor por defecto
        assertEquals("", book.categoria) // Valor por defecto
        assertEquals(0, book.anio) // Valor por defecto
        assertEquals(0, book.stock) // Valor por defecto
        assertEquals(0, book.disponibles) // Valor por defecto
        assertEquals("", book.descripcion) // Valor por defecto
        assertEquals("", book.coverUrl) // Valor por defecto
        assertEquals("None", book.homeSection) // Valor por defecto
    }

    @Test
    fun `BookEntity puede tener diferentes estados`() {
        val estados = listOf("Available", "Loaned", "Damaged", "Retired")

        estados.forEach { estado ->
            val book = BookEntity(
                title = "Libro $estado",
                author = "Autor",
                isbn = "1111111111",
                categoryId = 1L,
                publisher = "Editorial",
                publishDate = "2020-01-01",
                status = estado,
                inventoryCode = "LIB-003"
            )

            assertEquals(estado, book.status)
        }
    }

    @Test
    fun `BookEntity puede tener stock y disponibles diferentes`() {
        val book = BookEntity(
            title = "Libro con Stock",
            author = "Autor",
            isbn = "2222222222",
            categoryId = 1L,
            publisher = "Editorial",
            publishDate = "2020-01-01",
            status = "Available",
            inventoryCode = "LIB-004",
            stock = 20,
            disponibles = 15
        )

        assertEquals(20, book.stock)
        assertEquals(15, book.disponibles)
        // Verificar que hay 5 libros prestados (20 - 15 = 5)
        assertEquals(5, book.stock - book.disponibles)
    }
}

