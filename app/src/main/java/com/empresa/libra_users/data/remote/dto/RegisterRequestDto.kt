package com.empresa.libra_users.data.remote.dto

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val phone: String?
)

