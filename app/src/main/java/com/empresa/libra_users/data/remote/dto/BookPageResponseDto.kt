package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookPageResponseDto(
    val content: List<BookDto>,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("totalPages")
    val totalPages: Int,
    val size: Int,
    val number: Int
)

