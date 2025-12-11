package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.theme.*

object CozyBottomNavRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val ADD = "add"
    const val JOURNAL = "journal"
    const val PROFILE = "profile"
}

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
        BottomNavItem("Home", CozyBottomNavRoutes.HOME, Icons.Filled.Home),
        BottomNavItem("Explorar", CozyBottomNavRoutes.EXPLORE, Icons.Filled.Explore),
        BottomNavItem("Add", CozyBottomNavRoutes.ADD, Icons.Filled.Add), // Treat as a regular item
        BottomNavItem("Diario", CozyBottomNavRoutes.JOURNAL, Icons.Filled.Article),
        BottomNavItem("Perfil", CozyBottomNavRoutes.PROFILE, Icons.Filled.Person)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(SoftGray)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            if (item.route == CozyBottomNavRoutes.ADD) {
                AddButtonItem(onClick = { onItemSelected(item.route) })
            } else {
                CozyBottomNavItem(
                    item = item,
                    isSelected = selectedRoute == item.route,
                    onClick = { onItemSelected(item.route) }
                )
            }
        }
    }
}

@Composable
fun RowScope.AddButtonItem(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(CozyYellow)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
            tint = CozyTextMain,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun RowScope.CozyBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        val icon = if (isSelected) {
            // Relleno para el estado activo (ej. Icons.Filled.Home)
            when (item.route) {
                CozyBottomNavRoutes.HOME -> Icons.Filled.Home
                CozyBottomNavRoutes.EXPLORE -> Icons.Filled.Explore
                CozyBottomNavRoutes.JOURNAL -> Icons.Filled.Article
                CozyBottomNavRoutes.PROFILE -> Icons.Filled.Person
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

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) CozyTextMain else CozyIconGray
        )
    }
}
