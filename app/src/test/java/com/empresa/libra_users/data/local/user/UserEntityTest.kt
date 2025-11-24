package com.empresa.libra_users.data.local.user

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests simples para UserEntity.
 * Verificamos que los data classes funcionan correctamente.
 */
class UserEntityTest {

    @Test
    fun `UserEntity se crea correctamente con todos los campos`() {
        // Arrange & Act: Crear una entidad de usuario
        val user = UserEntity(
            id = 1L,
            name = "Juan Pérez",
            email = "juan@gmail.com",
            phone = "12345678",
            password = "Password123!",
            role = "user",
            status = "active",
            profilePictureUri = "https://example.com/profile.jpg"
        )

        // Assert: Verificar que todos los campos se asignaron correctamente
        assertEquals(1L, user.id)
        assertEquals("Juan Pérez", user.name)
        assertEquals("juan@gmail.com", user.email)
        assertEquals("12345678", user.phone)
        assertEquals("Password123!", user.password)
        assertEquals("user", user.role)
        assertEquals("active", user.status)
        assertEquals("https://example.com/profile.jpg", user.profilePictureUri)
    }

    @Test
    fun `UserEntity usa valores por defecto correctamente`() {
        // Arrange & Act: Crear una entidad con valores mínimos
        val user = UserEntity(
            name = "María García",
            email = "maria@gmail.com",
            phone = "87654321",
            password = "Password456!"
        )

        // Assert: Verificar que los valores por defecto se aplicaron
        assertEquals(0L, user.id) // Valor por defecto
        assertEquals("user", user.role) // Valor por defecto
        assertEquals("active", user.status) // Valor por defecto
        assertNull(user.profilePictureUri) // Valor por defecto (null)
    }

    @Test
    fun `UserEntity puede tener diferentes roles`() {
        val roles = listOf("user", "admin", "librarian")

        roles.forEach { role ->
            val user = UserEntity(
                name = "Usuario $role",
                email = "$role@gmail.com",
                phone = "12345678",
                password = "Password123!",
                role = role
            )

            assertEquals(role, user.role)
        }
    }

    @Test
    fun `UserEntity puede tener diferentes estados`() {
        val estados = listOf("active", "inactive", "suspended")

        estados.forEach { estado ->
            val user = UserEntity(
                name = "Usuario",
                email = "usuario@gmail.com",
                phone = "12345678",
                password = "Password123!",
                status = estado
            )

            assertEquals(estado, user.status)
        }
    }

    @Test
    fun `UserEntity puede tener profilePictureUri nulo`() {
        val user = UserEntity(
            name = "Usuario Sin Foto",
            email = "usuario@gmail.com",
            phone = "12345678",
            password = "Password123!",
            profilePictureUri = null
        )

        assertNull(user.profilePictureUri)
    }

    @Test
    fun `UserEntity puede tener profilePictureUri con valor`() {
        val uri = "content://media/external/images/media/123"
        val user = UserEntity(
            name = "Usuario Con Foto",
            email = "usuario@gmail.com",
            phone = "12345678",
            password = "Password123!",
            profilePictureUri = uri
        )

        assertEquals(uri, user.profilePictureUri)
    }
}

