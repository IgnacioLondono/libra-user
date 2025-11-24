package com.empresa.libra_users.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// LocalComposition para el estado del tema oscuro que se puede acceder desde cualquier pantalla
val LocalDarkTheme = compositionLocalOf<Boolean> { false }

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.Transparent, // Fondo transparente para que se vea el decorativo
    surface = Color.Transparent // Superficies transparentes
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.Transparent, // Fondo transparente para que se vea el decorativo
    surface = Color.Transparent // Superficies transparentes
)

@Composable
fun LibrausersTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val dynamicScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            // Sobrescribir background y surface para que sean transparentes y se vea el fondo decorativo
            dynamicScheme.copy(
                background = Color.Transparent,
                surface = Color.Transparent,
                surfaceVariant = if (darkTheme) dynamicScheme.surfaceVariant.copy(alpha = 0.8f) else dynamicScheme.surfaceVariant.copy(alpha = 0.9f)
            )
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        // Proporcionar el estado del tema oscuro a todas las pantallas
        CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
            // Aplicar fondo decorativo a toda la aplicación
            DecoratedBackground(darkTheme = darkTheme) {
                content()
            }
        }
    }
}
