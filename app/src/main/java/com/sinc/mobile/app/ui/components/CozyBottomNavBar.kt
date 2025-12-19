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
import androidx.compose.ui.draw.shadow
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
        BottomNavItem("Home", CozyBottomNavRoutes.HOME, Icons.Filled.Home),
        BottomNavItem("Explorar", CozyBottomNavRoutes.EXPLORE, Icons.Filled.Explore),
        BottomNavItem("Diario", CozyBottomNavRoutes.JOURNAL, Icons.Filled.Article),
        BottomNavItem("Perfil", CozyBottomNavRoutes.PROFILE, Icons.Filled.Person)
    )

    val leftItems = items.subList(0, 2)
    val rightItems = items.subList(2, 4)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(SoftGray)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Section
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leftItems.forEach { item ->
                CozyBottomNavItem(
                    item = item,
                    isSelected = selectedRoute == item.route,
                    onClick = { onItemSelected(item.route) }
                )
            }
        }

        // Center Button
        AddButtonItem(onClick = { onItemSelected(CozyBottomNavRoutes.ADD) })

        // Right Section
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            rightItems.forEach { item ->
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
            .size(50.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
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

