package com.empresa.libra_users.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
fun SplashScreen(
    onFinish: () -> Unit,
    durationMillis: Long = 1800L
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = rememberInfiniteTransition(label = "alpha")
    val scaleAnim = rememberInfiniteTransition(label = "scale")
    
    // Animación de fade-in y scale-in
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "alpha_animation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "scale_animation"
    )
    
    // Efecto de glow suave (pulso sutil)
    val glowAlpha by alphaAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
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
        // Efecto de glow de fondo
        Box(
            modifier = Modifier
                .size(300.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6650a4).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Logo con animaciones
        Image(
            painter = painterResource(id = R.drawable.library_logo),
            contentDescription = "Logo Libra",
            modifier = Modifier
                .size(200.dp)
                .alpha(alpha)
                .scale(scale),
            contentScale = ContentScale.Fit
        )
    }
}

