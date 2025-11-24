package com.empresa.libra_users.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Posiciones fijas para los elementos decorativos (coordenadas normalizadas 0.0 - 1.0)
private val bookPositions = listOf(
    Pair(0.1f, 0.15f),
    Pair(0.85f, 0.25f),
    Pair(0.15f, 0.75f),
    Pair(0.9f, 0.8f),
    Pair(0.05f, 0.5f),
    Pair(0.7f, 0.6f)
)

private val starPositions = listOf(
    Pair(0.2f, 0.3f),
    Pair(0.8f, 0.15f),
    Pair(0.3f, 0.85f),
    Pair(0.9f, 0.7f),
    Pair(0.1f, 0.65f),
    Pair(0.75f, 0.4f),
    Pair(0.4f, 0.2f),
    Pair(0.6f, 0.9f),
    Pair(0.25f, 0.5f),
    Pair(0.95f, 0.35f),
    Pair(0.15f, 0.9f),
    Pair(0.85f, 0.55f)
)

/**
 * Componente de fondo decorativo con iconos de libro y estrellas
 * Se aplica automáticamente a todas las pantallas a través del Theme
 */
@Composable
fun DecoratedBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (darkTheme) {
        Color(0xFF1E1E1E) // Fondo oscuro base más visible
    } else {
        Color(0xFFF5F5F5) // Fondo blanco suave/crema base más visible
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Fondo decorativo (encima del color base, debajo del contenido)
        DecorativePattern(
            modifier = Modifier.fillMaxSize(),
            darkTheme = darkTheme
        )
        
        // Contenido principal (encima del fondo decorativo)
        content()
    }
}

/**
 * Patrón decorativo con libros y estrellas dibujados con Canvas
 * Estilo sutil con contornos como en la imagen de referencia
 */
@Composable
private fun DecorativePattern(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false
) {
    // Colores sutiles para el fondo decorativo (como en la imagen)
    // Aumentando opacidad y visibilidad para que se vean claramente
    val bookColor = if (darkTheme) {
        Color(0xFFB0B0B0).copy(alpha = 0.35f) // Gris claro para modo oscuro - más visible
    } else {
        Color(0xFF6A5ACD).copy(alpha = 0.30f) // Lavanda/púrpura claro para modo claro - más visible
    }
    
    val starColor = if (darkTheme) {
        Color(0xFFC0C0C0).copy(alpha = 0.38f) // Más visible
    } else {
        Color(0xFF7B68EE).copy(alpha = 0.32f) // Lavanda un poco más intenso - más visible
    }
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Dibujar iconos de libro (solo contornos, estilo sutil)
        bookPositions.forEachIndexed { index, (xRatio, yRatio) ->
            val x = width * xRatio
            val y = height * yRatio
            val rotation = index * 30f // Rotación suave para cada libro
            val bookSize = if (index % 2 == 0) 40.dp.toPx() else 32.dp.toPx() // Libros más grandes
            drawBookOutline(
                center = Offset(x, y),
                size = bookSize,
                color = bookColor,
                rotation = rotation
            )
        }
        
        // Dibujar estrellas (solo contornos)
        starPositions.forEachIndexed { index, (xRatio, yRatio) ->
            val x = width * xRatio
            val y = height * yRatio
            val starSize = when {
                index % 4 == 0 -> 16.dp.toPx() // Estrellas más grandes
                index % 3 == 0 -> 12.dp.toPx()
                else -> 9.dp.toPx()
            }
            drawStarOutline(
                center = Offset(x, y),
                size = starSize,
                color = starColor,
                points = 5
            )
        }
    }
}

/**
 * Dibuja un icono de libro abierto (solo contorno) estilo sutil
 */
private fun DrawScope.drawBookOutline(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float = 0f
) {
    rotate(degrees = rotation, pivot = center) {
        val bookWidth = size * 0.8f
        val bookHeight = size * 0.6f
        val left = center.x - bookWidth / 2
        val top = center.y - bookHeight / 2
        val strokeWidth = 2.dp.toPx() // Línea un poco más gruesa para mejor visibilidad
        
        // Contorno del libro abierto (dos páginas)
        // Página izquierda
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(bookWidth / 2, bookHeight),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Página derecha
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x, top),
            size = Size(bookWidth / 2, bookHeight),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Línea central (lomo del libro)
        drawLine(
            color = color,
            start = Offset(center.x, top),
            end = Offset(center.x, top + bookHeight),
            strokeWidth = strokeWidth
        )
        
        // Líneas horizontales sutiles (simulando páginas)
        for (i in 1..2) {
            val y = top + (bookHeight / 3) * i
            // Línea en página izquierda
            drawLine(
                color = color.copy(alpha = color.alpha * 0.5f),
                start = Offset(left + 3.dp.toPx(), y),
                end = Offset(center.x - 3.dp.toPx(), y),
                strokeWidth = 0.8.dp.toPx()
            )
            // Línea en página derecha
            drawLine(
                color = color.copy(alpha = color.alpha * 0.5f),
                start = Offset(center.x + 3.dp.toPx(), y),
                end = Offset(left + bookWidth - 3.dp.toPx(), y),
                strokeWidth = 0.8.dp.toPx()
            )
        }
    }
}

/**
 * Dibuja una estrella de 5 puntas (solo contorno) estilo sutil
 */
private fun DrawScope.drawStarOutline(
    center: Offset,
    size: Float,
    color: Color,
    points: Int = 5
) {
    val outerRadius = size / 2
    val innerRadius = outerRadius * 0.4f
    val angleStep = 360f / (points * 2)
    val strokeWidth = 1.5.dp.toPx() // Línea un poco más gruesa para mejor visibilidad
    
    val path = Path().apply {
        for (i in 0 until points * 2) {
            val angleDeg = i * angleStep - 90f
            val angleRad = (angleDeg * PI / 180.0).toFloat()
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val x = center.x + radius * cos(angleRad)
            val y = center.y + radius * sin(angleRad)
            
            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }
    
    // Dibujar solo el contorno (stroke) en lugar de relleno
    drawPath(
        path = path,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )
}

