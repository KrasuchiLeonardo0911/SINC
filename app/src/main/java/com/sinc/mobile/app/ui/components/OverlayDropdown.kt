package com.sinc.mobile.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun <T> OverlayDropdown(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    getItemName: (T) -> String,
    leadingIcon: @Composable () -> Unit,
    placeholder: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }

    Box {
        // --- El "Input" que siempre es visible ---
        val shape = RoundedCornerShape(24.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorSize = it }
                .shadow(elevation = 4.dp, shape = shape, clip = false)
                .clip(shape)
                .background(CozyWhite)
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(Modifier.width(16.dp))
            Text(
                text = if (selectedItem != null) getItemName(selectedItem) else placeholder,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = CozyTextMain,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Desplegar",
                tint = CozyIconGray
            )
        }

        // --- El Popup que contiene la lista ---
        if (isExpanded) {
            Popup(
                offset = IntOffset(0, with(LocalDensity.current) { anchorSize.height.toDp().roundToPx() }),
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { isExpanded = false }
            ) {
                Box(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { anchorSize.width.toDp() })
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .background(CozyWhite)
                            .border(BorderStroke(1.dp, CozyYellow), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        items.forEachIndexed { index, item ->
                            val isSelected = item == selectedItem
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onItemSelected(item)
                                        isExpanded = false
                                    }
                                    .background(if (isSelected) CreamyYellow else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (index % 3) {
                                    0 -> Icons.Outlined.Eco
                                    1 -> Icons.Outlined.Map
                                    else -> Icons.Outlined.Home
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF388E3C) else CozyIconGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = getItemName(item),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = CozyTextMain,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = CozyYellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            if (index < items.lastIndex) {
                                HorizontalDivider(
                                    color = CozyDivider,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
