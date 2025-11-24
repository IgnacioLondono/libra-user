package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.user.UserDao
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.remote.dto.LoginRequestDto
import com.empresa.libra_users.data.remote.dto.LoginResponseDto
import com.empresa.libra_users.data.remote.dto.RegisterRequestDto
import com.empresa.libra_users.data.remote.dto.UpdateUserRequestDto
import com.empresa.libra_users.data.remote.dto.UserApi
import com.empresa.libra_users.data.remote.dto.UserDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UserRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var userApi: UserApi
    private lateinit var userPreferences: UserPreferencesRepository
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        userDao = mockk(relaxed = true)
        userApi = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true)
        userRepository = UserRepository(userDao, userApi, userPreferences)
    }

    @Test
    fun `login con admin debería retornar ADMIN`() = runTest {
        // Act
        val result = userRepository.login("admin123@gmail.com", "admin12345678")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("ADMIN", result.getOrNull())
    }

    @Test
    fun `login con API exitosa debería retornar rol del usuario`() = runTest {
        // Arrange
        val userDto = UserDto(
            id = "1",
            name = "Test User",
            email = "test@test.com",
            phone = "123456789",
            role = "USER",
            status = "active",
            profileImageUri = null,
            createdAt = "",
            updatedAt = ""
        )
        val loginResponse = LoginResponseDto(token = "test-token", user = userDto, expiresIn = 3600L)
        val response = Response.success(loginResponse)
        coEvery { userApi.login(any()) } returns response
        coEvery { userPreferences.saveAuthToken(any()) } returns Unit
        coEvery { userPreferences.saveUserEmail(any()) } returns Unit
        coEvery { userPreferences.saveUserRole(any()) } returns Unit
        coEvery { userDao.insert(any()) } returns 1L

        // Act
        val result = userRepository.login("test@test.com", "password")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("USER", result.getOrNull())
    }

    @Test
    fun `login con error de red debería hacer fallback a Room`() = runTest {
        // Arrange
        val user = UserEntity(
            id = 1L,
            name = "Test User",
            email = "test@test.com",
            phone = "123456789",
            password = "password",
            status = "active"
        )
        coEvery { userApi.login(any()) } throws IOException("Network error")
        coEvery { userDao.getByEmail("test@test.com") } returns user

        // Act
        val result = userRepository.login("test@test.com", "password")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("USER", result.getOrNull())
    }

    @Test
    fun `login con usuario bloqueado debería retornar error`() = runTest {
        // Arrange
        val user = UserEntity(
            id = 1L,
            name = "Test User",
            email = "test@test.com",
            phone = "123456789",
            password = "password",
            status = "blocked"
        )
        coEvery { userApi.login(any()) } throws IOException("Network error")
        coEvery { userDao.getByEmail("test@test.com") } returns user

        // Act
        val result = userRepository.login("test@test.com", "password")

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `register con API exitosa debería retornar ID`() = runTest {
        // Arrange
        val userDto = UserDto(
            id = "1",
            name = "Test User",
            email = "test@test.com",
            phone = "123456789",
            role = "USER",
            status = "active",
            profileImageUri = null,
            createdAt = "",
            updatedAt = ""
        )
        val response = Response.success(userDto)
        coEvery { userApi.register(any()) } returns response
        coEvery { userDao.insert(any()) } returns 1L

        // Act
        val result = userRepository.register("Test User", "test@test.com", "123456789", "password", null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `register con email de admin debería retornar error`() = runTest {
        // Act
        val result = userRepository.register("Admin", "admin123@gmail.com", "123456789", "password", null)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `getUsers debería retornar Flow de usuarios`() = runTest {
        // Arrange
        val users = listOf(
            UserEntity(id = 1L, name = "User 1", email = "user1@test.com", phone = "123", password = "pass", status = "active"),
            UserEntity(id = 2L, name = "User 2", email = "user2@test.com", phone = "456", password = "pass", status = "active")
        )
        every { userDao.getAll() } returns flowOf(users)

        // Act
        val result = userRepository.getUsers()

        // Assert
        result.collect { collectedUsers ->
            assertEquals(2, collectedUsers.size)
        }
    }

    @Test
    fun `getUserById con API exitosa debería retornar usuario`() = runTest {
        // Arrange
        val userDto = UserDto(
            id = "1",
            name = "Test User",
            email = "test@test.com",
            phone = "123456789",
            role = "USER",
            status = "active",
            profileImageUri = null,
            createdAt = "",
            updatedAt = ""
        )
        val response = Response.success(userDto)
        coEvery { userApi.getUserById("1") } returns response
        coEvery { userDao.insert(any()) } returns 1L

        // Act
        val result = userRepository.getUserById(1L)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `getUserByEmail debería retornar usuario`() = runTest {
        // Arrange
        val user = UserEntity(id = 1L, name = "Test", email = "test@test.com", phone = "123", password = "pass", status = "active")
        coEvery { userDao.getByEmail("test@test.com") } returns user

        // Act
        val result = userRepository.getUserByEmail("test@test.com")

        // Assert
        assertNotNull(result)
        assertEquals("test@test.com", result?.email)
    }

    @Test
    fun `countUsers debería retornar número de usuarios`() = runTest {
        // Arrange
        coEvery { userDao.countUsers() } returns 10

        // Act
        val result = userRepository.countUsers()

        // Assert
        assertEquals(10, result)
    }
}

