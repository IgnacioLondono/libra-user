package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.remote.dto.BookApi
import com.empresa.libra_users.data.remote.mapper.toDto
import com.empresa.libra_users.data.remote.mapper.toEntity
import com.empresa.libra_users.domain.validation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val bookApi: BookApi
) {
    
    // Estado en memoria para los libros (sin Room)
    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())
    val books: StateFlow<List<BookEntity>> = _books.asStateFlow()
    
    /**
     * Carga todos los libros desde el API y actualiza el estado.
     */
    suspend fun loadAllBooks(): Result<Int> {
        return try {
            var totalLoaded = 0
            var page = 0
            val pageSize = 100
            val allBooks = mutableListOf<BookEntity>()
            
            while (true) {
                val response = bookApi.getBooks(page = page, size = pageSize)
                if (response.isSuccessful && response.body() != null) {
                    val booksDto = response.body()!!.content
                    if (booksDto.isEmpty()) break
                    
                    val booksEntity = booksDto.map { it.toEntity() }
                    allBooks.addAll(booksEntity)
                    totalLoaded += booksEntity.size
                    
                    if (booksDto.size < pageSize) break
                    page++
                } else {
                    break
                }
            }
            
            _books.value = allBooks
            Result.success(totalLoaded)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los libros (desde el estado en memoria).
     * Si el estado está vacío, carga desde el API.
     */
    suspend fun getAllBooks(): List<BookEntity> {
        if (_books.value.isEmpty()) {
            loadAllBooks()
        }
        return _books.value
    }
    
    /**
     * Obtiene todos los libros como Flow (para compatibilidad con código existente).
     */
    fun getAllBooksFlow(): StateFlow<List<BookEntity>> = books
    
    /**
     * Crea un libro en la base de datos del microservicio.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     */
    suspend fun insert(book: BookEntity): Result<Long> {
        // Validaciones antes de llamar al API
        val titleError = validateBookTitle(book.title)
        if (titleError != null) {
            return Result.failure(IllegalArgumentException(titleError))
        }
        
        val authorError = validateBookAuthor(book.author)
        if (authorError != null) {
            return Result.failure(IllegalArgumentException(authorError))
        }
        
        val categoriaError = validateBookCategory(book.categoria)
        if (categoriaError != null) {
            return Result.failure(IllegalArgumentException(categoriaError))
        }
        
        val isbnError = validateISBN(book.isbn)
        if (isbnError != null) {
            return Result.failure(IllegalArgumentException(isbnError))
        }
        
        val publisherError = validateBookPublisher(book.publisher)
        if (publisherError != null) {
            return Result.failure(IllegalArgumentException(publisherError))
        }
        
        val anioError = validateBookYear(book.anio)
        if (anioError != null) {
            return Result.failure(IllegalArgumentException(anioError))
        }
        
        val stockError = validateStock(book.stock)
        if (stockError != null) {
            return Result.failure(IllegalArgumentException(stockError))
        }
        
        val disponiblesError = validateDisponibles(book.disponibles, book.stock)
        if (disponiblesError != null) {
            return Result.failure(IllegalArgumentException(disponiblesError))
        }
        
        val descripcionError = validateBookDescription(book.descripcion ?: "")
        if (descripcionError != null) {
            return Result.failure(IllegalArgumentException(descripcionError))
        }
        
        return try {
            // PRIMERO: Intentar crear en el API/base de datos
            val bookDto = book.toDto()
            val response = bookApi.createBook(bookDto)
            
            if (response.isSuccessful && response.body() != null) {
                val createdBook = response.body()!!
                val bookEntity = createdBook.toEntity()
                // SOLO SI ES EXITOSO: Actualizar estado en memoria
                _books.value = _books.value + bookEntity
                Result.success(bookEntity.id)
            } else {
                Result.failure(IllegalArgumentException("Error al crear libro en la base de datos"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Actualiza un libro en la base de datos del microservicio.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     */
    suspend fun update(book: BookEntity): Result<Unit> {
        // Validaciones antes de llamar al API
        val idError = validateId(book.id)
        if (idError != null) {
            return Result.failure(IllegalArgumentException(idError))
        }
        
        val titleError = validateBookTitle(book.title)
        if (titleError != null) {
            return Result.failure(IllegalArgumentException(titleError))
        }
        
        val authorError = validateBookAuthor(book.author)
        if (authorError != null) {
            return Result.failure(IllegalArgumentException(authorError))
        }
        
        val categoriaError = validateBookCategory(book.categoria)
        if (categoriaError != null) {
            return Result.failure(IllegalArgumentException(categoriaError))
        }
        
        val isbnError = validateISBN(book.isbn)
        if (isbnError != null) {
            return Result.failure(IllegalArgumentException(isbnError))
        }
        
        val publisherError = validateBookPublisher(book.publisher)
        if (publisherError != null) {
            return Result.failure(IllegalArgumentException(publisherError))
        }
        
        val anioError = validateBookYear(book.anio)
        if (anioError != null) {
            return Result.failure(IllegalArgumentException(anioError))
        }
        
        val stockError = validateStock(book.stock)
        if (stockError != null) {
            return Result.failure(IllegalArgumentException(stockError))
        }
        
        val disponiblesError = validateDisponibles(book.disponibles, book.stock)
        if (disponiblesError != null) {
            return Result.failure(IllegalArgumentException(disponiblesError))
        }
        
        val descripcionError = validateBookDescription(book.descripcion ?: "")
        if (descripcionError != null) {
            return Result.failure(IllegalArgumentException(descripcionError))
        }
        
        return try {
            if (book.id > 0) {
                // PRIMERO: Intentar actualizar en el API/base de datos
                val bookDto = book.toDto()
                val response = bookApi.updateBook(book.id.toString(), bookDto)
                
                if (response.isSuccessful && response.body() != null) {
                    val updatedBook = response.body()!!
                    val bookEntity = updatedBook.toEntity().copy(id = book.id)
                    // SOLO SI ES EXITOSO: Actualizar estado en memoria
                    _books.value = _books.value.map { if (it.id == book.id) bookEntity else it }
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalArgumentException("Error al actualizar libro en la base de datos"))
                }
            } else {
                Result.failure(IllegalArgumentException("ID de libro inválido"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Elimina un libro de la base de datos del microservicio.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     */
    suspend fun delete(book: BookEntity): Result<Unit> {
        // Validaciones antes de llamar al API
        val idError = validateId(book.id)
        if (idError != null) {
            return Result.failure(IllegalArgumentException(idError))
        }
        
        return try {
            if (book.id > 0) {
                // PRIMERO: Intentar eliminar en el API/base de datos
                val response = bookApi.deleteBook(book.id.toString())
                
                if (response.isSuccessful) {
                    // SOLO SI ES EXITOSO: Eliminar del estado en memoria
                    _books.value = _books.value.filter { it.id != book.id }
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalArgumentException("Error al eliminar libro en la base de datos"))
                }
            } else {
                Result.failure(IllegalArgumentException("ID de libro inválido"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }

    suspend fun count(): Int {
        if (_books.value.isEmpty()) {
            loadAllBooks()
        }
        return _books.value.size
    }

    suspend fun getBookById(id: Long): BookEntity? {
        return try {
            val response = bookApi.getBookById(id.toString())
            if (response.isSuccessful && response.body() != null) {
                val bookDto = response.body()!!
                val bookEntity = bookDto.toEntity()
                // Actualizar estado en memoria si no existe
                val currentBooks = _books.value.toMutableList()
                val index = currentBooks.indexOfFirst { it.id == id }
                if (index >= 0) {
                    currentBooks[index] = bookEntity
                } else {
                    currentBooks.add(bookEntity)
                }
                _books.value = currentBooks
                bookEntity
            } else {
                // Buscar en estado en memoria
                _books.value.find { it.id == id }
            }
        } catch (e: Exception) {
            // Buscar en estado en memoria
            _books.value.find { it.id == id }
        }
    }

    suspend fun searchBooks(query: String): List<BookEntity> {
        return try {
            val response = bookApi.searchBooks(query = query, page = 0, size = 100)
            if (response.isSuccessful && response.body() != null) {
                val booksDto = response.body()!!.content
                booksDto.map { it.toEntity() }
            } else {
                // Buscar en estado en memoria
                _books.value.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true) ||
                    it.isbn.contains(query, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            // Buscar en estado en memoria
            _books.value.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true) ||
                it.isbn.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun searchBooksWithFilters(query: String, categoria: String?, soloDisponibles: Boolean): List<BookEntity> {
        return try {
            val response = if (categoria != null) {
                bookApi.getBooksByCategory(categoria, page = 0, size = 100)
            } else {
                bookApi.searchBooks(query = query, page = 0, size = 100)
            }
            
            if (response.isSuccessful && response.body() != null) {
                var booksDto = response.body()!!.content
                
                if (query.isNotEmpty()) {
                    booksDto = booksDto.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true) ||
                        it.isbn.contains(query, ignoreCase = true)
                    }
                }
                
                if (soloDisponibles) {
                    booksDto = booksDto.filter { it.availableCopies > 0 }
                }
                
                booksDto.map { it.toEntity() }
            } else {
                // Filtrar en estado en memoria
                _books.value.filter { book ->
                    val matchesQuery = query.isBlank() || 
                        book.title.contains(query, ignoreCase = true) ||
                        book.author.contains(query, ignoreCase = true) ||
                        book.isbn.contains(query, ignoreCase = true)
                    val matchesCategory = categoria == null || book.categoria == categoria
                    val matchesDisponibles = !soloDisponibles || book.disponibles > 0
                    matchesQuery && matchesCategory && matchesDisponibles
                }
            }
        } catch (e: Exception) {
            // Filtrar en estado en memoria
            _books.value.filter { book ->
                val matchesQuery = query.isBlank() || 
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.isbn.contains(query, ignoreCase = true)
                val matchesCategory = categoria == null || book.categoria == categoria
                val matchesDisponibles = !soloDisponibles || book.disponibles > 0
                matchesQuery && matchesCategory && matchesDisponibles
            }
        }
    }

    suspend fun getBooksByCategory(categoria: String): List<BookEntity> {
        return try {
            val response = bookApi.getBooksByCategory(categoria, page = 0, size = 100)
            if (response.isSuccessful && response.body() != null) {
                val booksDto = response.body()!!.content
                booksDto.map { it.toEntity() }
            } else {
                // Filtrar en estado en memoria
                _books.value.filter { it.categoria == categoria }
            }
        } catch (e: Exception) {
            // Filtrar en estado en memoria
            _books.value.filter { it.categoria == categoria }
        }
    }

    suspend fun getAvailableBooks(): List<BookEntity> {
        return try {
            val response = bookApi.getBooks(page = 0, size = 100)
            if (response.isSuccessful && response.body() != null) {
                val booksDto = response.body()!!.content.filter { it.availableCopies > 0 }
                booksDto.map { it.toEntity() }
            } else {
                // Filtrar en estado en memoria
                _books.value.filter { it.disponibles > 0 }
            }
        } catch (e: Exception) {
            // Filtrar en estado en memoria
            _books.value.filter { it.disponibles > 0 }
        }
    }

    suspend fun countActiveLoansForBook(bookId: Long): Int {
        return try {
            val response = bookApi.getBookAvailability(bookId.toString())
            if (response.isSuccessful && response.body() != null) {
                val availability = response.body()!!
                availability.totalCopies - availability.availableCopies
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Sincroniza los libros desde el API (alias para loadAllBooks).
     */
    suspend fun syncBooksFromApi(): Result<Int> = loadAllBooks()
}
