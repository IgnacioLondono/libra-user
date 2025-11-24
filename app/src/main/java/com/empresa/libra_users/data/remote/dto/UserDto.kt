package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val status: String,
    @SerializedName("profileImageUri")
    val profileImageUri: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

