package com.empresa.libra_users.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Se crea la instancia de DataStore a nivel de top-level, asociada al contexto de la app.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    // 2. Se definen las claves para cada valor que queremos guardar.
    private companion object {
        // CLAVE ANTIGUA ELIMINADA: val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email") // <-- NUEVA CLAVE
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ROLE = stringPreferencesKey("user_role")
        val SPLASH_SHOWN = booleanPreferencesKey("splash_shown")
    }

    // 3. Se exponen los valores como Flows para que la UI pueda reaccionar a los cambios.
    // FLOW ANTIGUO ELIMINADO: val isLoggedIn: Flow<Boolean> = ...

    // NUEVO FLOW para el email del usuario. Será null si no hay sesión.
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL]
        }

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN]
        }

    val userRole: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ROLE]
        }

    val splashShown: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SPLASH_SHOWN] ?: false
        }

    // 4. Se crean funciones suspend para modificar los datos de forma segura.
    // FUNCIÓN ANTIGUA ELIMINADA: suspend fun setLoggedIn(isLoggedIn: Boolean)

    // NUEVA FUNCIÓN para guardar el email del usuario al hacer login
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Helper para obtener el token de forma sincrónica (suspend)
    suspend fun getAuthToken(): String? {
        return authToken.first()
    }
    
    // Helper para obtener el token con formato Bearer
    suspend fun getBearerToken(): String? {
        val token = getAuthToken()
        return token?.let { "Bearer $it" }
    }
    
    // Función para marcar que el splash ya se mostró
    suspend fun setSplashShown() {
        context.dataStore.edit { preferences ->
            preferences[SPLASH_SHOWN] = true
        }
    }
    
    // Función para obtener si el splash ya se mostró
    suspend fun hasSplashShown(): Boolean {
        return splashShown.first()
    }
}
