package com.empresa.libra_users.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.data.local.database.getInitialBooks
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.UserRepository
import com.empresa.libra_users.domain.validation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// --- Data classes para los estados de la UI ---
data class AdminDashboardUiState(val totalBooks: Int = 0, val totalUsers: Int = 0, val pendingLoans: Int = 0, val totalLoans: Int = 0, val isLoading: Boolean = true, val error: String? = null)
data class AdminBooksUiState(
    val books: List<BookEntity> = emptyList(),
    val filteredBooks: List<BookEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val soloDisponibles: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
data class AdminUsersUiState(val users: List<UserEntity> = emptyList(), val isLoading: Boolean = true, val error: String? = null)
data class LoanDetails(val loan: LoanEntity, val book: BookEntity?, val user: UserEntity?)
data class AdminLoansUiState(
    val loans: List<LoanDetails> = emptyList(),
    val filteredLoans: List<LoanDetails> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
data class BookLoanStats(val book: BookEntity, val loanCount: Int)
data class UserLoanStats(val user: UserEntity, val loanCount: Int)
data class LibraryStatus(val available: Int, val loaned: Int, val damaged: Int)
data class AdminReportsUiState(val topBooks: List<BookLoanStats> = emptyList(), val topUsers: List<UserLoanStats> = emptyList(), val libraryStatus: LibraryStatus = LibraryStatus(0, 0, 0), val isLoading: Boolean = true, val error: String? = null)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _dashboardUiState = MutableStateFlow(AdminDashboardUiState())
    val dashboardUiState: StateFlow<AdminDashboardUiState> = _dashboardUiState.asStateFlow()

    private val _booksUiState = MutableStateFlow(AdminBooksUiState())
    val booksUiState: StateFlow<AdminBooksUiState> = _booksUiState.asStateFlow()

    private val _usersUiState = MutableStateFlow(AdminUsersUiState())
    val usersUiState: StateFlow<AdminUsersUiState> = _usersUiState.asStateFlow()

    private val _loansUiState = MutableStateFlow(AdminLoansUiState())
    val loansUiState: StateFlow<AdminLoansUiState> = _loansUiState.asStateFlow()

    private val _reportsUiState = MutableStateFlow(AdminReportsUiState())
    val reportsUiState: StateFlow<AdminReportsUiState> = _reportsUiState.asStateFlow()

    init {
        loadDashboardTotals()
        loadBooks()
        loadUsers()
        loadLoanDetails()
        loadReports()
        // Precargar datos si no hay libros
        checkAndLoadInitialData()
    }

    /**
     * NOTA: Esta función es un respaldo, pero la carga principal de datos iniciales
     * se ejecuta en MainViewModel.init, que se crea al iniciar la app.
     * Esta función aquí solo se ejecuta si el admin entra antes de que MainViewModel
     * haya cargado los datos (caso muy raro).
     */
    private fun checkAndLoadInitialData() {
        viewModelScope.launch {
            try {
                val count = bookRepository.count()
                if (count == 0) {
                    // Cargar libros iniciales (respaldo - normalmente ya se cargaron en MainViewModel)
                    val initialBooks = getInitialBooks()
                    initialBooks.forEach { book ->
                        bookRepository.insert(book)
                    }
                }
            } catch (e: Exception) {
                // Error al cargar datos iniciales
            }
        }
    }

    fun loadDashboardTotals() {
        viewModelScope.launch {
            _dashboardUiState.update { it.copy(isLoading = true) }
            try {
                _dashboardUiState.update {
                    it.copy(
                        totalBooks = bookRepository.count(),
                        totalUsers = userRepository.countUsers(),
                        pendingLoans = loanRepository.countActiveLoans(),
                        totalLoans = loanRepository.countAllLoans(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _dashboardUiState.update { it.copy(isLoading = false, error = "Error al cargar totales: ${e.message}") }
            }
        }
    }

    fun loadBooks() {
        bookRepository.getAllBooks()
            .onStart { _booksUiState.update { it.copy(isLoading = true) } }
            .catch { e -> _booksUiState.update { it.copy(isLoading = false, error = e.message) } }
            .onEach { books ->
                _booksUiState.update { state ->
                    val filtered = applyBookFilters(books, state.searchQuery, state.selectedCategory, state.soloDisponibles)
                    state.copy(isLoading = false, books = books, filteredBooks = filtered)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun applyBookFilters(
        books: List<BookEntity>,
        query: String,
        category: String?,
        soloDisponibles: Boolean
    ): List<BookEntity> {
        return books.filter { book ->
            val matchesQuery = query.isBlank() || book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.isbn.contains(query, ignoreCase = true)
            val matchesCategory = category == null || book.categoria == category
            val matchesDisponibles = !soloDisponibles || book.disponibles > 0
            matchesQuery && matchesCategory && matchesDisponibles
        }
    }

    fun updateBookSearchQuery(query: String) {
        _booksUiState.update { state ->
            val filtered = applyBookFilters(state.books, query, state.selectedCategory, state.soloDisponibles)
            state.copy(searchQuery = query, filteredBooks = filtered)
        }
    }

    fun updateBookCategoryFilter(category: String?) {
        _booksUiState.update { state ->
            val filtered = applyBookFilters(state.books, state.searchQuery, category, state.soloDisponibles)
            state.copy(selectedCategory = category, filteredBooks = filtered)
        }
    }

    fun updateBookDisponiblesFilter(soloDisponibles: Boolean) {
        _booksUiState.update { state ->
            val filtered = applyBookFilters(state.books, state.searchQuery, state.selectedCategory, soloDisponibles)
            state.copy(soloDisponibles = soloDisponibles, filteredBooks = filtered)
        }
    }

    fun loadUsers() {
        userRepository.getUsers()
            .onStart { _usersUiState.update { it.copy(isLoading = true) } }
            .catch { e -> _usersUiState.update { it.copy(isLoading = false, error = e.message) } }
            .onEach { users -> _usersUiState.update { it.copy(isLoading = false, users = users) } }
            .launchIn(viewModelScope)
    }

    fun loadLoanDetails() {
        viewModelScope.launch {
            loanRepository.getAllLoansFlow()
                .onStart { _loansUiState.update { it.copy(isLoading = true) } }
                .catch { e -> _loansUiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { loans ->
                    // Actualizar estados de préstamos vencidos
                    updateOverdueLoans(loans)
                    val details = loans.map { loan ->
                        LoanDetails(
                            loan = loan,
                            book = bookRepository.getBookById(loan.bookId),
                            user = userRepository.getUserById(loan.userId)
                        )
                    }
                    val filtered = applyLoanFilters(details, _loansUiState.value)
                    _loansUiState.update { it.copy(isLoading = false, loans = details, filteredLoans = filtered) }
                }
        }
    }

    private fun updateOverdueLoans(loans: List<LoanEntity>) {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            loans.forEach { loan ->
                if (loan.status == "Active" && loan.dueDate < today) {
                    val updatedLoan = loan.copy(status = "Overdue")
                    loanRepository.update(updatedLoan)
                }
            }
        }
    }

    private fun applyLoanFilters(
        loans: List<LoanDetails>,
        state: AdminLoansUiState
    ): List<LoanDetails> {
        return loans.filter { detail ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    detail.book?.title?.contains(state.searchQuery, ignoreCase = true) == true ||
                    detail.user?.name?.contains(state.searchQuery, ignoreCase = true) == true
            val matchesStatus = state.selectedStatus == null || detail.loan.status == state.selectedStatus
            val matchesDateRange = when {
                state.fechaInicio == null && state.fechaFin == null -> true
                state.fechaInicio != null && state.fechaFin != null ->
                    detail.loan.loanDate >= state.fechaInicio && detail.loan.loanDate <= state.fechaFin
                state.fechaInicio != null -> detail.loan.loanDate >= state.fechaInicio
                state.fechaFin != null -> detail.loan.loanDate <= state.fechaFin
                else -> true
            }
            matchesQuery && matchesStatus && matchesDateRange
        }
    }

    fun updateLoanSearchQuery(query: String) {
        _loansUiState.update { state ->
            val filtered = applyLoanFilters(state.loans, state.copy(searchQuery = query))
            state.copy(searchQuery = query, filteredLoans = filtered)
        }
    }

    fun updateLoanStatusFilter(status: String?) {
        _loansUiState.update { state ->
            val filtered = applyLoanFilters(state.loans, state.copy(selectedStatus = status))
            state.copy(selectedStatus = status, filteredLoans = filtered)
        }
    }

    fun updateLoanDateRange(fechaInicio: String?, fechaFin: String?) {
        _loansUiState.update { state ->
            val filtered = applyLoanFilters(state.loans, state.copy(fechaInicio = fechaInicio, fechaFin = fechaFin))
            state.copy(fechaInicio = fechaInicio, fechaFin = fechaFin, filteredLoans = filtered)
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _reportsUiState.update { it.copy(isLoading = true) }
            try {
                val allLoans = loanRepository.getAllLoans()
                val allBooks = bookRepository.getAllBooks().first()
                val allUsers = userRepository.getUsers().first()

                val topBooks = allLoans.groupBy { it.bookId }
                    .mapNotNull { (bookId, loans) ->
                        allBooks.find { it.id == bookId }?.let { BookLoanStats(it, loans.size) }
                    }
                    .sortedByDescending { it.loanCount }
                    .take(5)
                val topUsers = allLoans.groupBy { it.userId }
                    .mapNotNull { (userId, loans) ->
                        allUsers.find { it.id == userId }?.let { UserLoanStats(it, loans.size) }
                    }
                    .sortedByDescending { it.loanCount }
                    .take(5)
                val libraryStatus = LibraryStatus(
                    available = allBooks.count { it.status == "Available" },
                    loaned = allBooks.count { it.status == "Loaned" },
                    damaged = allBooks.count { it.status == "Damaged" }
                )

                _reportsUiState.update {
                    it.copy(
                        isLoading = false,
                        topBooks = topBooks,
                        topUsers = topUsers,
                        libraryStatus = libraryStatus
                    )
                }
            } catch (e: Exception) {
                _reportsUiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun getLoansWithDetailsForUser(userId: Long): Flow<List<LoanDetails>> {
        return loanRepository.getLoansByUser(userId).map { loans ->
            loans.mapNotNull { loan ->
                bookRepository.getBookById(loan.bookId)?.let {
                    LoanDetails(loan = loan, book = it, user = null)
                }
            }
        }
    }

    fun updateUser(user: UserEntity) = viewModelScope.launch {
        userRepository.updateUser(user)
    }

    fun addBook(
        title: String,
        author: String,
        categoria: String,
        isbn: String,
        publisher: String,
        anio: Int,
        stock: Int,
        descripcion: String,
        coverUrl: String,
        categoryId: Int,
        homeSection: String
    ): Result<String> {
        // Validaciones
        val titleError = validateBookTitle(title)
        val authorError = validateBookAuthor(author)
        val categoriaError = validateBookCategory(categoria)
        val isbnError = validateISBN(isbn)
        val publisherError = validateBookPublisher(publisher)
        val anioError = validateBookYear(anio)
        val stockError = validateStock(stock)

        val errors = listOfNotNull(titleError, authorError, categoriaError, isbnError, publisherError, anioError, stockError)
        if (errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(errors.first()))
        }

        return try {
            viewModelScope.launch {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val publishDate = "${anio}-01-01"
                val newBook = BookEntity(
                    title = title,
                    author = author,
                    categoria = categoria,
                    isbn = isbn.replace("-", "").replace(" ", ""),
                    categoryId = categoryId.toLong(),
                    publisher = publisher,
                    publishDate = publishDate,
                    anio = anio,
                    status = if (stock > 0) "Available" else "Retired",
                    inventoryCode = "AUTO-${System.currentTimeMillis()}",
                    stock = stock,
                    disponibles = stock,
                    descripcion = descripcion,
                    coverUrl = coverUrl,
                    homeSection = homeSection
                )
                bookRepository.insert(newBook)
            }
            Result.success("Libro guardado correctamente.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBook(book: BookEntity): Result<String> {
        // Validaciones
        val titleError = validateBookTitle(book.title)
        val authorError = validateBookAuthor(book.author)
        val categoriaError = validateBookCategory(book.categoria)
        val isbnError = validateISBN(book.isbn)
        val publisherError = validateBookPublisher(book.publisher)
        val anioError = validateBookYear(book.anio)
        val stockError = validateStock(book.stock)

        // Validar stock contra préstamos activos
        val prestados = bookRepository.countActiveLoansForBook(book.id)
        val stockError2 = validateStockAgainstLoaned(book.stock, prestados)
        if (stockError2 != null) {
            return Result.failure(IllegalArgumentException(stockError2))
        }

        val errors = listOfNotNull(titleError, authorError, categoriaError, isbnError, publisherError, anioError, stockError)
        if (errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(errors.first()))
        }

        return try {
            // Asegurar que disponibles <= stock
            val updatedBook = book.copy(
                disponibles = book.disponibles.coerceAtMost(book.stock),
                status = when {
                    book.disponibles > 0 -> "Available"
                    book.stock == 0 -> "Retired"
                    else -> "Loaned"
                }
            )
            bookRepository.update(updatedBook)
            Result.success("Libro actualizado correctamente.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteBook(book: BookEntity) = viewModelScope.launch {
        bookRepository.delete(book)
    }

    suspend fun createLoan(userId: Long, bookId: Long, fechaPrestamo: String, fechaDevolucion: String): Result<String> {
        return try {
            // Validar fechas
            val dateError = validateLoanDates(fechaPrestamo, fechaDevolucion)
            if (dateError != null) {
                return Result.failure(IllegalArgumentException(dateError))
            }

            // Verificar si el usuario ya tiene un préstamo activo del mismo libro
            if (loanRepository.hasActiveLoan(userId, bookId)) {
                return Result.failure(IllegalArgumentException("El usuario ya tiene un préstamo activo de este libro."))
            }

            // Obtener el libro
            val book = bookRepository.getBookById(bookId)
            if (book == null) {
                return Result.failure(IllegalArgumentException("Libro no encontrado."))
            }

            // Verificar disponibilidad
            if (book.disponibles <= 0) {
                return Result.failure(IllegalArgumentException("No hay ejemplares disponibles de este libro."))
            }

            // Crear el préstamo
            val newLoan = LoanEntity(
                userId = userId,
                bookId = bookId,
                loanDate = fechaPrestamo,
                dueDate = fechaDevolucion,
                returnDate = null,
                status = "Active"
            )
            loanRepository.insert(newLoan)

            // Actualizar disponibles del libro
            val updatedBook = book.copy(disponibles = book.disponibles - 1)
            bookRepository.update(updatedBook)

            Result.success("Préstamo creado correctamente.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun markLoanAsReturned(loan: LoanEntity) {
        viewModelScope.launch {
            try {
                val updatedLoan = loan.copy(
                    status = "Returned",
                    returnDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
                loanRepository.update(updatedLoan)

                // Actualizar disponibles del libro
                val book = bookRepository.getBookById(loan.bookId)
                book?.let {
                    val updatedBook = it.copy(disponibles = it.disponibles + 1)
                    bookRepository.update(updatedBook)
                }
            } catch (e: Exception) {
                // Error al marcar como devuelto
            }
        }
    }

    suspend fun extendLoan(loan: LoanEntity, nuevaFechaDevolucion: String): Result<String> {
        return try {
            // Validar que la nueva fecha sea posterior a la fecha actual
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            if (nuevaFechaDevolucion <= today) {
                return Result.failure(IllegalArgumentException("La nueva fecha de devolución debe ser posterior a hoy."))
            }

            val updatedLoan = loan.copy(dueDate = nuevaFechaDevolucion)
            loanRepository.update(updatedLoan)
            Result.success("Plazo extendido correctamente.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelLoan(loan: LoanEntity): Result<String> {
        return try {
            if (loan.status != "Active") {
                return Result.failure(IllegalArgumentException("Solo se pueden cancelar préstamos activos."))
            }

            // Actualizar el préstamo
            val updatedLoan = loan.copy(status = "Cancelled")
            loanRepository.update(updatedLoan)

            // Actualizar disponibles del libro
            val book = bookRepository.getBookById(loan.bookId)
            book?.let {
                val updatedBook = it.copy(disponibles = it.disponibles + 1)
                bookRepository.update(updatedBook)
            }

            Result.success("Préstamo cancelado correctamente.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
