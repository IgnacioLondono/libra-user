package com.empresa.libra_users.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullRefreshIndicator
import androidx.compose.material3.pulltorefresh.pullRefresh
import androidx.compose.material3.pulltorefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.text.input.ImeAction // Ya no se necesita
// import androidx.compose.ui.text.input.KeyboardActions // Ya no se necesita
// import androidx.compose.ui.text.input.KeyboardOptions // Ya no se necesita
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    vm: MainViewModel,
    // onBookClick: (Long) -> Unit
) {
    val state by vm.search.collectAsStateWithLifecycle()
    
    // Pull to refresh
    val isRefreshing = state.isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { vm.refreshSearch() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Campo de Búsqueda
            SearchBar(
                query = state.query,
                onQueryChange = vm::onSearchQueryChange,
                onSearch = vm::performSearch,
                modifier = Modifier.padding(16.dp)
            )

            // Indicador de Carga
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Lista de Resultados
            SearchResultList(
                results = state.results,
                initialSearchPerformed = state.initialSearchPerformed,
                errorMsg = state.errorMsg
            )
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Buscar por título o autor...") },
        singleLine = true,
        leadingIcon = {
            IconButton(onClick = onSearch) { // La búsqueda se activa aquí
                Icon(Icons.Filled.Search, contentDescription = "Buscar")
            }
        },
        // --- SE ELIMINARON LAS OPCIONES DEL TECLADO ---
        // keyboardOptions y keyboardActions han sido removidas.
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun SearchResultList(
    results: List<BookEntity>,
    initialSearchPerformed: Boolean,
    errorMsg: String?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        errorMsg?.let {
            item { Text(it, color = MaterialTheme.colorScheme.error) }
        }

        if (initialSearchPerformed && results.isEmpty() && errorMsg == null) {
            item {
                Text(
                    "No se encontraron resultados para la búsqueda.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(results) { book ->
            BookResultItem(book = book, onClick = { /* onBookClick(book.id) */ })
            Divider()
        }
    }

    if (!initialSearchPerformed) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Escribe un término y presiona buscar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BookResultItem(book: BookEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // SIMULACIÓN DE PORTADA
        Surface(
            modifier = Modifier.size(60.dp, 90.dp),
            color = Color.LightGray
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Portada", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(book.title, style = MaterialTheme.typography.titleMedium)
            Text("Autor: ${book.author}", style = MaterialTheme.typography.bodySmall)
            Text("Categoría ID: ${book.categoryId}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
