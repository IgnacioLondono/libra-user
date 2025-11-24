package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val id: String,
    @SerializedName("userId")
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val priority: String?,
    @SerializedName("createdAt")
    val createdAt: String
)

