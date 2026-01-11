package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
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
import com.sinc.mobile.app.ui.theme.*


data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)


@Composable
fun CozyBottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem("Inicio", CozyBottomNavRoutes.HOME, Icons.Outlined.Home),
        BottomNavItem("Ayuda", CozyBottomNavRoutes.HELP, Icons.Outlined.HelpOutline),
        BottomNavItem("Perfil", CozyBottomNavRoutes.PROFILE, Icons.Outlined.Person),
        BottomNavItem("Alertas", CozyBottomNavRoutes.NOTIFICATIONS, Icons.Outlined.Notifications)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp) // Altura reducida
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround // Distribuir uniformemente
    ) {
        items.forEach { item ->
            CozyBottomNavItem(
                item = item,
                isSelected = selectedRoute == item.route,
                onClick = { onItemSelected(item.route) }
            )
        }
    }
}


@Composable

fun RowScope.CozyBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
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
        contentAlignment = Alignment.Center // Center content vertically and horizontally
    ) {
        // Indicator Line
        val indicatorColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter) // Align to the very top of the Box
                .height(3.dp)
                .width(36.dp) // Lengthen the indicator line slightly
                .background(indicatorColor, shape = CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight() // Fill the remaining height after the indicator
                .padding(top = 0.dp) // Reduce space between top of bar and icons
        ) {
            val icon = if (isSelected) {
                // Relleno para el estado activo
                when (item.route) {
                    CozyBottomNavRoutes.HOME -> Icons.Filled.Home
                    CozyBottomNavRoutes.HELP -> Icons.Filled.Help
                    CozyBottomNavRoutes.PROFILE -> Icons.Filled.Person
                    CozyBottomNavRoutes.NOTIFICATIONS -> Icons.Filled.Notifications
                    else -> item.icon // Fallback
                }
            } else {
                // Contorno para el estado inactivo
                item.icon
            }

            Icon(
                imageVector = icon,
                contentDescription = item.label,
                tint = if (isSelected) CozyTextMain else CozyIconGray,
                modifier = Modifier.size(24.dp)
            )


            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) CozyTextMain else CozyIconGray
            )
        }
    }
}

