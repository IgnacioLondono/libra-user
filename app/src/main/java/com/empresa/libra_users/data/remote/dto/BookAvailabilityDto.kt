package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookAvailabilityDto(
    @SerializedName("bookId")
    val bookId: String,
    val available: Boolean,
    @SerializedName("availableCopies")
    val availableCopies: Int,
    @SerializedName("totalCopies")
    val totalCopies: Int,
    val message: String?
)

