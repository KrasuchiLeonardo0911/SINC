package com.sinc.mobile.app.features.stock.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.app.features.stock.StockGrouping

@Composable
fun GroupingOptions(
    selectedGrouping: StockGrouping,
    onGroupingSelected: (StockGrouping) -> Unit,
    modifier: Modifier = Modifier,
    selectedChipColor: Color = MaterialTheme.colorScheme.primary
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.White,
        labelColor = selectedChipColor,
        selectedContainerColor = selectedChipColor,
        selectedLabelColor = Color.White
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
    ) {
        item {
            val isSelected = selectedGrouping == StockGrouping.BY_ALL
            FilterChip(
                selected = isSelected,
                onClick = { onGroupingSelected(StockGrouping.BY_ALL) },
                label = { 
                    Text(
                        text = "Todos", 
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = selectedChipColor,
                    selectedBorderColor = selectedChipColor,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
        item {
            val isSelected = selectedGrouping == StockGrouping.BY_CATEGORY
            FilterChip(
                selected = isSelected,
                onClick = { onGroupingSelected(StockGrouping.BY_CATEGORY) },
                label = { 
                    Text(
                        text = "Por Categoría", 
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = selectedChipColor,
                    selectedBorderColor = selectedChipColor,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
        item {
            val isSelected = selectedGrouping == StockGrouping.BY_BREED
            FilterChip(
                selected = isSelected,
                onClick = { onGroupingSelected(StockGrouping.BY_BREED) },
                label = { 
                    Text(
                        text = "Por Raza", 
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = selectedChipColor,
                    selectedBorderColor = selectedChipColor,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
    }
}
