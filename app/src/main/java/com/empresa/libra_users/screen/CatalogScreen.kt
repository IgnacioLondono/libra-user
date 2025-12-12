package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(vm: MainViewModel) {
    val homeState by vm.home.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val purpleColor = Color(0xFF6650a4)
    
    // Cargar libros al iniciar la pantalla
    LaunchedEffect(Unit) {
        vm.loadCategorizedBooks()
    }

    // Obtener todas las categorías únicas de los libros (usando categoria del BookEntity)
    val allBooks = homeState.categorizedBooks.values.flatten().distinctBy { it.id }
    val uniqueCategories = remember(allBooks) {
        allBooks.mapNotNull { it.categoria }.distinct().sorted()
    }
    val categories = listOf("Todos") + uniqueCategories

    val booksForCategory = if (selectedCategory == "Todos") {
        allBooks
    } else {
        allBooks.filter { it.categoria == selectedCategory }
    }

    val filteredBooks = if (searchQuery.isBlank()) {
        booksForCategory
    } else {
        booksForCategory.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true)
        }
    }

    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    val isDarkMode by vm.isDarkMode.collectAsStateWithLifecycle()

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar en el catálogo...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )

                FilterChipRow(categories, selectedCategory) { category ->
                    selectedCategory = category
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredBooks, key = { it.id }) { book ->
                        TrendingBookGridItem(
                            book = book,
                            onBookClick = { selectedBook = book },
                            purpleColor = purpleColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipRow(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}
