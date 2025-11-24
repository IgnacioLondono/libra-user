package com.empresa.libra_users.ui.theme.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.empresa.libra_users.navigation.Routes

// Data class para representar cada opción del drawer
data class DrawerItem(
    val route: String, // Añadimos la ruta para saber cuál está seleccionada
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// Componente AppDrawer para usar en ModalNavigationDrawer
@Composable
fun AppDrawer(
    currentRoute: String?,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
    ) {
        // Primero, los items de navegación
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = item.route == currentRoute,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        // Separador
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Fila para el interruptor de modo oscuro
        Row(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Modo Oscuro", modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggleDarkMode() }
            )
        }
    }
}

// Helper para construir la lista para usuarios SIN sesión
@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
): List<DrawerItem> = listOf(
    DrawerItem(Routes.HOME, "Home", Icons.Filled.Home, onHome),
    DrawerItem(Routes.LOGIN, "Login", Icons.Filled.AccountCircle, onLogin),
    DrawerItem(Routes.REGISTER, "Registro", Icons.Filled.Person, onRegister)
)

@Composable
fun authenticatedDrawerItems(
    onHome: () -> Unit,
    onLogout: () -> Unit,
    // Añadimos callbacks para las nuevas opciones
    onCatalog: () -> Unit,
    onNews: () -> Unit,
    onAccountSettings: () -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            route = Routes.HOME,
            label = "Novedades",
            icon = Icons.Filled.NewReleases,
            onClick = onHome
        ),
        DrawerItem(
            route = Routes.CATALOG,
            label = "Catálogo",
            icon = Icons.Filled.MenuBook,
            onClick = onCatalog
        ),
        DrawerItem(
            route = Routes.NEWS,
            label = "Noticias",
            icon = Icons.Filled.Feed,
            onClick = onNews
        ),
        DrawerItem(
            route = Routes.ACCOUNT_SETTINGS,
            label = "Configuración de la cuenta",
            icon = Icons.Filled.Settings,
            onClick = onAccountSettings
        ),
        DrawerItem(
            // La opción de logout no tiene una ruta de destino, por lo que nunca se marcará
            route = "",
            label = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.Logout,
            onClick = onLogout
        )
    )
}
