package com.empresa.libra_users.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullRefreshIndicator
import androidx.compose.material3.pulltorefresh.pullRefresh
import androidx.compose.material3.pulltorefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onLogout: () -> Unit // Manteniéndolo por si se usa en el drawer
) {
    val homeState by vm.home.collectAsStateWithLifecycle()
    val user by vm.user.collectAsStateWithLifecycle()
    val allBooks = homeState.categorizedBooks.values.flatten().distinctBy { it.id }
    val purpleColor = Color(0xFF6650a4) // Color morado principal

    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    
    // Snackbar para mostrar mensaje de bienvenida
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var hasShownWelcome by remember { mutableStateOf(false) }
    
    // Pull to refresh
    val isRefreshing = homeState.isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { vm.refreshHome() }
    )
    
    // Cargar libros al iniciar la pantalla
    LaunchedEffect(Unit) {
        vm.loadCategorizedBooks()
    }
    
    // Mostrar mensaje de bienvenida solo una vez cuando el usuario se carga
    LaunchedEffect(user?.id) {
        if (user != null && !hasShownWelcome) {
            val userName = user?.name?.takeIf { it.isNotBlank() } ?: "Usuario"
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "¡Bienvenido, $userName!",
                    duration = SnackbarDuration.Short
                )
            }
            hasShownWelcome = true
        }
    }

    val isDarkMode by vm.isDarkMode.collectAsStateWithLifecycle()
    
    // -- Lógica para mostrar el diálogo de detalles --
    selectedBook?.let { book ->
        BookDetailsDialog(
            book = book,
            onDismiss = { selectedBook = null },
            onAddToCart = { bookEntity ->
                vm.addToCart(bookEntity)
            },
            isDarkMode = isDarkMode
        )
    }

    // Si no hay libros con homeSection configurado, mostrar algunos libros aleatorios
    val freeBooks = if (allBooks.any { it.homeSection == "Free" }) {
        allBooks.filter { it.homeSection == "Free" }
    } else {
        allBooks.shuffled().take(5) // Mostrar 5 libros aleatorios si no hay "Free"
    }
    
    val trendingBooks = if (allBooks.any { it.homeSection == "Trending" }) {
        allBooks.filter { it.homeSection == "Trending" }
    } else {
        allBooks.shuffled().take(6) // Mostrar 6 libros aleatorios si no hay "Trending"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.padding(paddingValues)
                ) {
                    item { FreeBooksSection(books = freeBooks, onBookClick = { book -> selectedBook = book }, purpleColor = purpleColor) }
                    item { TrendingBooksSection(books = trendingBooks, onBookClick = { book -> selectedBook = book }, purpleColor = purpleColor) }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun FreeBooksSection(books: List<BookEntity>, onBookClick: (BookEntity) -> Unit, purpleColor: Color) {
    Column {
        Text(
            text = "Nuestra selección de libros destacables por su popularidad",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = purpleColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books) { book ->
                FreeBookCarouselItem(book = book, onBookClick = { onBookClick(book) })
            }
        }
    }
}

@Composable
private fun FreeBookCarouselItem(book: BookEntity, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier.width(280.dp).clickable { onBookClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(book.coverUrl).crossfade(true).build(),
            contentDescription = book.title,
            modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun TrendingBooksSection(books: List<BookEntity>, onBookClick: (BookEntity) -> Unit, purpleColor: Color) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "Novedades y títulos de tendencia",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = purpleColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "Novedades y títulos de actualidad",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            modifier = Modifier.height(420.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books) { book ->
                TrendingBookGridItem(book = book, onBookClick = { onBookClick(book) }, purpleColor = purpleColor)
            }
        }
    }
}
