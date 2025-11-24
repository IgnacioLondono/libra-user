package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.user.UserDao
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.remote.dto.LoginRequestDto
import com.empresa.libra_users.data.remote.dto.RegisterRequestDto
import com.empresa.libra_users.data.remote.dto.UpdateUserRequestDto
import com.empresa.libra_users.data.remote.dto.UserApi
import com.empresa.libra_users.data.remote.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val userPreferences: UserPreferencesRepository
) {
    
    suspend fun login(email: String, pass: String): Result<String> {
        // Caso especial para admin
        if (email.equals("admin123@gmail.com", ignoreCase = true) && pass == "admin12345678") {
            return Result.success("ADMIN")
        }
        
        return try {
            // Intentar login con API REST
            val request = LoginRequestDto(email = email, password = pass)
            val response = userApi.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                // Guardar token y datos del usuario
                userPreferences.saveAuthToken(loginResponse.token)
                userPreferences.saveUserEmail(loginResponse.user.email)
                userPreferences.saveUserRole(loginResponse.user.role)
                
                // Guardar usuario en Room local como caché
                val userEntity = loginResponse.user.toEntity()
                userDao.insert(userEntity)
                
                Result.success(loginResponse.user.role.uppercase())
            } else {
                // Fallback a Room local si la API falla
                val user = userDao.getByEmail(email)
                if (user != null && user.status == "blocked") {
                    return Result.failure(IllegalArgumentException("Tu cuenta ha sido bloqueada."))
                }
                return if (user != null && user.password == pass) {
                    Result.success("USER")
                } else {
                    Result.failure(IllegalArgumentException("Credenciales Inválidas"))
                }
            }
        } catch (e: IOException) {
            // Error de red, fallback a Room local
            val user = userDao.getByEmail(email)
            if (user != null && user.status == "blocked") {
                return Result.failure(IllegalArgumentException("Tu cuenta ha sido bloqueada."))
            }
            return if (user != null && user.password == pass) {
                Result.success("USER")
            } else {
                Result.failure(IllegalArgumentException("Credenciales Inválidas"))
            }
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error de autenticación: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }

    suspend fun register(name: String, email: String, phone: String, pass: String, profilePictureUri: String?): Result<Long> {
        if (email.equals("admin123@gmail.com", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Este correo no se puede registrar."))
        }
        
        return try {
            // Intentar registro con API REST
            val request = RegisterRequestDto(
                name = name,
                email = email,
                password = pass,
                phone = phone.ifEmpty { null }
            )
            val response = userApi.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                
                // Guardar usuario en Room local como caché
                val userEntity = userDto.toEntity().copy(password = pass)
                val id = userDao.insert(userEntity)
                
                Result.success(id)
            } else {
                // Fallback a Room local si la API falla
                if (userDao.getByEmail(email) != null) {
                    return Result.failure(IllegalArgumentException("Correo ya existente"))
                }
                val id = userDao.insert(UserEntity(
                    name = name,
                    email = email,
                    phone = phone,
                    password = pass,
                    profilePictureUri = profilePictureUri
                ))
                Result.success(id)
            }
        } catch (e: IOException) {
            // Error de red, fallback a Room local
            if (userDao.getByEmail(email) != null) {
                return Result.failure(IllegalArgumentException("Correo ya existente"))
            }
            val id = userDao.insert(UserEntity(
                name = name,
                email = email,
                phone = phone,
                password = pass,
                profilePictureUri = profilePictureUri
            ))
            Result.success(id)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                409 -> "Correo ya existente"
                else -> "Error al registrar: ${e.message()}"
            }
            Result.failure(IllegalArgumentException(errorMessage))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }

    fun getUsers(): Flow<List<UserEntity>> = userDao.getAll()

    suspend fun getUserById(id: Long): UserEntity? {
        return try {
            // El AuthInterceptor agregará automáticamente el token si existe
            val response = userApi.getUserById(id.toString())
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                val userEntity = userDto.toEntity()
                // Actualizar caché local
                userDao.insert(userEntity)
                userEntity
            } else {
                // Fallback a Room local
                userDao.getById(id)
            }
        } catch (e: Exception) {
            // Error, usar Room local
            userDao.getById(id)
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getByEmail(email)

    suspend fun updateUser(user: UserEntity): Result<Unit> {
        return try {
            if (user.id > 0) {
                // Intentar actualizar en API REST
                // El AuthInterceptor agregará automáticamente el token si existe
                val updateRequest = UpdateUserRequestDto(
                    name = user.name,
                    phone = user.phone.ifEmpty { null },
                    profileImageUri = user.profilePictureUri
                )
                val response = userApi.updateUser(user.id.toString(), updateRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val updatedDto = response.body()!!
                    val updatedEntity = updatedDto.toEntity().copy(
                        id = user.id,
                        password = user.password // Mantener contraseña local
                    )
                    // Actualizar caché local
                    userDao.update(updatedEntity)
                    Result.success(Unit)
                } else {
                    // Fallback a Room local
                    userDao.update(user)
                    Result.success(Unit)
                }
            } else {
                // Usuario nuevo, solo actualizar local
                userDao.update(user)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Error, actualizar solo local
            userDao.update(user)
            Result.success(Unit)
        }
    }

    suspend fun countUsers(): Int = userDao.countUsers()
}
