package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    val token: String,
    val user: UserDto,
    @SerializedName("expiresIn")
    val expiresIn: Long
)

