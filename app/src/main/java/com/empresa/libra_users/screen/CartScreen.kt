package com.empresa.libra_users.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.navigation.Routes
import com.empresa.libra_users.viewmodel.CartItem
import com.empresa.libra_users.viewmodel.MainViewModel
import java.text.NumberFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(vm: MainViewModel, navController: NavController) {
    val cartItems by vm.cart.collectAsStateWithLifecycle()
    val isDarkMode by vm.isDarkMode.collectAsStateWithLifecycle()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showMultipleLoansDialog by remember { mutableStateOf(false) }
    var selectedCartItemForPayment by remember { mutableStateOf<CartItem?>(null) }
    val isMultipleLoans = cartItems.size > 1
    val totalPrice = cartItems.sumOf { it.price }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCartCheckout,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tu carrito está vacío",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                if (isMultipleLoans) {
                    // Botón para confirmar múltiples préstamos
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = NumberFormat.getCurrencyInstance().format(totalPrice),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Button(
                                onClick = { showMultipleLoansDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Pagar ${cartItems.size} Préstamos Juntos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (isMultipleLoans) {
                        // Resumen cuando hay más de un préstamo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Resumen de Préstamos",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${cartItems.size} libros en tu carrito",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val minDays = cartItems.minOfOrNull { it.loanDays } ?: 0
                                val maxDays = cartItems.maxOfOrNull { it.loanDays } ?: 0
                                Text(
                                    text = "Rango de días: ${if (minDays == maxDays) "$minDays días" else "$minDays - $maxDays días"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cartItems, key = { it.book.id }) { cartItem ->
                            CartItemCard(
                                cartItem = cartItem,
                                isMultiple = isMultipleLoans,
                                isDarkMode = isDarkMode,
                                onRemove = { vm.removeFromCart(cartItem.book.id) },
                                onDaysChange = { days: Int -> vm.updateLoanDays(cartItem.book.id, days) },
                                onConfirmLoan = { 
                                    if (!isMultipleLoans) {
                                        selectedCartItemForPayment = cartItem
                                        showPaymentDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Diálogos
        if (showMultipleLoansDialog) {
            MultipleLoansConfirmationDialog(
                cartItems = cartItems,
                isDarkMode = isDarkMode,
                onDismiss = { showMultipleLoansDialog = false },
                onConfirm = { 
                    showMultipleLoansDialog = false
                    showPaymentDialog = true
                }
            )
        }
        
        if (showPaymentDialog) {
            PaymentDialog(
                cartItems = if (selectedCartItemForPayment != null) {
                    listOf(selectedCartItemForPayment!!)
                } else {
                    cartItems
                },
                isMultiple = isMultipleLoans,
                isDarkMode = isDarkMode,
                onDismiss = { 
                    showPaymentDialog = false
                    selectedCartItemForPayment = null
                },
                onPaymentSuccess = {
                    showPaymentDialog = false
                    if (isMultipleLoans) {
                        vm.confirmMultipleLoansFromCart(cartItems)
                    } else {
                        selectedCartItemForPayment?.let { vm.confirmLoanFromCart(it) }
                    }
                    selectedCartItemForPayment = null
                }
            )
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    isMultiple: Boolean,
    isDarkMode: Boolean,
    onRemove: () -> Unit,
    onDaysChange: (Int) -> Unit,
    onConfirmLoan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cartItem.book.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = cartItem.book.title,
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000)
                )
                Text(
                    text = cartItem.book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666)
                )
                Spacer(Modifier.height(12.dp))

                // --- Slider para los días de préstamo ---
                Text(
                    text = "Días de préstamo: ${cartItem.loanDays}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666)
                )
                Slider(
                    value = cartItem.loanDays.toFloat(),
                    onValueChange = { onDaysChange(it.toInt()) },
                    valueRange = 1f..30f,
                    steps = 28
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precio: ${NumberFormat.getCurrencyInstance().format(cartItem.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!isMultiple) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onConfirmLoan,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Confirmar Préstamo",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Quitar del carrito",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MultipleLoansConfirmationDialog(
    cartItems: List<CartItem>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dialogBackgroundColor = if (isDarkMode) {
        Color(0xFF1E1E1E)
    } else {
        Color(0xFFFFFFFF)
    }
    
    val textColor = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textColorVariant = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666)
    val cardColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    
    val totalPrice = cartItems.sumOf { it.price }
    val minDays = cartItems.minOfOrNull { it.loanDays } ?: 0
    val maxDays = cartItems.maxOfOrNull { it.loanDays } ?: 0
    val daysRange = if (minDays == maxDays) "$minDays días" else "$minDays - $maxDays días"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackgroundColor,
        title = {
            Text(
                text = "Confirmar Préstamos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cantidad:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColorVariant
                            )
                            Text(
                                text = "${cartItems.size} libros",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rango de días:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColorVariant
                            )
                            Text(
                                text = daysRange,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }
                        
                        Divider(color = if (isDarkMode) Color(0xFF404040) else Color(0xFFE0E0E0))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance().format(totalPrice),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Continuar",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
