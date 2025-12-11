package com.empresa.libra_users.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import com.empresa.libra_users.domain.validation.*
import com.empresa.libra_users.ui.state.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val profileImageUri: String? = null, // Campo para la foto de perfil
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class HomeUiState(
    val currentCoverIndex: Int = 0,
    val coverImages: List<String> = emptyList(),
    val featuredBooks: List<BookEntity> = emptyList(),
    val categorizedBooks: Map<String, List<BookEntity>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

data class SearchUiState(
    val query: String = "",
    val results: List<BookEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val initialSearchPerformed: Boolean = false
)

data class UpdateUserUiState(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUri: String? = null, // Campo para la foto de perfil
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val showVerificationDialog: Boolean = false,
    val verificationCode: String = "",
    val isVerifying: Boolean = false,
    val verificationError: String? = null
)

data class CartItem(
    val book: BookEntity,
    val loanDays: Int = 7
) {
    val price: Double
        get() = loanDays * 0.15
}

// Data class for combined loan and book details
data class ActiveLoanDetails(
    val book: BookEntity,
    val loan: LoanEntity
)

enum class AuthState {
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository,
    private val notificationRepository: NotificationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    private val _home = MutableStateFlow(HomeUiState())
    val home: StateFlow<HomeUiState> = _home.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _updateUserState = MutableStateFlow(UpdateUserUiState())
    val updateUserState: StateFlow<UpdateUserUiState> = _updateUserState.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _activeLoans = MutableStateFlow<List<ActiveLoanDetails>>(emptyList())
    val activeLoans: StateFlow<List<ActiveLoanDetails>> = _activeLoans.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    val splashShown: StateFlow<Boolean> = userPreferencesRepository.splashShown.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private var searchDebounceJob: Job? = null
    private val searchDebounceMillis = 500L

    init {
        checkAuthStatus()
        syncBooksFromApi()
        loadCategorizedBooks()
        // Los datos ahora se obtienen desde los microservicios
        // No se cargan datos precargados localmente
    }
    
    /**
     * Sincroniza los libros desde el API al iniciar la app.
     * Esto asegura que los datos se obtengan desde los microservicios.
     */
    private fun syncBooksFromApi() {
        viewModelScope.launch {
            try {
                bookRepository.loadAllBooks()
            } catch (e: Exception) {
                // Error silencioso - se mostrará en la UI si es necesario
            }
        }
    }

    private fun checkAuthStatus() {
        userPreferencesRepository.userEmail
            .onEach { email ->
                if (email == null) {
                    _authState.value = AuthState.UNAUTHENTICATED
                    _user.value = null
                } else {
                    loadUserSession(email)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUserSession(email: String) {
        // *** Special case for admin user ***
        if (email.equals("admin123@gmail.com", ignoreCase = true)) {
            _user.value = UserEntity(id = -1, name = "Admin", email = email, password = "", phone = "")
            _authState.value = AuthState.AUTHENTICATED
            return // Stop execution here for admin
        }

        // Existing logic for regular users
        viewModelScope.launch {
            try {
                val userProfile = userRepository.getUserByEmail(email)
                if (userProfile != null) {
                    _user.value = userProfile
                    _authState.value = AuthState.AUTHENTICATED
                    loadActiveLoans() // Load loans when user session is loaded
                } else {
                    logout()
                }
            } catch (e: Exception) {
                logout()
            }
        }
    }

    fun loadCategorizedBooks() {
        viewModelScope.launch {
            _home.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                // Cargar libros desde el API si no están cargados
                if (bookRepository.books.value.isEmpty()) {
                    bookRepository.loadAllBooks()
                }
                
                bookRepository.getAllBooksFlow()
                    .collect { books ->
                        val categorized = books.groupBy { book ->
                            when (book.categoryId) {
                                1L -> "Clásicos universales"
                                2L -> "Ciencia ficción y fantasía"
                                3L -> "Romance y drama"
                                4L -> "Misterio y suspenso"
                                else -> "Otros"
                            }
                        }
                        _home.update {
                            it.copy(
                                isLoading = false,
                                categorizedBooks = categorized,
                                featuredBooks = books.shuffled().take(6)
                            )
                        }
                    }
            } catch (e: Exception) {
                _home.update { it.copy(isLoading = false, errorMsg = "Error al cargar libros: ${e.message}") }
            }
        }
    }

    fun loadActiveLoans() {
        _user.value?.let { user ->
            viewModelScope.launch {
                try {
                    // Cargar préstamos del usuario desde el API
                    loanRepository.loadUserLoans(user.id)
                    
                    loanRepository.getLoansByUser(user.id)
                        .collect { loans ->
                            val activeLoansList = loans.filter { it.status == "Active" }
                            val loanDetails = activeLoansList.mapNotNull { loan ->
                                bookRepository.getBookById(loan.bookId)?.let { book ->
                                    ActiveLoanDetails(book, loan)
                                }
                            }
                            _activeLoans.value = loanDetails
                        }
                } catch (e: Exception) {
                    _home.update { it.copy(errorMsg = "Error al cargar préstamos: ${e.message}") }
                }
            }
        }
    }


    suspend fun getBookById(bookId: Long): BookEntity? {
        return bookRepository.getBookById(bookId)
    }

    fun addToCart(book: BookEntity) {
        _cart.update { currentCart ->
            if (currentCart.any { it.book.id == book.id }) {
                currentCart
            } else {
                currentCart + CartItem(book = book)
            }
        }
    }

    fun removeFromCart(bookId: Long) {
        _cart.update { currentCart ->
            currentCart.filterNot { it.book.id == bookId }
        }
    }

    fun confirmLoanFromCart(cartItem: CartItem) {
        registerLoan(cartItem.book.id, cartItem.loanDays)
    }
    
    fun confirmMultipleLoansFromCart(cartItems: List<CartItem>) {
        viewModelScope.launch {
            cartItems.forEach { cartItem ->
                registerLoan(cartItem.book.id, cartItem.loanDays)
                delay(100) // Pequeño delay entre préstamos para evitar problemas de concurrencia
            }
        }
    }

    fun registerLoan(bookId: Long, loanDays: Int) {
        viewModelScope.launch {
            // Validar usuario autenticado
            val userId = _user.value?.id
            val authError = validateUserAuthenticated(userId)
            if (authError != null) {
                _home.update { it.copy(errorMsg = authError) }
                return@launch
            }
            
            // Validar que userId no sea null
            val nonNullUserId: Long = userId ?: run {
                _home.update { it.copy(errorMsg = "Usuario no autenticado") }
                return@launch
            }
            
            // Validar días de préstamo
            val daysError = validateLoanDays(loanDays)
            if (daysError != null) {
                _home.update { it.copy(errorMsg = daysError) }
                return@launch
            }
            
            // Validar ID del libro
            val bookIdError = validateId(bookId)
            if (bookIdError != null) {
                _home.update { it.copy(errorMsg = bookIdError) }
                return@launch
            }
            
            val bookToLoan = bookRepository.getBookById(bookId)
            
            // Validar disponibilidad del libro
            val bookError = validateBookAvailable(bookToLoan)
            if (bookError != null) {
                _home.update { it.copy(errorMsg = bookError) }
                return@launch
            }
            
            // Validar que bookToLoan no sea null y asignarlo a variable no-null
            val book: BookEntity = bookToLoan ?: run {
                _home.update { it.copy(errorMsg = "Libro no encontrado") }
                return@launch
            }

            try {
                val loanDate = LocalDate.now()
                val dueDate = loanDate.plusDays(loanDays.toLong())
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                // Crear el préstamo con userId no-null y bookId del parámetro
                val newLoan = LoanEntity(
                    userId = nonNullUserId,
                    bookId = bookId,
                    loanDate = loanDate.format(formatter),
                    dueDate = dueDate.format(formatter),
                    returnDate = null,
                    status = "Active"
                )
                
                // PRIMERO: Crear préstamo en la base de datos del microservicio
                val loanResult = loanRepository.insert(newLoan)
                
                if (loanResult.isSuccess) {
                    // SOLO SI EL PRÉSTAMO SE CREÓ EXITOSAMENTE: Actualizar el libro usando la variable no-null 'book'
                    val updatedBook = book.copy(
                        status = "Loaned",
                        disponibles = (book.disponibles - 1).coerceAtLeast(0)
                    )
                    val bookUpdateResult = bookRepository.update(updatedBook)
                    
                    if (bookUpdateResult.isSuccess) {
                        // --- Actualización Crítica ---
                        loadActiveLoans() // Refresca la lista de préstamos
                        removeFromCart(bookId) // Quita el libro del carrito
                    } else {
                        _home.update { it.copy(errorMsg = "Error al actualizar libro: ${bookUpdateResult.exceptionOrNull()?.message}") }
                    }
                } else {
                    _home.update { it.copy(errorMsg = "Error al crear préstamo: ${loanResult.exceptionOrNull()?.message}") }
                }

            } catch (e: Exception) {
                _home.update { it.copy(errorMsg = "Error al registrar el préstamo: ${e.message}") }
            }
        }
    }

    fun updateLoanDays(bookId: Long, days: Int) {
        _cart.update { currentCart ->
            currentCart.map {
                if (it.book.id == bookId) {
                    it.copy(loanDays = days.coerceIn(1, 30))
                } else {
                    it
                }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _search.update { it.copy(query = newQuery) }
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(searchDebounceMillis)
            performSearchInternal(newQuery)
        }
    }

    fun performSearch() {
        performSearchInternal(_search.value.query)
    }

    private fun performSearchInternal(query: String) {
        viewModelScope.launch {
            _search.update { it.copy(isLoading = true, errorMsg = null, initialSearchPerformed = true) }
            try {
                val searchResults = bookRepository.searchBooks(query)
                _search.update { it.copy(isLoading = false, results = searchResults, errorMsg = null) }
            } catch (e: Exception) {
                _search.update { it.copy(isLoading = false, errorMsg = "Error al buscar: ${e.message}") }
            }
        }
    }

    fun clearSearchResults() {
        _search.update { SearchUiState() }
        searchDebounceJob?.cancel()
    }

    fun onLoginEmailChange(value: String) {
        val emailError = validateEmail(value)
        _login.update { it.copy(email = value, emailError = emailError) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        
        // Validaciones adicionales antes de enviar
        val emailError = validateEmail(s.email.trim())
        if (emailError != null) {
            _login.update { it.copy(emailError = emailError) }
            return
        }
        
        if (s.pass.isBlank()) {
            _login.update { it.copy(errorMsg = "La contraseña es obligatoria") }
            return
        }
        
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false, userRole = null) }
            val result = try {
                userRepository.login(s.email.trim(), s.pass)
            } catch (e: Exception) {
                _login.update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Error de login") }
                return@launch
            }

            if (result.isSuccess) {
                val userEmail = s.email.trim()
                userPreferencesRepository.saveUserEmail(userEmail)
                _login.update { it.copy(isSubmitting = false, success = true, userRole = result.getOrNull()) }
            } else {
                _login.update {
                    it.copy(isSubmitting = false, errorMsg = result.exceptionOrNull()?.message ?: "Credenciales inválidas")
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null, userRole = null) }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearAll()
            _login.update { LoginUiState() }
            _register.update { RegisterUiState() }
            clearSearchResults()
            _cart.value = emptyList()
            _activeLoans.value = emptyList()
        }
    }

    fun onRegisterProfileImageChange(uri: Uri?) {
        _register.update { it.copy(profileImageUri = uri?.toString()) }
    }

    fun onRegisterNameChange(value: String) {
        val error = validateNameLettersOnly(value)
        _register.update { it.copy(name = value, nameError = error) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        val error = validateEmail(value)
        _register.update { it.copy(email = value, emailError = error) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPhoneChange(value: String) {
        val error = validatePhoneDigitsOnly(value)
        _register.update { it.copy(phone = value, phoneError = error) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        val error = validateStrongPassword(value)
        val confirmError = validateConfirm(value, _register.value.confirm)
        _register.update { it.copy(pass = value, passError = error, confirmError = confirmError) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterConfirmChange(value: String) {
        val pass = _register.value.pass
        val error = validateConfirm(pass, value)
        _register.update { it.copy(confirm = value, confirmError = error) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val allFieldsValid = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        val noErrors = s.nameError == null && s.emailError == null && s.phoneError == null && s.passError == null && s.confirmError == null
        _register.update { it.copy(canSubmit = allFieldsValid && noErrors) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        
        // Validaciones finales antes de enviar
        val nameError = validateNameLettersOnly(s.name.trim())
        val emailError = validateEmail(s.email.trim())
        val phoneError = validatePhoneDigitsOnly(s.phone.trim())
        val passError = validateStrongPassword(s.pass)
        val confirmError = validateConfirm(s.pass, s.confirm)
        
        if (nameError != null || emailError != null || phoneError != null || passError != null || confirmError != null) {
            _register.update { 
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    passError = passError,
                    confirmError = confirmError
                ) 
            }
            return
        }
        
        // Validar imagen si está presente
        val imageError = s.profileImageUri?.let { validateBase64Image(it) }
        if (imageError != null) {
            _register.update { it.copy(errorMsg = imageError) }
            return
        }
        
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            val result = try {
                userRepository.register(s.name.trim(), s.email.trim(), s.phone.trim(), s.pass, s.profileImageUri)
            } catch (e: Exception) {
                _register.update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Error de registro") }
                return@launch
            }
            _register.update {
                if (result.isSuccess) it.copy(isSubmitting = false, success = true)
                else it.copy(isSubmitting = false, errorMsg = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    fun onUpdateUserProfileImageChange(uri: Uri?) {
        _updateUserState.update { it.copy(profileImageUri = uri?.toString()) }
    }

    fun onUpdateUserNameChange(name: String) {
        val error = validateNameLettersOnly(name)
        _updateUserState.update { it.copy(name = name) }
        // Nota: Podríamos agregar nameError al UpdateUserUiState si queremos mostrar errores en tiempo real
    }

    fun onUpdateUserEmailChange(email: String) {
        val error = validateEmail(email)
        _updateUserState.update { it.copy(email = email) }
        // Nota: Podríamos agregar emailError al UpdateUserUiState si queremos mostrar errores en tiempo real
    }

    fun onUpdateUserPhoneChange(phone: String) {
        val error = validatePhoneDigitsOnly(phone)
        _updateUserState.update { it.copy(phone = phone) }
        // Nota: Podríamos agregar phoneError al UpdateUserUiState si queremos mostrar errores en tiempo real
    }

    fun loadCurrentUserData() {
        _user.value?.let { user ->
            _updateUserState.update {
                it.copy(
                    userId = formatUserId(user.id, user.email),
                    name = user.name ?: "",
                    email = user.email ?: "",
                    phone = user.phone ?: "",
                    profileImageUri = user.profilePictureUri // Cargar la foto de perfil
                )
            }
        }
    }

    private fun formatUserId(id: Long, email: String): String {
        val lettersSource = kotlin.math.abs(email.hashCode().toLong()).toString(36)
        val letters = lettersSource.filter { it.isLetter() }.take(5).uppercase().padEnd(5, 'X')
        val lastDigit = id.toString().last()
        return "$letters$lastDigit"
    }

    fun updateUser() {
        viewModelScope.launch {
            _updateUserState.update { it.copy(isSubmitting = true) }
            try {
                val currentUser = _user.value
                val state = _updateUserState.value
                
                // Validar usuario autenticado
                val authError = validateUserAuthenticated(currentUser?.id)
                if (authError != null) {
                    _updateUserState.update { it.copy(isSubmitting = false, errorMsg = authError) }
                    return@launch
                }
                
                // Validaciones de campos
                val nameError = validateNameLettersOnly(state.name.trim())
                val emailError = validateEmail(state.email.trim())
                val phoneError = if (state.phone.isNotBlank()) validatePhoneDigitsOnly(state.phone.trim()) else null
                
                if (nameError != null || emailError != null || phoneError != null) {
                    _updateUserState.update { 
                        it.copy(
                            isSubmitting = false, 
                            errorMsg = nameError ?: emailError ?: phoneError ?: "Error de validación"
                        ) 
                    }
                    return@launch
                }
                
                // Verificar si la imagen cambió (nueva imagen seleccionada)
                val newImageUri = if (state.profileImageUri != currentUser?.profilePictureUri) {
                    state.profileImageUri // Nueva imagen
                } else {
                    null // No hay nueva imagen
                }
                
                // Validar imagen si está presente
                val imageError = newImageUri?.let { validateBase64Image(it) }
                if (imageError != null) {
                    _updateUserState.update { it.copy(isSubmitting = false, errorMsg = imageError) }
                    return@launch
                }
                
                val updatedUser = currentUser?.copy(
                    name = state.name.trim(),
                    email = state.email.trim(), 
                    phone = state.phone.trim(),
                    profilePictureUri = state.profileImageUri ?: currentUser.profilePictureUri
                )
                if (updatedUser != null) {
                    // Pasar la nueva imagen URI si hay una nueva
                    val result = userRepository.updateUser(updatedUser, newProfileImageUri = newImageUri)
                    
                    if (result.isSuccess) {
                        _user.value = updatedUser
                        _updateUserState.update { it.copy(isSubmitting = false, success = true) }
                    } else {
                        _updateUserState.update { 
                            it.copy(
                                isSubmitting = false, 
                                errorMsg = result.exceptionOrNull()?.message ?: "Error al actualizar usuario"
                            ) 
                        }
                    }
                } else {
                    _updateUserState.update { it.copy(isSubmitting = false, errorMsg = "Usuario no encontrado") }
                }
            } catch (e: Exception) {
                _updateUserState.update { it.copy(isSubmitting = false, errorMsg = e.message) }
            }
        }
    }

    fun clearUpdateUserState() {
        _updateUserState.update { it.copy(success = false, errorMsg = null) }
    }

    fun onVerificationCodeChange(code: String) {
        _updateUserState.update { it.copy(verificationCode = code, verificationError = null) }
    }

    fun initiateEmailUpdate() {
        _updateUserState.update { it.copy(showVerificationDialog = true, verificationCode = "", verificationError = null) }
    }

    fun confirmEmailUpdate() {
        viewModelScope.launch {
            _updateUserState.update { it.copy(isVerifying = true, verificationError = null) }
            delay(1000) 
            if (_updateUserState.value.verificationCode.isNotBlank()) {
                _updateUserState.update { it.copy(isVerifying = false, showVerificationDialog = false) }
                updateUser()
            } else {
                _updateUserState.update { it.copy(isVerifying = false, verificationError = "El código no puede estar vacío.") }
            }
        }
    }

    fun cancelEmailUpdate() {
        _updateUserState.update { it.copy(showVerificationDialog = false, verificationError = null, verificationCode = "") }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun markSplashShown() {
        viewModelScope.launch {
            userPreferencesRepository.setSplashShown()
        }
    }
}
