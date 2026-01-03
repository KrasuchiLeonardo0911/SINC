package com.sinc.mobile.app.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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

private enum class FormDropdownState { Collapsed, Expanded }

@Composable
fun <T> FormDropdown(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String,
    placeholder: String = "Seleccionar",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var currentState by remember { mutableStateOf(FormDropdownState.Collapsed) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val transition = updateTransition(targetState = currentState, label = "FormDropdownTransition")

    val rotationState by transition.animateFloat(
        label = "Rotation",
        transitionSpec = { tween(durationMillis = 300) }
    ) { state ->
        if (state == FormDropdownState.Expanded) 180f else 0f
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Trigger: The visible part inside the FormFieldWrapper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorSize = it }
                .clickable(enabled = enabled) {
                    currentState = if (currentState == FormDropdownState.Collapsed) FormDropdownState.Expanded else FormDropdownState.Collapsed
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedItem?.let(itemToString) ?: placeholder,
                color = if (selectedItem != null && enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Expandir",
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.rotate(rotationState)
            )
        }

        // Popup with the list of items
        if (currentState == FormDropdownState.Expanded) {
            val verticalOffset = with(density) { 16.dp.toPx().toInt() }
            Popup(
                offset = IntOffset(0, anchorSize.height + verticalOffset),
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { currentState = FormDropdownState.Collapsed }
            ) {
                val dropdownShape = RoundedCornerShape(12.dp)

                Card(
                    modifier = Modifier
                        .width(with(density) { anchorSize.width.toDp() })
                        .shadow(elevation = 12.dp, shape = dropdownShape),
                    shape = dropdownShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 184.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(items) { item ->
                            val isSelected = item == selectedItem
                            
                            Card(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onItemSelected(item)
                                            currentState = FormDropdownState.Collapsed
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = itemToString(item),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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