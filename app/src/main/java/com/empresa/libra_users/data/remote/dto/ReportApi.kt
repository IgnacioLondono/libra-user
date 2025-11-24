package com.empresa.libra_users.data.remote.dto

import retrofit2.Response
import retrofit2.http.GET

interface ReportApi {
    
    @GET("api/reports/dashboard")
    suspend fun getDashboardStatistics(): Response<DashboardStatisticsDto>
}

