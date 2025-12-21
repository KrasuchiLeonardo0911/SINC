package com.sinc.mobile.app.ui.components

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
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
enum class PopupDirection { Up, Down }

@OptIn(ExperimentalFoundationApi::class)
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
    selectedCheckmarkColor: Color = CozyWhite,
    direction: PopupDirection = PopupDirection.Down,
    onDisabledClick: (() -> Unit)? = null
) {
    var currentState by remember { mutableStateOf(SoftDropdownState.Collapsed) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val transition = updateTransition(targetState = currentState, label = "SoftDropdownTransition")

    val rotationState by transition.animateFloat(
        label = "Rotation",
        transitionSpec = { tween(durationMillis = 0) }
    ) { state ->
        if (direction == PopupDirection.Down) {
            if (state == SoftDropdownState.Expanded) 180f else 0f
        } else { // Up
            if (state == SoftDropdownState.Expanded) 0f else 180f
        }
    }

    val menuAlpha by transition.animateFloat(
        label = "MenuAlpha",
        transitionSpec = { tween(durationMillis = 0) }
    ) { state ->
        if (state == SoftDropdownState.Expanded) 1f else 0f
    }

    val menuTranslationY by transition.animateDp(
        label = "MenuTranslationY",
        transitionSpec = { tween(durationMillis = 0) }
    ) { state ->
        if (state == SoftDropdownState.Expanded) {
            0.dp
        } else {
            if (direction == PopupDirection.Down) (-10).dp else 10.dp
        }
    }
    
    val cornerRadius = 24.dp

    val triggerShape = if (direction == PopupDirection.Up) {
        RoundedCornerShape(cornerRadius)
    } else {
        val animatedBottomRadius by transition.animateDp(label = "TriggerRadius", transitionSpec = { tween(durationMillis = 0) }) { state ->
            if (state == SoftDropdownState.Expanded) 0.dp else cornerRadius
        }
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = animatedBottomRadius,
            bottomEnd = animatedBottomRadius
        )
    }
    
    val triggerShadow = 8.dp

    val menuShape = if (direction == PopupDirection.Up) {
        RoundedCornerShape(cornerRadius)
    } else {
        RoundedCornerShape(
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )
    }
    val menuShadow = 8.dp 

    Box(modifier = modifier.onSizeChanged { anchorSize = it }) {
        // --- Trigger ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = triggerShadow, shape = triggerShape)
                .clip(triggerShape)
                .background(CozyWhite)
                .then(
                    if (enabled) {
                        Modifier.clickable {
                            currentState = if (currentState == SoftDropdownState.Collapsed) SoftDropdownState.Expanded else SoftDropdownState.Collapsed
                        }
                    } else {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDisabledClick?.invoke()
                        }
                    }
                )
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
        if (currentState == SoftDropdownState.Expanded && transition.targetState == SoftDropdownState.Expanded) {
            Popup(
                alignment = if (direction == PopupDirection.Down) Alignment.TopStart else Alignment.BottomStart,
                offset = if (direction == PopupDirection.Down) {
                    IntOffset(0, with(density) { anchorSize.height.toDp().roundToPx() })
                } else {
                    val margin = with(density) { 8.dp.roundToPx() }
                    IntOffset(0, -with(density) { anchorSize.height.toDp().roundToPx() } - margin)
                },
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
                        .shadow(elevation = menuShadow, shape = menuShape) 
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