package com.empresa.libra_users.data.remote.dto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApi {
    
    @GET("api/books")
    suspend fun getBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "title",
        @Query("sortDir") sortDir: String = "ASC"
    ): Response<BookPageResponseDto>
    
    @GET("api/books/{bookId}")
    suspend fun getBookById(
        @Path("bookId") bookId: String
    ): Response<BookDto>
    
    @POST("api/books")
    suspend fun createBook(
        @Body book: BookDto
    ): Response<BookDto>
    
    @PUT("api/books/{bookId}")
    suspend fun updateBook(
        @Path("bookId") bookId: String,
        @Body book: BookDto
    ): Response<BookDto>
    
    @DELETE("api/books/{bookId}")
    suspend fun deleteBook(
        @Path("bookId") bookId: String
    ): Response<Unit>
    
    @GET("api/books/search")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<BookPageResponseDto>
    
    @GET("api/books/{bookId}/availability")
    suspend fun getBookAvailability(
        @Path("bookId") bookId: String
    ): Response<BookAvailabilityDto>
    
    @GET("api/books/featured")
    suspend fun getFeaturedBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<BookPageResponseDto>
    
    @GET("api/books/category/{category}")
    suspend fun getBooksByCategory(
        @Path("category") category: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<BookPageResponseDto>
}

