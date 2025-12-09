package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    @SerializedName("profileImageBase64")
    val profileImageBase64: String? = null
)

