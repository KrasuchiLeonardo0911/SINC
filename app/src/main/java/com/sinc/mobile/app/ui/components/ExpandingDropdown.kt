package com.sinc.mobile.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> ExpandingDropdown(
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
    selectedItemBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedItemTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedCheckmarkColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    var isExpanded by remember { mutableStateOf(false) }
    val cornerRadius = 24.dp
    val elevation = 4.dp // Use elevation for Card

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- Trigger ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // No ripple effect
                        enabled = enabled
                    ) {
                        isExpanded = !isExpanded
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
                        color = if (selectedItem != null && enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            // --- Expandable Menu ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                    // Content will inherit the Card's background
                ) {
                    items.forEach { item ->
                        val isSelected = item == selectedItem
                        ExpandingMenuItem(
                            item = item,
                            isSelected = isSelected,
                            onItemSelected = {
                                onItemSelected(it)
                                isExpanded = false
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

@Composable
private fun <T> ExpandingMenuItem(
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
                color = if (isSelected) selectedItemTextColor else MaterialTheme.colorScheme.onSurface,
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