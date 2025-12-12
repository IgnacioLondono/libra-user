package com.empresa.libra_users.data.repository

import android.content.Context
import android.net.Uri
import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.remote.dto.LoginRequestDto
import com.empresa.libra_users.data.remote.dto.RegisterRequestDto
import com.empresa.libra_users.data.remote.dto.UpdateUserRequestDto
import com.empresa.libra_users.data.remote.dto.UserApi
import com.empresa.libra_users.data.remote.mapper.toEntity
import com.empresa.libra_users.domain.validation.*
import com.empresa.libra_users.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val userPreferences: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) {

    // Estado en memoria para los usuarios (sin Room)
    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users: StateFlow<List<UserEntity>> = _users.asStateFlow()

    suspend fun login(email: String, pass: String): Result<String> {
        // Validaciones antes de llamar al API
        val emailError = validateEmail(email.trim())
        if (emailError != null) {
            return Result.failure(IllegalArgumentException(emailError))
        }

        if (pass.isBlank()) {
            return Result.failure(IllegalArgumentException("La contraseña es obligatoria"))
        }

        // Caso especial para admin
        if (email.equals("admin123@gmail.com", ignoreCase = true) && pass == "admin12345678") {
            return Result.success("ADMIN")
        }

        return try {
            val request = LoginRequestDto(email = email, password = pass)
            val response = userApi.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                // Guardar token y datos del usuario
                userPreferences.saveAuthToken(loginResponse.token)
                userPreferences.saveUserEmail(loginResponse.user.email)
                userPreferences.saveUserRole(loginResponse.user.role)

                // Actualizar estado en memoria
                val userEntity = loginResponse.user.toEntity()
                val currentUsers = _users.value.toMutableList()
                val index = currentUsers.indexOfFirst { it.id == userEntity.id }
                if (index >= 0) {
                    currentUsers[index] = userEntity
                } else {
                    currentUsers.add(userEntity)
                }
                _users.value = currentUsers

                Result.success(loginResponse.user.role.uppercase())
            } else {
                Result.failure(IllegalArgumentException("Credenciales inválidas"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error de autenticación: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos del microservicio.
     * Si se proporciona un Uri de imagen, se convierte a Base64 antes de enviar.
     *
     * @param name Nombre del usuario
     * @param email Email del usuario
     * @param phone Teléfono del usuario (opcional)
     * @param pass Contraseña del usuario
     * @param profileImageUri Uri de la imagen de perfil (opcional)
     * @return Result<Long> con el ID del usuario creado o error
     */
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        profileImageUri: String?
    ): Result<Long> {
        // Validaciones antes de llamar al API
        val nameError = validateNameLettersOnly(name.trim())
        if (nameError != null) {
            return Result.failure(IllegalArgumentException(nameError))
        }

        val emailError = validateEmail(email.trim())
        if (emailError != null) {
            return Result.failure(IllegalArgumentException(emailError))
        }

        val phoneError = validatePhoneDigitsOnly(phone.trim())
        if (phoneError != null) {
            return Result.failure(IllegalArgumentException(phoneError))
        }

        val passError = validateStrongPassword(pass)
        if (passError != null) {
            return Result.failure(IllegalArgumentException(passError))
        }

        if (email.equals("admin123@gmail.com", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Este correo no se puede registrar."))
        }

        return try {
            // Convertir imagen a Base64 si está presente
            val profileImageBase64 = profileImageUri?.let { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    val base64 = ImageUtils.uriToBase64(
                        context = context,
                        uri = uri,
                        maxWidth = 800,
                        maxHeight = 800,
                        quality = 85
                    )

                    if (base64 == null) {
                        return Result.failure(IllegalArgumentException("No se pudo cargar la imagen. Por favor, intenta con otra imagen."))
                    }

                    // Validar el Base64 resultante
                    val imageError = validateBase64Image(base64)
                    if (imageError != null) {
                        return Result.failure(IllegalArgumentException(imageError))
                    }

                    base64
                } catch (e: Exception) {
                    // Si hay error al convertir, retornar error
                    return Result.failure(IllegalArgumentException("Error al procesar la imagen: ${e.message ?: "Formato de imagen inválido"}"))
                }
            }

            // PRIMERO: Crear usuario en la base de datos del microservicio
            val request = RegisterRequestDto(
                name = name,
                email = email,
                password = pass,
                phone = phone.ifEmpty { null },
                profileImageBase64 = profileImageBase64
            )
            val response = userApi.register(request)

            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!

                // SOLO SI ES EXITOSO: Actualizar estado en memoria
                val userEntity = userDto.toEntity().copy(
                    password = pass,
                    profilePictureUri = userDto.profileImageUri // Usar la URL del backend si existe
                )
                val currentUsers = _users.value.toMutableList()
                currentUsers.add(userEntity)
                _users.value = currentUsers

                Result.success(userEntity.id)
            } else {
                Result.failure(IllegalArgumentException("Error al registrar usuario en la base de datos"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
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

    /**
     * Carga todos los usuarios desde el API.
     */
    suspend fun loadAllUsers(): Result<Int> {
        return try {
            val response = userApi.getAllUsers()
            if (response.isSuccessful && response.body() != null) {
                val usersDto = response.body()!!
                val usersEntity = usersDto.map { it.toEntity() }
                _users.value = usersEntity
                Result.success(usersEntity.size)
            } else {
                Result.failure(IllegalArgumentException("Error al cargar usuarios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminada la función getUsers() - usar directamente la propiedad 'users'

    suspend fun getUserById(id: Long): UserEntity? {
        return try {
            val response = userApi.getUserById(id.toString())
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                val userEntity = userDto.toEntity()
                // Actualizar estado en memoria
                val currentUsers = _users.value.toMutableList()
                val index = currentUsers.indexOfFirst { it.id == id }
                if (index >= 0) {
                    currentUsers[index] = userEntity
                } else {
                    currentUsers.add(userEntity)
                }
                _users.value = currentUsers
                userEntity
            } else {
                // Buscar en estado en memoria
                _users.value.find { it.id == id }
            }
        } catch (e: Exception) {
            // Buscar en estado en memoria
            _users.value.find { it.id == id }
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        // Primero buscar en estado en memoria
        val user = _users.value.find { it.email.equals(email, ignoreCase = true) }
        if (user != null) return user

        // Si no está, intentar obtener del API
        return try {
            // Nota: El API podría no tener un endpoint para buscar por email
            // En ese caso, cargar todos los usuarios
            loadAllUsers()
            _users.value.find { it.email.equals(email, ignoreCase = true) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualiza un usuario en la base de datos del microservicio.
     * Si se proporciona un Uri de imagen nuevo, se convierte a Base64 antes de enviar.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     *
     * @param user Usuario con los datos actualizados
     * @param newProfileImageUri Uri de la nueva imagen de perfil (opcional, si es diferente a la actual)
     */
    suspend fun updateUser(user: UserEntity, newProfileImageUri: String? = null): Result<Unit> {
        // Validaciones antes de llamar al API
        val idError = validateId(user.id)
        if (idError != null) {
            return Result.failure(IllegalArgumentException(idError))
        }

        val nameError = validateNameLettersOnly(user.name?.trim() ?: "")
        if (nameError != null) {
            return Result.failure(IllegalArgumentException(nameError))
        }

        val emailError = validateEmail(user.email?.trim() ?: "")
        if (emailError != null) {
            return Result.failure(IllegalArgumentException(emailError))
        }

        val phoneError = user.phone?.let { if (it.isNotBlank()) validatePhoneDigitsOnly(it.trim()) else null }
        if (phoneError != null) {
            return Result.failure(IllegalArgumentException(phoneError))
        }

        // Validar imagen si está presente
        val imageError = newProfileImageUri?.let { validateBase64Image(it) }
        if (imageError != null) {
            return Result.failure(IllegalArgumentException(imageError))
        }

        return try {
            if (user.id > 0) {
                // Convertir nueva imagen a Base64 si se proporciona un Uri nuevo
                val profileImageBase64 = newProfileImageUri?.let { uriString ->
                    try {
                        val uri = Uri.parse(uriString)
                        ImageUtils.uriToBase64(
                            context = context,
                            uri = uri,
                            maxWidth = 800,
                            maxHeight = 800,
                            quality = 85
                        ) ?: null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                // PRIMERO: Intentar actualizar en el API/base de datos
                val updateRequest = UpdateUserRequestDto(
                    name = user.name,
                    phone = user.phone.ifEmpty { null },
                    profileImageUri = user.profilePictureUri, // Mantener la URI existente si no hay nueva
                    profileImageBase64 = profileImageBase64 // Enviar Base64 si hay nueva imagen
                )
                val response = userApi.updateUser(user.id.toString(), updateRequest)

                if (response.isSuccessful && response.body() != null) {
                    val updatedDto = response.body()!!
                    val updatedEntity = updatedDto.toEntity().copy(
                        id = user.id,
                        password = user.password // Mantener contraseña local
                    )
                    // SOLO SI ES EXITOSO: Actualizar estado en memoria
                    val currentUsers = _users.value.toMutableList()
                    val index = currentUsers.indexOfFirst { it.id == user.id }
                    if (index >= 0) {
                        currentUsers[index] = updatedEntity
                    } else {
                        currentUsers.add(updatedEntity)
                    }
                    _users.value = currentUsers
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalArgumentException("Error al actualizar usuario en la base de datos"))
                }
            } else {
                Result.failure(IllegalArgumentException("ID de usuario inválido"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }

    suspend fun countUsers(): Int {
        if (_users.value.isEmpty()) {
            loadAllUsers()
        }
        return _users.value.size
    }

    /**
     * Elimina un usuario de la base de datos del microservicio.
     * Solo actualiza el estado en memoria si la operación en el API es exitosa.
     */
    suspend fun deleteUser(user: UserEntity): Result<Unit> {
        // Validaciones antes de llamar al API
        val idError = validateId(user.id)
        if (idError != null) {
            return Result.failure(IllegalArgumentException(idError))
        }

        return try {
            if (user.id > 0) {
                // PRIMERO: Intentar eliminar en el API/base de datos
                val response = userApi.deleteUser(user.id.toString())

                if (response.isSuccessful) {
                    // SOLO SI ES EXITOSO: Eliminar del estado en memoria
                    _users.value = _users.value.filter { it.id != user.id }
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalArgumentException("Error al eliminar usuario en la base de datos"))
                }
            } else {
                Result.failure(IllegalArgumentException("ID de usuario inválido"))
            }
        } catch (e: IOException) {
            Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(IllegalArgumentException("Error HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Error inesperado: ${e.message}"))
        }
    }
}