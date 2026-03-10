package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sinc.mobile.ui.theme.*


data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)


@Composable
fun CozyBottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    unreadNotificationCount: Int,
    hasWeatherAlerts: Boolean = false
) {
    val items = listOf(
        BottomNavItem("Inicio", CozyBottomNavRoutes.HOME, Icons.Outlined.Home),
        BottomNavItem("Agenda", CozyBottomNavRoutes.AGENDA, Icons.Outlined.Event),
        BottomNavItem("Clima", CozyBottomNavRoutes.WEATHER, Icons.Outlined.WbCloudy),
        BottomNavItem("Notif", CozyBottomNavRoutes.NOTIFICATIONS, Icons.Outlined.Notifications),
        BottomNavItem("Perfil", CozyBottomNavRoutes.PROFILE, Icons.Outlined.Person)
    )

    Column(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.9f)) // Fondo negro para el área del sistema
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp) // Un poco más de altura para elegancia
                .background(SincBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                val showBadge = when (item.route) {
                    CozyBottomNavRoutes.NOTIFICATIONS -> unreadNotificationCount > 0
                    CozyBottomNavRoutes.WEATHER -> hasWeatherAlerts
                    else -> false
                }
                
                CozyBottomNavItem(
                    item = item,
                    isSelected = selectedRoute == item.route,
                    showBadge = showBadge,
                    onClick = { onItemSelected(item.route) }
                )
            }
        }
    }
}


@Composable
fun RowScope.CozyBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    showBadge: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            val icon = item.icon // Always use Outlined version for a finer look

            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else SincTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }


            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else SincTextSecondary
            )
        }
    }
}

