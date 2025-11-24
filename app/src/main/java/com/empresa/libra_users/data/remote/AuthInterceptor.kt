package com.empresa.libra_users.data.remote

import com.empresa.libra_users.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthInterceptor: Interceptor de OkHttp que agrega automáticamente
 * el token JWT a todas las peticiones HTTP que lo requieran.
 * 
 * Funcionamiento:
 * - Lee el token del TokenManager
 * - Lo agrega como header: Authorization: Bearer <token>
 * - Se excluyen las rutas de login y registro (no requieren autenticación)
 * 
 * Rutas excluidas (no se agrega token):
 * - /api/users/login
 * - /api/users/register
 * - /api/users/validate-token
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    /**
     * Lista de rutas que NO requieren autenticación.
     * Estas rutas no recibirán el header Authorization.
     */
    private val excludedPaths = listOf(
        "/api/users/login",
        "/api/users/register",
        "/api/users/validate-token"
    )
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        
        // Verificar si la ruta está excluida (no requiere autenticación)
        val isExcluded = excludedPaths.any { excludedPath ->
            url.contains(excludedPath, ignoreCase = true)
        }
        
        // Si la ruta está excluida, continuar sin agregar el token
        if (isExcluded) {
            return chain.proceed(originalRequest)
        }
        
        // Obtener el token de forma síncrona (usando runBlocking porque
        // el interceptor no es suspend)
        val bearerToken = runBlocking {
            tokenManager.getBearerToken()
        }
        
        // Si no hay token, continuar sin agregarlo
        // (el servidor responderá con 401 si la ruta requiere autenticación)
        if (bearerToken == null) {
            return chain.proceed(originalRequest)
        }
        
        // Crear una nueva petición con el header Authorization
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", bearerToken)
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}


