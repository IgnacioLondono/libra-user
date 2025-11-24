package com.empresa.libra_users.data.remote.dto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    
    @POST("api/users/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<UserDto>
    
    @POST("api/users/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>
    
    @GET("api/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: String
    ): Response<UserDto>
    
    @PUT("api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body request: UpdateUserRequestDto
    ): Response<UserDto>
    
    @POST("api/users/validate-token")
    suspend fun validateToken(
        @Body request: TokenValidationRequestDto
    ): Response<TokenValidationResponseDto>
    
    @POST("api/users/logout")
    suspend fun logout(): Response<Unit>
}

