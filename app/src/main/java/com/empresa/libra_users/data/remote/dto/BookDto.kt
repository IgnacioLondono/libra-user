package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookDto(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val category: String,
    val publisher: String?,
    val year: Int?,
    val description: String?,
    @SerializedName("coverUrl")
    val coverUrl: String?,
    val status: String,
    @SerializedName("totalCopies")
    val totalCopies: Int,
    @SerializedName("availableCopies")
    val availableCopies: Int,
    val price: Double?,
    val featured: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

