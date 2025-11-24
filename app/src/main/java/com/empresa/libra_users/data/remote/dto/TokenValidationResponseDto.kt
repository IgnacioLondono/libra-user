package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TokenValidationResponseDto(
    val token: String?,
    val valid: Boolean,
    @SerializedName("userId")
    val userId: String?,
    val message: String?
)

