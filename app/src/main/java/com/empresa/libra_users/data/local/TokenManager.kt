package com.empresa.libra_users.data.local

import com.empresa.libra_users.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenManager: Gestiona el almacenamiento y recuperación del token JWT
 * de forma segura usando DataStore a través de UserPreferencesRepository.
 * 
 * Este manager proporciona una interfaz simple para:
 * - Guardar tokens JWT después del login
 * - Recuperar tokens para usar en peticiones HTTP
 * - Limpiar tokens al hacer logout
 * - Observar cambios en el token mediante Flow
 */
@Singleton
class TokenManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Flow que emite el token actual. Emite null si no hay token guardado.
     * Útil para observar cambios en el estado de autenticación.
     */
    val token: Flow<String?> = userPreferencesRepository.authToken
    
    /**
     * Guarda el token JWT después de un login exitoso.
     * @param token El token JWT recibido del servidor
     */
    suspend fun saveToken(token: String) {
        userPreferencesRepository.saveAuthToken(token)
    }
    
    /**
     * Obtiene el token actual de forma síncrona (suspend).
     * @return El token JWT o null si no existe
     */
    suspend fun getToken(): String? {
        return userPreferencesRepository.getAuthToken()
    }
    
    /**
     * Obtiene el token con el formato "Bearer <token>" requerido por el header Authorization.
     * @return "Bearer <token>" o null si no hay token
     */
    suspend fun getBearerToken(): String? {
        return userPreferencesRepository.getBearerToken()
    }
    
    /**
     * Limpia el token almacenado. Se usa al hacer logout.
     */
    suspend fun clearToken() {
        userPreferencesRepository.clearAll()
    }
    
    /**
     * Verifica si hay un token guardado.
     * @return true si existe un token, false en caso contrario
     */
    suspend fun hasToken(): Boolean {
        return getToken() != null
    }
}


