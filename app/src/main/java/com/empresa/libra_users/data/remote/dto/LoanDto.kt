package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoanDto(
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("bookId")
    val bookId: String,
    @SerializedName("loanDate")
    val loanDate: String,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("returnDate")
    val returnDate: String?,
    val status: String,
    @SerializedName("loanDays")
    val loanDays: Int,
    @SerializedName("fineAmount")
    val fineAmount: Double?,
    @SerializedName("extensionsCount")
    val extensionsCount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

