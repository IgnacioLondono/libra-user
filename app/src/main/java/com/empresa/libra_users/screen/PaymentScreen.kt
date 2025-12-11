package com.empresa.libra_users.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.empresa.libra_users.viewmodel.CartItem
import java.text.NumberFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PaymentMethod {
    DEBIT, CREDIT
}

@Composable
fun PaymentDialog(
    cartItems: List<CartItem>,
    isMultiple: Boolean = false,
    isDarkMode: Boolean = false,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var showAddCard by remember { mutableStateOf(false) }
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Estados de validación
    var cardNumberError by remember { mutableStateOf<String?>(null) }
    var cardHolderError by remember { mutableStateOf<String?>(null) }
    var expiryDateError by remember { mutableStateOf<String?>(null) }
    var cvvError by remember { mutableStateOf<String?>(null) }
    
    // Funciones de validación
    fun validateCardNumber(value: String): String? {
        val digitsOnly = value.replace(" ", "")
        return when {
            digitsOnly.isEmpty() -> "El número de tarjeta es obligatorio"
            digitsOnly.length < 16 -> "El número de tarjeta debe tener 16 dígitos"
            digitsOnly.length > 16 -> "El número de tarjeta no puede tener más de 16 dígitos"
            !digitsOnly.all { it.isDigit() } -> "Solo se permiten números"
            else -> null
        }
    }
    
    fun validateCardHolder(value: String): String? {
        return when {
            value.isBlank() -> "El titular de la tarjeta es obligatorio"
            value.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            value.length > 50 -> "El nombre no puede exceder 50 caracteres"
            !value.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> "Solo se permiten letras y espacios"
            else -> null
        }
    }
    
    fun validateExpiryDate(value: String): String? {
        return when {
            value.isBlank() -> "La fecha de vencimiento es obligatoria"
            !value.matches(Regex("^\\d{2}/\\d{2}$")) -> "El formato debe ser MM/AA (ej: 12/25)"
            else -> {
                val parts = value.split("/")
                val month = parts[0].toIntOrNull()
                val year = parts[1].toIntOrNull()
                when {
                    month == null || year == null -> "La fecha no es válida"
                    month < 1 || month > 12 -> "El mes debe estar entre 01 y 12"
                    else -> null
                }
            }
        }
    }
    
    fun validateCvv(value: String): String? {
        return when {
            value.isBlank() -> "El CVV es obligatorio"
            value.length < 3 -> "El CVV debe tener 3 dígitos"
            value.length > 3 -> "El CVV no puede tener más de 3 dígitos"
            !value.all { it.isDigit() } -> "Solo se permiten números"
            else -> null
        }
    }
    
    // Validar todos los campos
    fun isFormValid(): Boolean {
        return validateCardNumber(cardNumber) == null &&
                validateCardHolder(cardHolder) == null &&
                validateExpiryDate(expiryDate) == null &&
                validateCvv(cvv) == null &&
                selectedPaymentMethod != null
    }
    
    val dialogBackgroundColor = if (isDarkMode) {
        Color(0xFF1E1E1E)
    } else {
        Color(0xFFFFFFFF)
    }
    
    val textColor = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textColorVariant = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666)
    val cardColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    
    val totalPrice = cartItems.sumOf { it.price }
    val totalDays = if (isMultiple) {
        val minDays = cartItems.minOfOrNull { it.loanDays } ?: 0
        val maxDays = cartItems.maxOfOrNull { it.loanDays } ?: 0
        if (minDays == maxDays) "$minDays días" else "$minDays - $maxDays días"
    } else {
        "${cartItems.firstOrNull()?.loanDays ?: 0} días"
    }
    
    Dialog(
        onDismissRequest = { if (!isProcessing && !paymentSuccess) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dialogBackgroundColor)
        ) {
            if (paymentSuccess) {
                // Pantalla de éxito
                PaymentSuccessScreen(
                    isDarkMode = isDarkMode,
                    onDismiss = {
                        paymentSuccess = false
                        onPaymentSuccess()
                    }
                )
            } else if (isProcessing) {
                // Pantalla de procesamiento
                PaymentProcessingScreen(
                    isDarkMode = isDarkMode
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = textColor
                            )
                        }
                        Text(
                            text = "Proceso de Pago",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(Modifier.width(48.dp)) // Balance para el botón de cerrar
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Resumen del pedido
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
                            Text(
                                text = "Resumen del Pedido",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isMultiple) "Cantidad de préstamos:" else "Préstamo:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColorVariant
                                )
                                Text(
                                    text = if (isMultiple) "${cartItems.size} libros" else "1 libro",
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
                                    text = "Duración del préstamo:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColorVariant
                                )
                                Text(
                                    text = totalDays,
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
                                    text = "Total a pagar:",
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
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Selección de método de pago
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tarjeta de Débito
                        Box(modifier = Modifier.weight(1f)) {
                            PaymentMethodCard(
                                title = "Débito",
                                icon = Icons.Default.CreditCard,
                                isSelected = selectedPaymentMethod == PaymentMethod.DEBIT,
                                isDarkMode = isDarkMode,
                                onClick = { selectedPaymentMethod = PaymentMethod.DEBIT }
                            )
                        }
                        
                        // Tarjeta de Crédito
                        Box(modifier = Modifier.weight(1f)) {
                            PaymentMethodCard(
                                title = "Crédito",
                                icon = Icons.Default.CreditCard,
                                isSelected = selectedPaymentMethod == PaymentMethod.CREDIT,
                                isDarkMode = isDarkMode,
                                onClick = { selectedPaymentMethod = PaymentMethod.CREDIT }
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Formulario de tarjeta
                    if (selectedPaymentMethod != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = if (showAddCard) "Agregar Nueva Tarjeta" else "Datos de la Tarjeta",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { newValue ->
                                        // Remover espacios existentes para procesar solo dígitos
                                        val digitsOnly = newValue.filter { char -> char.isDigit() }
                                        
                                        // Limitar a 16 dígitos
                                        val limitedDigits = if (digitsOnly.length > 16) {
                                            digitsOnly.take(16)
                                        } else {
                                            digitsOnly
                                        }
                                        
                                        // Formatear con espacios cada 4 dígitos
                                        cardNumber = limitedDigits.chunked(4).joinToString(" ")
                                        cardNumberError = validateCardNumber(cardNumber)
                                    },
                                    label = { Text("Número de Tarjeta *", color = textColorVariant) },
                                    placeholder = { Text("1234 5678 9012 3456", color = textColorVariant.copy(alpha = 0.5f)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { focusState ->
                                            if (!focusState.isFocused) {
                                                cardNumberError = validateCardNumber(cardNumber)
                                            }
                                        },
                                    isError = cardNumberError != null,
                                    supportingText = {
                                        if (cardNumberError != null) {
                                            Text(
                                                text = cardNumberError!!,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedLabelColor = textColorVariant,
                                        unfocusedLabelColor = textColorVariant,
                                        errorLabelColor = MaterialTheme.colorScheme.error,
                                        errorSupportingTextColor = MaterialTheme.colorScheme.error
                                    )
                                )
                                
                                OutlinedTextField(
                                    value = cardHolder,
                                    onValueChange = { newValue ->
                                        cardHolder = newValue
                                        cardHolderError = validateCardHolder(cardHolder)
                                    },
                                    label = { Text("Titular de la Tarjeta *", color = textColorVariant) },
                                    placeholder = { Text("NOMBRE APELLIDO", color = textColorVariant.copy(alpha = 0.5f)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { focusState ->
                                            if (!focusState.isFocused) {
                                                cardHolderError = validateCardHolder(cardHolder)
                                            }
                                        },
                                    isError = cardHolderError != null,
                                    supportingText = {
                                        if (cardHolderError != null) {
                                            Text(
                                                text = cardHolderError!!,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedLabelColor = textColorVariant,
                                        unfocusedLabelColor = textColorVariant,
                                        errorLabelColor = MaterialTheme.colorScheme.error,
                                        errorSupportingTextColor = MaterialTheme.colorScheme.error
                                    )
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = expiryDate,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 5) {
                                                val filtered = newValue.filter { char -> char.isDigit() || char == '/' }
                                                if (filtered.length == 2 && !filtered.contains('/')) {
                                                    expiryDate = "$filtered/"
                                                } else {
                                                    expiryDate = filtered
                                                }
                                                expiryDateError = validateExpiryDate(expiryDate)
                                            }
                                        },
                                        label = { Text("MM/AA *", color = textColorVariant) },
                                        placeholder = { Text("12/25", color = textColorVariant.copy(alpha = 0.5f)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged { focusState ->
                                                if (!focusState.isFocused) {
                                                    expiryDateError = validateExpiryDate(expiryDate)
                                                }
                                            },
                                        isError = expiryDateError != null,
                                        supportingText = {
                                            if (expiryDateError != null) {
                                                Text(
                                                    text = expiryDateError!!,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textColor,
                                            unfocusedTextColor = textColor,
                                            focusedLabelColor = textColorVariant,
                                            unfocusedLabelColor = textColorVariant,
                                            errorLabelColor = MaterialTheme.colorScheme.error,
                                            errorSupportingTextColor = MaterialTheme.colorScheme.error
                                        )
                                    )
                                    
                                    OutlinedTextField(
                                        value = cvv,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 3) {
                                                cvv = newValue.filter { char -> char.isDigit() }
                                                cvvError = validateCvv(cvv)
                                            }
                                        },
                                        label = { Text("Código de Seguridad *", color = textColorVariant) },
                                        placeholder = { Text("123", color = textColorVariant.copy(alpha = 0.5f)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged { focusState ->
                                                if (!focusState.isFocused) {
                                                    cvvError = validateCvv(cvv)
                                                }
                                            },
                                        isError = cvvError != null,
                                        supportingText = {
                                            if (cvvError != null) {
                                                Text(
                                                    text = cvvError!!,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textColor,
                                            unfocusedTextColor = textColor,
                                            focusedLabelColor = textColorVariant,
                                            unfocusedLabelColor = textColorVariant,
                                            errorLabelColor = MaterialTheme.colorScheme.error,
                                            errorSupportingTextColor = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Botón de confirmar pago
                    Button(
                        onClick = {
                            // Validar todos los campos antes de procesar
                            cardNumberError = validateCardNumber(cardNumber)
                            cardHolderError = validateCardHolder(cardHolder)
                            expiryDateError = validateExpiryDate(expiryDate)
                            cvvError = validateCvv(cvv)
                            
                            if (isFormValid()) {
                                isProcessing = true
                                // Simular procesamiento de pago
                                scope.launch {
                                    delay(2000) // Simulación de 2 segundos
                                    isProcessing = false
                                    paymentSuccess = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isFormValid(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Confirmar Pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val cardColor = if (isSelected) {
        if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFE3F2FD)
    } else {
        if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDarkMode) Color(0xFF404040) else Color(0xFFE0E0E0)
    }
    
    val textColor = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(2.dp, borderColor)
            } else null
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else textColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
        }
    }

@Composable
private fun PaymentProcessingScreen(
    isDarkMode: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "payment_processing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val dialogBackgroundColor = if (isDarkMode) {
        Color(0xFF1E1E1E)
    } else {
        Color(0xFFFFFFFF)
    }
    
    val textColor = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dialogBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .rotate(rotation),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            
            Text(
                text = "Procesando pago...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "Por favor, espera un momento",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun PaymentSuccessScreen(
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val dialogBackgroundColor = if (isDarkMode) {
        Color(0xFF1E1E1E)
    } else {
        Color(0xFFFFFFFF)
    }
    
    val textColor = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dialogBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Éxito",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(80.dp)
            )
            
            Text(
                text = "¡Pago Exitoso!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Tu préstamo ha sido confirmado exitosamente",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

