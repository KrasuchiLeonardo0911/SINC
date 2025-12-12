package com.sinc.mobile.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

private enum class DropdownState { Collapsed, Expanded }

@Composable
fun <T> CustomDropdown(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    getItemName: (T) -> String,
    leadingIcon: @Composable () -> Unit,
    placeholder: String
) {
    var currentState by remember { mutableStateOf(DropdownState.Collapsed) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val transition = updateTransition(targetState = currentState, label = "DropdownTransition")

    val rotationState by transition.animateFloat(
        label = "Rotation",
        transitionSpec = { tween(durationMillis = 200) }
    ) { state ->
        if (state == DropdownState.Expanded) 180f else 0f
    }

    val bottomCornerRadius by transition.animateDp(
        label = "BottomCornerRadius",
        transitionSpec = { tween(durationMillis = 200) }
    ) { state ->
        if (state == DropdownState.Expanded) 0.dp else 24.dp
    }

    val inputShadowElevation by transition.animateDp(
        label = "InputShadowElevation",
        transitionSpec = { tween(durationMillis = 200) }
    ) { state ->
        if (state == DropdownState.Expanded) 0.dp else 4.dp
    }

    val inputShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = bottomCornerRadius,
        bottomEnd = bottomCornerRadius
    )
    val dropdownShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

    Box {
        // --- El "Input" que siempre es visible ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorSize = it }
                .clip(inputShape)
                .background(CozyWhite)
                .clickable {
                    currentState = if (currentState == DropdownState.Collapsed) DropdownState.Expanded else DropdownState.Collapsed
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (selectedItem != null) getItemName(selectedItem) else placeholder,
                color = if (selectedItem == null) Color.Gray else CozyTextMain,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Expandir",
                tint = Color.Gray,
                modifier = Modifier.rotate(rotationState)
            )
        }

        // --- El Popup que contiene la lista ---
        if (currentState == DropdownState.Expanded) {
            Popup(
                offset = IntOffset(0, anchorSize.height),
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { currentState = DropdownState.Collapsed }
            ) {
                transition.AnimatedVisibility(
                    visible = { it == DropdownState.Expanded },
                    enter = expandVertically(animationSpec = tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200))
                ) {
                    Card(
                        modifier = Modifier
                            .width(with(density) { anchorSize.width.toDp() }),
                        shape = dropdownShape,
                        colors = CardDefaults.cardColors(containerColor = CozyWhite)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            items.forEachIndexed { index, item ->
                                val isSelected = item == selectedItem
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Transparent)
                                        .clickable {
                                            onItemSelected(item)
                                            currentState = DropdownState.Collapsed
                                        }
                                        .padding(horizontal = 20.dp, vertical = 14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = CozyIconGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = getItemName(item),
                                        color = CozyTextMain,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Seleccionado",
                                            tint = Color(0xFF388E3C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                if (index < items.lastIndex) {
                                    Divider(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        color = CozyDivider
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
