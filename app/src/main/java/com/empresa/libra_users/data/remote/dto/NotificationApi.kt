package com.empresa.libra_users.data.remote.dto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    
    @GET("api/notifications/user/{userId}")
    suspend fun getUserNotifications(
        @Path("userId") userId: String,
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Response<List<NotificationDto>>
    
    @GET("api/notifications/user/{userId}/unread-count")
    suspend fun getUnreadNotificationCount(
        @Path("userId") userId: String
    ): Response<Int>
    
    @PATCH("api/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(
        @Path("notificationId") notificationId: String
    ): Response<NotificationDto>
    
    @PATCH("api/notifications/user/{userId}/read-all")
    suspend fun markAllAsRead(
        @Path("userId") userId: String
    ): Response<Unit>
    
    @GET("api/notifications/{notificationId}")
    suspend fun getNotificationById(
        @Path("notificationId") notificationId: String
    ): Response<NotificationDto>
    
    @PATCH("api/notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String
    ): Response<Unit>
    
    @PATCH("api/notifications/user/{userId}/delete-all")
    suspend fun deleteAllNotifications(
        @Path("userId") userId: String
    ): Response<Unit>
}

