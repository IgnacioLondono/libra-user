package com.empresa.libra_users.data.remote.dto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LoanApi {
    
    @POST("api/loans")
    suspend fun createLoan(
        @Body request: CreateLoanRequestDto
    ): Response<LoanDto>
    
    @GET("api/loans/user/{userId}")
    suspend fun getUserLoans(
        @Path("userId") userId: String
    ): Response<List<LoanDto>>
    
    @GET("api/loans/user/{userId}/active")
    suspend fun getActiveUserLoans(
        @Path("userId") userId: String
    ): Response<List<LoanDto>>
    
    @POST("api/loans/{loanId}/return")
    suspend fun returnLoan(
        @Path("loanId") loanId: String
    ): Response<LoanDto>
    
    @PATCH("api/loans/{loanId}/extend")
    suspend fun extendLoan(
        @Path("loanId") loanId: String
    ): Response<LoanDto>
}

