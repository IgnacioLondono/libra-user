package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.BookDao
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.remote.dto.BookApi
import com.empresa.libra_users.data.remote.mapper.toDto
import com.empresa.libra_users.data.remote.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val bookApi: BookApi
) {
    
    suspend fun insert(book: BookEntity): Long {
        return try {
            if (book.id == 0L) {
                // Intentar crear en el backend
                // El AuthInterceptor agregará automáticamente el token si existe
                val bookDto = book.toDto()
                val response = bookApi.createBook(bookDto)
                
                if (response.isSuccessful && response.body() != null) {
                    val createdBook = response.body()!!
                    val bookEntity = createdBook.toEntity()
                    // Insertar en Room con el ID del backend
                    bookDao.insert(bookEntity)
                    bookEntity.id
                } else {
                    // Fallback a Room local
                    bookDao.insert(book)
                }
            } else {
                // Libro existente, solo insertar local
                bookDao.insert(book)
            }
        } catch (e: IOException) {
            // Error de red, fallback a Room local
            bookDao.insert(book)
        } catch (e: HttpException) {
            // Error HTTP, fallback a Room local
            bookDao.insert(book)
        } catch (e: Exception) {
            // Otro error, fallback a Room local
            bookDao.insert(book)
        }
    }

    fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()

    suspend fun update(book: BookEntity): Result<Unit> {
        return try {
            if (book.id > 0) {
                // Intentar actualizar en el backend
                // El AuthInterceptor agregará automáticamente el token si existe
                val bookDto = book.toDto()
                val response = bookApi.updateBook(book.id.toString(), bookDto)
                
                if (response.isSuccessful && response.body() != null) {
                    val updatedBook = response.body()!!
                    val bookEntity = updatedBook.toEntity().copy(id = book.id)
                    // Actualizar caché local
                    bookDao.update(bookEntity)
                    Result.success(Unit)
                } else {
                    // Fallback a Room local
                    bookDao.update(book)
                    Result.success(Unit)
                }
            } else {
                // Libro nuevo, solo actualizar local
                bookDao.update(book)
                Result.success(Unit)
            }
        } catch (e: IOException) {
            // Error de red, actualizar solo local
            bookDao.update(book)
            Result.success(Unit)
        } catch (e: HttpException) {
            // Error HTTP, actualizar solo local
            bookDao.update(book)
            Result.success(Unit)
        } catch (e: Exception) {
            // Otro error, actualizar solo local
            bookDao.update(book)
            Result.success(Unit)
        }
    }

    suspend fun delete(book: BookEntity): Result<Unit> {
        return try {
            if (book.id > 0) {
                // Intentar eliminar en el backend
                // El AuthInterceptor agregará automáticamente el token si existe
                val response = bookApi.deleteBook(book.id.toString())
                
                if (response.isSuccessful) {
                    // Eliminar también de Room local
                    bookDao.delete(book)
                    Result.success(Unit)
                } else {
                    // Fallback: eliminar solo local
                    bookDao.delete(book)
                    Result.success(Unit)
                }
            } else {
                // Libro sin ID, eliminar solo local
                bookDao.delete(book)
                Result.success(Unit)
            }
        } catch (e: IOException) {
            // Error de red, eliminar solo local
            bookDao.delete(book)
            Result.success(Unit)
        } catch (e: HttpException) {
            // Error HTTP, eliminar solo local
            bookDao.delete(book)
            Result.success(Unit)
        } catch (e: Exception) {
            // Otro error, eliminar solo local
            bookDao.delete(book)
            Result.success(Unit)
        }
    }

    suspend fun count(): Int = bookDao.count()

    suspend fun getBookById(id: Long): BookEntity? {
        return try {
            // Intentar obtener del backend primero
            val response = bookApi.getBookById(id.toString())
            if (response.isSuccessful && response.body() != null) {
                val bookDto = response.body()!!
                val bookEntity = bookDto.toEntity()
                // Actualizar caché local
                bookDao.insert(bookEntity)
                bookEntity
            } else {
                // Fallback a Room local
                bookDao.getBookById(id)
            }
        } catch (e: Exception) {
            // Error, usar Room local
            bookDao.getBookById(id)
        }
    }

    suspend fun searchBooks(query: String): List<BookEntity> {
        return try {
            val response = bookApi.searchBooks(query = query, page = 0, size = 100)
            if (response.isSuccessful && response.body() != null) {
                val booksDto = response.body()!!.content
                val booksEntity = booksDto.map { it.toEntity() }
                // Actualizar caché local
                booksEntity.forEach { bookDao.insert(it) }
                booksEntity
            } else {
                // Fallback a Room local
                bookDao.searchBooks(query)
            }
        } catch (e: Exception) {
            // Error, usar Room local
            bookDao.searchBooks(query)
        }
    }

    suspend fun searchBooksWithFilters(query: String, categoria: String?, soloDisponibles: Boolean): List<BookEntity> {
        return try {
            // Si hay categoría, usar endpoint de categoría
            val response = if (categoria != null) {
                bookApi.getBooksByCategory(categoria, page = 0, size = 100)
            } else {
                bookApi.searchBooks(query = query, page = 0, size = 100)
            }
            
            if (response.isSuccessful && response.body() != null) {
                var booksDto = response.body()!!.content
                
                // Filtrar por query si existe
                if (query.isNotEmpty()) {
                    booksDto = booksDto.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true) ||
                        it.isbn.contains(query, ignoreCase = true)
                    }
                }
                
                // Filtrar por disponibilidad
                if (soloDisponibles) {
                    booksDto = booksDto.filter { it.availableCopies > 0 }
                }
                
                val booksEntity = booksDto.map { it.toEntity() }
                // Actualizar caché local
                booksEntity.forEach { bookDao.insert(it) }
                booksEntity
            } else {
                // Fallback a Room local
                bookDao.searchBooksWithFilters(query, categoria, if (soloDisponibles) 1 else 0)
            }
        } catch (e: Exception) {
            // Error, usar Room local
            bookDao.searchBooksWithFilters(query, categoria, if (soloDisponibles) 1 else 0)
        }
    }

    suspend fun getBooksByCategory(categoria: String): List<BookEntity> {
        return try {
            val response = bookApi.getBooksByCategory(categoria, page = 0, size = 100)
            if (response.isSuccessful && response.body() != null) {
                val booksDto = response.body()!!.content
                val booksEntity = booksDto.map { it.toEntity() }
                // Actualizar caché local
                booksEntity.forEach { bookDao.insert(it) }
                booksEntity
            } else {
                // Fallback a Room local
                bookDao.getBooksByCategory(categoria)
            }
        } catch (e: Exception) {
            // Error, usar Room local
            bookDao.getBooksByCategory(categoria)
        }
    }

    suspend fun getAvailableBooks(): List<BookEntity> {
        return try {
            // Obtener todos los libros y filtrar por disponibilidad
            val response = bookApi.getBooks(page = 0, size = 100)
            if (response.isSuccessful && response.body() != null) {
                val booksDto = response.body()!!.content.filter { it.availableCopies > 0 }
                val booksEntity = booksDto.map { it.toEntity() }
                // Actualizar caché local
                booksEntity.forEach { bookDao.insert(it) }
                booksEntity
            } else {
                // Fallback a Room local
                bookDao.getAvailableBooks()
            }
        } catch (e: Exception) {
            // Error, usar Room local
            bookDao.getAvailableBooks()
        }
    }

    suspend fun countActiveLoansForBook(bookId: Long): Int {
        return try {
            val response = bookApi.getBookAvailability(bookId.toString())
            if (response.isSuccessful && response.body() != null) {
                val availability = response.body()!!
                // Calcular préstamos activos = totalCopies - availableCopies
                availability.totalCopies - availability.availableCopies
            } else {
                // Fallback a Room local
                bookDao.countActiveLoansForBook(bookId)
            }
        } catch (e: Exception) {
            // Error, usar Room local
            bookDao.countActiveLoansForBook(bookId)
        }
    }
}
