package com.empresa.libra_users.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.domain.validation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.empresa.libra_users.ui.state.LoginUiState
import com.empresa.libra_users.ui.state.RegisterUiState


// ESTADOS DE UI (sin cambios)

// ANOTACIÓN PARA QUE HILT PUEDA CREAR ESTE VIEWMODEL
@HiltViewModel
// INYECTAMOS EL REPOSITORIO EN EL CONSTRUCTOR
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Los companion object y las listas estáticas se han movido al Repositorio.

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow() // Usar asStateFlow() es más idiomático

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    // --- LOGIN ---
    // (El código de los handlers como onLoginEmailChange no cambia)
    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false, userRole = null) }

            // Delegamos la lógica al repositorio
            val result = authRepository.login(s.email, s.pass)

            _login.update {
                it.copy(
                    isSubmitting = false,
                    success = result.isSuccess,
                    errorMsg = result.exceptionOrNull()?.message,
                    userRole = result.getOrNull()
                )
            }
        }
    }

    // --- REGISTRO ---
    // (Los handlers como onNameChange tampoco cambian, pero el submit sí)
    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            // Creamos el objeto DemoUser
            val newUser = DemoUser(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                pass = s.pass
            )

            // Delegamos la lógica al repositorio
            val result = authRepository.register(newUser)

            _register.update {
                it.copy(
                    isSubmitting = false,
                    success = result.isSuccess,
                    errorMsg = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // --- FUNCIONES DE LIMPIEZA Y CÁLCULO (sin cambios) ---
    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null, userRole = null) }
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }
}