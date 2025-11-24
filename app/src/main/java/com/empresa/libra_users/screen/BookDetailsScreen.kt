package com.empresa.libra_users.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    vm: MainViewModel,
    bookId: Long,
    onBack: () -> Unit
) {
    val bookState = remember { mutableStateOf<BookEntity?>(null) }
    val user by vm.user.collectAsStateWithLifecycle()

    // Estados para los diálogos
    val showLoanDialog = remember { mutableStateOf(false) }
    val showPaymentDialog = remember { mutableStateOf(false) }

    // Estados para los datos del préstamo y pago
    val loanDays = remember { mutableStateOf(1) }
    val cardNumber = remember { mutableStateOf("") }
    val expiryDate = remember { mutableStateOf("") }
    val cvv = remember { mutableStateOf("") }

    LaunchedEffect(bookId) {
        bookState.value = vm.getBookById(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bookState.value?.title ?: "Detalles del Libro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        val book = bookState.value
        if (book != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Portada del libro con diseño mejorado ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = book.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay gradiente para mejor legibilidad del texto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.background
                                    ),
                                    startY = 200f
                                )
                            )
                    )
                }
                
                // --- Información principal del libro ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp)
                ) {
                    // Título
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Autor
                    Text(
                        text = "por ${book.author}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Calificación con estrellas y número
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RatingBar(
                            rating = 4.5,
                            modifier = Modifier
                        )
                        Text(
                            text = "4.5",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "(125 reseñas)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    // Badge de disponibilidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (book.status == "Available") 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (book.status == "Available") "✓ Disponible" else "✗ No disponible",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (book.status == "Available") 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        
                        if (book.disponibles > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${book.disponibles} ejemplares",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // --- Información del libro en cards ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BookInfoRow("Categoría", book.categoria.ifEmpty { "Sin categoría" })
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            BookInfoRow("Editorial", book.publisher)
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            BookInfoRow("Año de publicación", "${book.anio}")
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            BookInfoRow("ISBN", book.isbn)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // --- Descripción del libro ---
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (book.descripcion.isNotEmpty()) {
                                    book.descripcion
                                } else {
                                    "No hay descripción disponible para este libro."
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Justify
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // --- Botón de préstamo mejorado ---
                    Button(
                        onClick = { showLoanDialog.value = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = book.status == "Available" && book.disponibles > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (book.status == "Available" && book.disponibles > 0) {
                                "Solicitar Préstamo"
                            } else {
                                "No disponible"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // --- Diálogos ---
        if (showLoanDialog.value) {
            LoanDaysDialog(
                loanDays = loanDays.value,
                onLoanDaysChange = { days -> loanDays.value = if (days in 1..30) days else loanDays.value },
                onConfirm = {
                    showLoanDialog.value = false
                    showPaymentDialog.value = true // Abrir diálogo de pago
                },
                onDismiss = { showLoanDialog.value = false }
            )
        }

        if (showPaymentDialog.value) {
            PaymentDialog(
                cardNumber = cardNumber.value, onCardNumberChange = { num -> cardNumber.value = num },
                expiryDate = expiryDate.value, onExpiryDateChange = { date -> expiryDate.value = date },
                cvv = cvv.value, onCvvChange = { c -> cvv.value = c },
                onConfirm = {
                    // Llama a la función registerLoan con los parámetros correctos
                    vm.registerLoan(bookId = bookId, loanDays = loanDays.value)
                    showPaymentDialog.value = false
                },
                onDismiss = { showPaymentDialog.value = false }
            )
        }
    }
}

@Composable
private fun RatingBar(
    rating: Double,
    starCount: Int = 5,
    starColor: Color = Color(0xFFFFC107),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fullStars = rating.toInt()
        val hasHalfStar = (rating - fullStars) >= 0.5
        val emptyStars = starCount - fullStars - if (hasHalfStar) 1 else 0
        
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(24.dp)
            )
        }
        if (hasHalfStar) {
            Icon(
                imageVector = Icons.Filled.StarHalf,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(24.dp)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Filled.StarOutline,
                contentDescription = null,
                tint = starColor.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BookInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

@Composable
private fun LoanDaysDialog(
    loanDays: Int, onLoanDaysChange: (Int) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Préstamo de Libro") },
        text = {
            Column {
                Text("Selecciona el número de días para el préstamo (máx. 30 días):")
                Slider(value = loanDays.toFloat(), onValueChange = { onLoanDaysChange(it.toInt()) }, valueRange = 1f..30f, steps = 28)
                Text(text = "Días: $loanDays", modifier = Modifier.align(Alignment.End))
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Siguiente") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PaymentDialog(
    cardNumber: String, onCardNumberChange: (String) -> Unit,
    expiryDate: String, onExpiryDateChange: (String) -> Unit,
    cvv: String, onCvvChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Realizar Pago") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Introduce los datos de tu tarjeta de débito para procesar el préstamo.")
                OutlinedTextField(value = cardNumber, onValueChange = onCardNumberChange, label = { Text("Número de Tarjeta") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = expiryDate, onValueChange = onExpiryDateChange, label = { Text("Fecha de Vencimiento (MM/AA)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = cvv, onValueChange = onCvvChange, label = { Text("CVV") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Pagar y Confirmar Préstamo") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
