package com.empresa.libra_users.data.remote.mapper

import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.remote.dto.UserDto

fun UserDto.toEntity(): UserEntity {
        return UserEntity(
            id = id.toLongOrNull() ?: 0L,
            name = name,
            email = email,
            phone = phone ?: "",
            password = "", // La contraseña no viene del backend por seguridad
            role = role,
            status = status,
            profilePictureUri = profileImageUri
        )
    }
    
fun UserEntity.toDto(): com.empresa.libra_users.data.remote.dto.UpdateUserRequestDto {
    return com.empresa.libra_users.data.remote.dto.UpdateUserRequestDto(
        name = name,
        phone = phone.ifEmpty { null },
        profileImageUri = profilePictureUri
    )
}

