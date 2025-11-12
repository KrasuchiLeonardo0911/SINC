package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.sinc.mobile.app.features.home.MainScreenRoutes

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem("Inicio", Icons.Outlined.Home, MainScreenRoutes.DASHBOARD),
        BottomNavItem("Cuaderno", Icons.Outlined.Book, MainScreenRoutes.MOVIMIENTO),
        BottomNavItem("Notificaciones", Icons.Outlined.Notifications, MainScreenRoutes.NOTIFICATIONS)
    )

    NavigationBar(
        modifier = Modifier.windowInsetsPadding(NavigationBarDefaults.windowInsets),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = null, // Remove label to show only the icon
                alwaysShowLabel = false
            )
        }
    }
}
