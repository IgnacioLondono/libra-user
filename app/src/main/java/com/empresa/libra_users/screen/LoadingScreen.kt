package com.empresa.libra_users.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.empresa.libra_users.R
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onFinish: () -> Unit,
    durationMillis: Long = 1000L
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Animación de pulso (scale)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Animación de brillo (alpha)
    val brightness by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brightness"
    )
    
    LaunchedEffect(Unit) {
        delay(durationMillis)
        onFinish()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000), // Negro
                        Color(0xFF1a0033), // Púrpura oscuro
                        Color(0xFF000000)  // Negro
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.library_logo),
            contentDescription = "Cargando",
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .alpha(brightness),
            contentScale = ContentScale.Fit
        )
    }
}

