package com.empresa.libra_users.data.local.user

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    // -------------------------------------------------------------------
    // MÉTODOS DE CONSULTA Y LECTURA (READ QUERIES)
    // -------------------------------------------------------------------

    @Query("SELECT * FROM books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentBooks(limit: Int): List<BookEntity>

    @Query("SELECT * FROM books WHERE categoryId = :categoryId")
    suspend fun getBooksByCategoryId(categoryId: Long): List<BookEntity>

    @Query("SELECT * FROM books WHERE author LIKE '%' || :authorTag || '%'" )
    suspend fun getBooksByAuthorTag(authorTag: String): List<BookEntity>

    // AÑADIDO: CONSULTA PARA LA FUNCIÓN DE BÚSQUEDA
    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR isbn LIKE '%' || :query || '%'" )
    suspend fun searchBooks(query: String): List<BookEntity>

    // Búsqueda por categoría
    @Query("SELECT * FROM books WHERE categoria LIKE '%' || :categoria || '%'")
    suspend fun getBooksByCategory(categoria: String): List<BookEntity>

    // Filtro por disponibilidad
    @Query("SELECT * FROM books WHERE disponibles > 0")
    suspend fun getAvailableBooks(): List<BookEntity>

    // Búsqueda combinada con filtros
    @Query("SELECT * FROM books WHERE (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR isbn LIKE '%' || :query || '%') AND (:categoria IS NULL OR categoria LIKE '%' || :categoria || '%') AND (:soloDisponibles = 0 OR disponibles > 0)")
    suspend fun searchBooksWithFilters(query: String, categoria: String?, soloDisponibles: Int): List<BookEntity>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int

    // Contar préstamos activos de un libro
    @Query("SELECT COUNT(*) FROM loans WHERE bookId = :bookId AND status = 'Active'")
    suspend fun countActiveLoansForBook(bookId: Long): Int
}