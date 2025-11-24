package com.empresa.libra_users.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity

@Composable
fun BookDetailsDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onAddToCart: (BookEntity) -> Unit,
    isDarkMode: Boolean = isSystemInDarkTheme() // Permite pasar el estado del modo oscuro
) {
    val dialogBackgroundColor = if (isDarkMode) {
        Color(0xFF1E1E1E) // Fondo oscuro sólido completamente opaco
    } else {
        Color(0xFFFFFFFF) // Fondo blanco sólido completamente opaco
    }
    
    val textColor = if (isDarkMode) {
        Color(0xFFFFFFFF) // Texto blanco en modo oscuro
    } else {
        Color(0xFF000000) // Texto negro en modo claro
    }
    
    val textColorVariant = if (isDarkMode) {
        Color(0xFFB0B0B0) // Texto gris claro en modo oscuro
    } else {
        Color(0xFF666666) // Texto gris oscuro en modo claro
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Fondo completamente opaco sin transparencia
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dialogBackgroundColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Contenido scrolleable
                val buttonHeight = 56.dp + 40.dp // Altura del botón + padding
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = buttonHeight) // Espacio para el botón fijo
                ) {
                // --- Portada del libro con overlay ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Overlay gradiente sólido para mejor legibilidad
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        dialogBackgroundColor.copy(alpha = 0.95f),
                                        dialogBackgroundColor
                                    ),
                                    startY = 180f
                                )
                            )
                    )
                    
                    // Botón de cerrar en la esquina superior derecha
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(
                                dialogBackgroundColor.copy(alpha = 0.95f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // --- Contenido principal ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // --- Título ---
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // --- Autor ---
                    Text(
                        text = "por ${book.author}",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColorVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // --- Calificación con estrellas ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DialogRatingBar(rating = 4.5)
                        Text(
                            text = "4.5",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "(125 reseñas)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColorVariant
                        )
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    // --- Badges de disponibilidad ---
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
                        
                        if (book.categoria.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = book.categoria,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
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
                            color = textColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) {
                                    Color(0xFF2C2C2C) // Card oscuro
                                } else {
                                    Color(0xFFF5F5F5) // Card claro
                                }
                            )
                        ) {
                            Text(
                                text = if (book.descripcion.isNotEmpty()) {
                                    book.descripcion
                                } else {
                                    "No hay descripción disponible para este libro."
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColorVariant,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Justify
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // --- Información adicional en card ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) {
                                Color(0xFF2C2C2C) // Card oscuro
                            } else {
                                Color(0xFFF5F5F5) // Card claro
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DialogInfoRow("Editorial", book.publisher, textColor, textColorVariant)
                            Divider(color = if (isDarkMode) Color(0xFF404040) else Color(0xFFE0E0E0))
                            DialogInfoRow("Año", "${book.anio}", textColor, textColorVariant)
                            Divider(color = if (isDarkMode) Color(0xFF404040) else Color(0xFFE0E0E0))
                            DialogInfoRow("ISBN", book.isbn, textColor, textColorVariant)
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
                }
            }
            
            // Botón fijo en la parte inferior
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = navigationBarsPadding.calculateBottomPadding()),
                color = dialogBackgroundColor,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 20.dp)
                ) {
                    Button(
                        onClick = { 
                            onAddToCart(book)
                            onDismiss()
                        },
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
                                "Agregar al carrito"
                            } else {
                                "No disponible"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogRatingBar(
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
                modifier = Modifier.size(20.dp)
            )
        }
        if (hasHalfStar) {
            Icon(
                imageVector = Icons.Filled.StarHalf,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(20.dp)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Filled.StarOutline,
                contentDescription = null,
                tint = starColor.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DialogInfoRow(
    label: String, 
    value: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textColorVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColorVariant,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}
