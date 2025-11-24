package com.empresa.libra_users.data.remote.mapper

import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.remote.dto.UserDto
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests simples para el mapeo de UserDto a UserEntity y viceversa.
 */
class UserMapperTest {

    @Test
    fun `toEntity convierte UserDto a UserEntity correctamente`() {
        // Arrange: Crear un UserDto de ejemplo
        val userDto = UserDto(
            id = "1",
            name = "Juan Pérez",
            email = "juan@gmail.com",
            phone = "12345678",
            role = "user",
            status = "active",
            profileImageUri = "https://example.com/profile.jpg",
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        // Act: Convertir a Entity
        val userEntity = userDto.toEntity()

        // Assert: Verificar que los campos se mapearon correctamente
        assertEquals(1L, userEntity.id)
        assertEquals("Juan Pérez", userEntity.name)
        assertEquals("juan@gmail.com", userEntity.email)
        assertEquals("12345678", userEntity.phone)
        assertEquals("user", userEntity.role)
        assertEquals("active", userEntity.status)
        assertEquals("https://example.com/profile.jpg", userEntity.profilePictureUri)
        assertEquals("", userEntity.password) // La contraseña no viene del backend
    }

    @Test
    fun `toEntity maneja valores nulos correctamente`() {
        // Arrange: UserDto con teléfono y foto nulos
        val userDto = UserDto(
            id = "2",
            name = "María García",
            email = "maria@gmail.com",
            phone = null,
            role = "admin",
            status = "active",
            profileImageUri = null,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        // Act
        val userEntity = userDto.toEntity()

        // Assert: Verificar que los valores nulos se manejan correctamente
        assertEquals("", userEntity.phone)
        assertNull(userEntity.profilePictureUri)
    }

    @Test
    fun `toEntity maneja ID inválido correctamente`() {
        val userDto = UserDto(
            id = "invalid",
            name = "Test User",
            email = "test@gmail.com",
            phone = "12345678",
            role = "user",
            status = "active",
            profileImageUri = null,
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        val userEntity = userDto.toEntity()

        assertEquals(0L, userEntity.id) // ID inválido se convierte a 0
    }

    @Test
    fun `toDto convierte UserEntity a UpdateUserRequestDto correctamente`() {
        // Arrange: Crear un UserEntity
        val userEntity = UserEntity(
            id = 1L,
            name = "Juan Pérez",
            email = "juan@gmail.com",
            phone = "12345678",
            password = "password123",
            role = "user",
            status = "active",
            profilePictureUri = "https://example.com/profile.jpg"
        )

        // Act: Convertir a Dto
        val updateDto = userEntity.toDto()

        // Assert: Verificar que los campos se mapearon correctamente
        assertEquals("Juan Pérez", updateDto.name)
        assertEquals("12345678", updateDto.phone)
        assertEquals("https://example.com/profile.jpg", updateDto.profileImageUri)
    }

    @Test
    fun `toDto maneja valores vacíos correctamente`() {
        val userEntity = UserEntity(
            id = 2L,
            name = "María García",
            email = "maria@gmail.com",
            phone = "",
            password = "password123",
            role = "admin",
            status = "active",
            profilePictureUri = null
        )

        val updateDto = userEntity.toDto()

        assertNull(updateDto.phone) // Teléfono vacío se convierte a null
        assertNull(updateDto.profileImageUri) // Foto nula se mantiene nula
    }
}

