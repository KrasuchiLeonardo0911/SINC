package com.sinc.mobile.app.ui.components

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.sinc.mobile.ui.theme.*


private enum class SoftDropdownState { Collapsed, Expanded }

@Composable
fun <T> SoftDropdown(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    getItemName: (T) -> String,
    placeholder: String,
    triggerIcon: @Composable (selectedItem: T?) -> Unit,
    itemIcon: @Composable (item: T, isSelected: Boolean) -> Unit,
    enabled: Boolean = true,
    showItemIcons: Boolean = true,
    selectedItemBackgroundColor: Color = AccentGreen,
    selectedItemTextColor: Color = DarkGreen,
    selectedCheckmarkColor: Color = CozyWhite
) {
    var currentState by remember { mutableStateOf(SoftDropdownState.Collapsed) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val transition = updateTransition(targetState = currentState, label = "SoftDropdownTransition")

    val rotationState by transition.animateFloat(
        label = "Rotation",
        transitionSpec = { tween(durationMillis = 300) }
    ) { state ->
        if (state == SoftDropdownState.Expanded) 180f else 0f
    }

    val menuAlpha by transition.animateFloat(
        label = "MenuAlpha",
        transitionSpec = { tween(durationMillis = 300) }
    ) { state ->
        if (state == SoftDropdownState.Expanded) 1f else 0f
    }

    val menuTranslationY by transition.animateDp(
        label = "MenuTranslationY",
        transitionSpec = { tween(durationMillis = 300) }
    ) { state ->
        if (state == SoftDropdownState.Expanded) 0.dp else (-10).dp
    }
    
    val cornerRadius = 24.dp
    val triggerBottomRadius by transition.animateDp(label = "TriggerRadius") { state ->
        if (state == SoftDropdownState.Expanded) 0.dp else cornerRadius
    }

    val triggerShape = RoundedCornerShape(
        topStart = cornerRadius,
        topEnd = cornerRadius,
        bottomStart = triggerBottomRadius,
        bottomEnd = triggerBottomRadius
    )
    
    val triggerShadow = 8.dp

    val menuShape = RoundedCornerShape(
        bottomStart = cornerRadius,
        bottomEnd = cornerRadius
    )
    val menuShadow = 8.dp // Re-added menuShadow

    Box(modifier = modifier.onSizeChanged { anchorSize = it }) {
        // --- Trigger ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = triggerShadow, shape = triggerShape)
                .clip(triggerShape)
                .background(CozyWhite)
                .clickable(enabled = enabled) {
                    currentState = if (currentState == SoftDropdownState.Collapsed) SoftDropdownState.Expanded else SoftDropdownState.Collapsed
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                triggerIcon(selectedItem)
                Spacer(Modifier.width(16.dp))
                Text(
                    text = selectedItem?.let(getItemName) ?: placeholder,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedItem != null && enabled) CozyTextMain else InactiveGray,
                    fontSize = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Expandir",
                tint = if (enabled) CozyTextMain else InactiveGray,
                modifier = Modifier.rotate(rotationState)
            )
        }

        // --- Menu ---
        if (currentState == SoftDropdownState.Expanded) {
            Popup(
                offset = IntOffset(0, with(density) { anchorSize.height.toDp().roundToPx() }),
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { currentState = SoftDropdownState.Collapsed }
            ) {
                Box(
                    modifier = Modifier
                        .width(with(density) { anchorSize.width.toDp() })
                        .graphicsLayer {
                            alpha = menuAlpha
                            translationY = menuTranslationY.toPx()
                        }
                        .shadow(elevation = menuShadow, shape = menuShape) // Re-added shadow
                        .clip(menuShape)
                        .background(CozyWhite)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp), // Approx 4 items
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(items) { item ->
                            val isSelected = item == selectedItem
                            MenuItem(
                                item = item,
                                isSelected = isSelected,
                                onItemSelected = {
                                    onItemSelected(it)
                                    currentState = SoftDropdownState.Collapsed
                                },
                                getItemName = getItemName,
                                itemIcon = itemIcon,
                                showItemIcons = showItemIcons,
                                selectedItemBackgroundColor = selectedItemBackgroundColor,
                                selectedItemTextColor = selectedItemTextColor,
                                selectedCheckmarkColor = selectedCheckmarkColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> MenuItem(
    item: T,
    isSelected: Boolean,
    onItemSelected: (T) -> Unit,
    getItemName: (T) -> String,
    itemIcon: @Composable (item: T, isSelected: Boolean) -> Unit,
    showItemIcons: Boolean,
    selectedItemBackgroundColor: Color,
    selectedItemTextColor: Color,
    selectedCheckmarkColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) selectedItemBackgroundColor else Color.Transparent)
            .clickable { onItemSelected(item) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showItemIcons) {
                itemIcon(item, isSelected)
                Spacer(Modifier.width(16.dp))
            }
            Text(
                text = getItemName(item),
                color = if (isSelected) selectedItemTextColor else CozyTextMain,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 16.sp
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = selectedCheckmarkColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Composable para el icono circular como se ve en el ejemplo
@Composable
fun SoftDropdownIcon(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CozyYellow,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}