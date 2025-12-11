package com.empresa.libra_users.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullRefreshIndicator
import androidx.compose.material3.pulltorefresh.pullRefresh
import androidx.compose.material3.pulltorefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.empresa.libra_users.screen.admin.books.AdminBooksScreen
import com.empresa.libra_users.screen.admin.loans.AdminLoansScreen
import com.empresa.libra_users.screen.admin.reports.AdminReportsScreen
import com.empresa.libra_users.screen.admin.users.AdminUsersScreen
import com.empresa.libra_users.viewmodel.MainViewModel
import com.empresa.libra_users.viewmodel.admin.AdminDashboardViewModel

// ============================================================================
// DATA CLASSES
// ============================================================================

data class AdminNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// ============================================================================
// MAIN ADMIN DASHBOARD SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val navItems = listOf(
        AdminNavItem("Panel", Icons.Filled.Dashboard, "admin_dashboard"),
        AdminNavItem("Libros", Icons.Filled.Book, "admin_books"),
        AdminNavItem("Usuarios", Icons.Filled.Group, "admin_users"),
        AdminNavItem("Préstamos", Icons.Filled.SwapHoriz, "admin_loans"),
        AdminNavItem("Informes", Icons.Filled.BarChart, "admin_reports")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "admin_dashboard"

    val mainViewModel: MainViewModel = hiltViewModel()
    val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AdminTopAppBar(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { mainViewModel.toggleDarkMode() },
                onLogout = { mainViewModel.logout() }
            )
        },
        bottomBar = {
            AdminBottomNavigationBar(
                navController = navController,
                navItems = navItems,
                currentRoute = currentRoute
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "admin_dashboard",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable("admin_dashboard") {
                AdminHomeScreen()
            }
            composable("admin_books") { 
                AdminBooksScreen() 
            }
            composable("admin_users") { 
                AdminUsersScreen() 
            }
            composable("admin_loans") { 
                AdminLoansScreen() 
            }
            composable("admin_reports") { 
                AdminReportsScreen() 
            }
        }
    }
}

// ============================================================================
// TOP APP BAR
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopAppBar(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Dashboard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Panel de Administración",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sistema de Gestión Bibliotecaria",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            // Botón de modo oscuro/claro
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = if (isDarkMode) "Cambiar a modo claro" else "Cambiar a modo oscuro",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            // Botón de logout
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar Sesión",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

// ============================================================================
// ADMIN HOME SCREEN (DASHBOARD CON STATISTICS CARDS)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val dashboardUiState by viewModel.dashboardUiState.collectAsStateWithLifecycle()
    val reportsUiState by viewModel.reportsUiState.collectAsStateWithLifecycle()
    
    // Pull to refresh
    val isRefreshing = dashboardUiState.isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshDashboard() }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        // Estado de carga
        if (dashboardUiState.isLoading) {
            LoadingState()
        } 
        // Estado de error
        else if (dashboardUiState.error != null) {
            ErrorState(errorMessage = dashboardUiState.error!!)
        } 
        // Contenido principal: Grid de estadísticas
        else {
            StatisticsGrid(
                dashboardUiState = dashboardUiState,
                reportsUiState = reportsUiState
            )
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ============================================================================
// STATISTICS GRID (2 COLUMNS, 3 ROWS)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatisticsGrid(
    dashboardUiState: com.empresa.libra_users.viewmodel.admin.AdminDashboardUiState,
    reportsUiState: com.empresa.libra_users.viewmodel.admin.AdminReportsUiState
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 2
    ) {
        // Fila 1: Total de Libros, Préstamos
        StatCard(
            icon = Icons.Filled.Book,
            value = dashboardUiState.totalBooks.toString(),
            label = "Total de Libros",
            subtitle = "En el inventario",
            gradientColors = listOf(
                Color(0xFF1976D2), // Azul oscuro
                Color(0xFF1565C0).copy(alpha = 0.8f)
            )
        )
        
        StatCard(
            icon = Icons.Filled.Verified,
            value = dashboardUiState.totalLoans.toString(),
            label = "Préstamos",
            subtitle = "Total histórico",
            gradientColors = listOf(
                Color(0xFF42A5F5), // Azul claro
                Color(0xFF1E88E5).copy(alpha = 0.8f)
            )
        )
        
        // Fila 2: Usuarios, Disponibles
        StatCard(
            icon = Icons.Filled.Group,
            value = dashboardUiState.totalUsers.toString(),
            label = "Usuarios",
            subtitle = "Registrados",
            gradientColors = listOf(
                Color(0xFF616161), // Gris oscuro
                Color(0xFF424242).copy(alpha = 0.8f)
            )
        )
        
        StatCard(
            icon = Icons.Filled.CheckCircle,
            value = reportsUiState.libraryStatus.available.toString(),
            label = "Disponibles",
            subtitle = "Libros disponibles",
            gradientColors = listOf(
                Color(0xFF42A5F5), // Azul claro
                Color(0xFF1E88E5).copy(alpha = 0.8f)
            )
        )
        
        // Fila 3: Pendientes, Prestados
        StatCard(
            icon = Icons.Filled.PendingActions,
            value = dashboardUiState.pendingLoans.toString(),
            label = "Pendientes",
            subtitle = "Préstamos activos",
            gradientColors = listOf(
                Color(0xFF7B1FA2), // Púrpura
                Color(0xFF6A1B9A).copy(alpha = 0.8f)
            ),
            showWarning = dashboardUiState.pendingLoans > 0
        )
        
        StatCard(
            icon = Icons.Filled.SwapHoriz,
            value = reportsUiState.libraryStatus.loaned.toString(),
            label = "Prestados",
            subtitle = "Libros prestados",
            gradientColors = listOf(
                Color(0xFF42A5F5), // Azul claro
                Color(0xFF1E88E5).copy(alpha = 0.8f)
            )
        )
    }
}

// ============================================================================
// STATISTICS CARD COMPONENT
// ============================================================================

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    subtitle: String = "",
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    ),
    showWarning: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 200),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth(0.48f)
            .scale(scale)
            .clickable { },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icono y warning (si aplica)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icono con fondo circular
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(colors = gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Icono de advertencia (si hay pendientes)
                if (showWarning) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Valor numérico principal
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Etiqueta principal
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Subtítulo (si existe)
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// LOADING STATE
// ============================================================================

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cargando estadísticas...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// ERROR STATE
// ============================================================================

@Composable
private fun ErrorState(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// ============================================================================
// BOTTOM NAVIGATION BAR
// ============================================================================

@Composable
fun AdminBottomNavigationBar(
    navController: NavController,
    navItems: List<AdminNavItem>,
    currentRoute: String
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = tween(durationMillis = 200),
                label = "nav_item_scale"
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.scale(scale)
                    )
                },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
// A simple placeholder screen for sections not yet built
@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}

