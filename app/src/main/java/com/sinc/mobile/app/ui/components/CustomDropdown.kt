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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalConfiguration
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
    leadingIcon: @Composable (selectedItem: T?) -> Unit,
    itemLeadingIcon: @Composable (item: T, isSelected: Boolean) -> Unit,
    placeholder: String
) {
    var currentState by remember { mutableStateOf(DropdownState.Collapsed) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val transition = updateTransition(targetState = currentState, label = "DropdownTransition")

    val rotationState by transition.animateFloat(
        label = "Rotation",
        transitionSpec = { tween(durationMillis = 300) }
    ) { state ->
        if (state == DropdownState.Expanded) 180f else 0f
    }

    val menuShadow by transition.animateDp(
        label = "MenuShadow",
        transitionSpec = { tween(durationMillis = 300) }
    ) { state ->
        if (state == DropdownState.Expanded) 12.dp else 0.dp
    }

    val inputShape = RoundedCornerShape(12.dp)
    val dropdownShape = RoundedCornerShape(12.dp)

    Box {
        // --- El "Input" que siempre es visible (Trigger) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorSize = it }
                .shadow(elevation = 8.dp, shape = inputShape)
                .clip(inputShape)
                .background(if (selectedItem != null) AccentYellow else CozyWhite) // Color condicional
                .clickable {
                    currentState =
                        if (currentState == DropdownState.Collapsed) DropdownState.Expanded else DropdownState.Collapsed
                }
                .padding(horizontal = 16.dp, vertical = 12.dp), // Ajuste de padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon(selectedItem) // Pasar estado de selección
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (selectedItem != null) getItemName(selectedItem) else placeholder,
                color = if (selectedItem != null) CozyTextMain else InactiveGray, // Color condicional
                fontSize = 16.sp, // Fuente más grande
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Expandir",
                tint = if (selectedItem != null) CozyTextMain else InactiveGray, // Color condicional para chevron
                modifier = Modifier.rotate(rotationState)
            )
        }

        // --- El Popup que contiene la lista ---
        if (currentState == DropdownState.Expanded) {
            val verticalOffset = with(density) { 4.dp.toPx().toInt() }
            Popup(
                offset = IntOffset(0, anchorSize.height + verticalOffset), // Ajuste de offset para que no se superponga
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { currentState = DropdownState.Collapsed }
            ) {
                transition.AnimatedVisibility(
                    visible = { it == DropdownState.Expanded },
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(500)) // Animación de cierre más suave
                ) {
                    Card(
                        modifier = Modifier
                            .width(with(density) { anchorSize.width.toDp() })
                            .shadow(elevation = menuShadow, shape = dropdownShape),
                        shape = dropdownShape,
                        colors = CardDefaults.cardColors(containerColor = PaleWarmGray)
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 184.dp), // Aprox 3 items
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(items.size) { index ->
                                val item = items[index]
                                val isSelected = item == selectedItem

                                // Cápsula para cada opción
                                Card(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) AccentYellow else CozyWhite
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onItemSelected(item)
                                                currentState = DropdownState.Collapsed
                                            }
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        itemLeadingIcon(item, isSelected)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = getItemName(item),
                                            color = if (isSelected) CozyTextMain else InactiveGray,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 16.sp,
                                            modifier = Modifier.weight(1f)
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
}
