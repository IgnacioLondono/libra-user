package com.empresa.libra_users.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateUserRequestDto(
    val name: String?,
    val phone: String?,
    @SerializedName("profileImageUri")
    val profileImageUri: String? = null,
    @SerializedName("profileImageBase64")
    val profileImageBase64: String? = null
)

