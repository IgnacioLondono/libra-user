package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateLoanRequestDto(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("bookId")
    val bookId: String,
    @SerializedName("loanDays")
    val loanDays: Int
)

