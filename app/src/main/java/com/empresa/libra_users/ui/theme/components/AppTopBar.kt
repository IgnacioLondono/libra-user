package com.empresa.libra_users.ui.theme.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text

import androidx.compose.ui.graphics.vector.ImageVector

// Renombrada para evitar choque con alguna DrawerItem existente
data class NavDrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AppDrawer(
    currentRoute: String?,            // si luego quieres marcar seleccionado
    items: List<NavDrawerItem>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false, // usa (currentRoute == "ruta") si quieres seleccionarlo
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                colors = NavigationDrawerItemDefaults.colors()
            )
        }
    }
}

// Renombrada para evitar "Conflicting overloads"
fun buildDefaultNavDrawerItems(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
): List<NavDrawerItem> = listOf(
    NavDrawerItem("Home", Icons.Filled.Home, onHome),
    NavDrawerItem("Login", Icons.Filled.AccountCircle, onLogin),
    NavDrawerItem("Registro", Icons.Filled.Person, onRegister)
)
