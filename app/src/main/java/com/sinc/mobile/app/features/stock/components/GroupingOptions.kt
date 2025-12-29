package com.sinc.mobile.app.features.stock.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.stock.StockGrouping

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupingOptions(
    selectedGrouping: StockGrouping,
    onGroupingSelected: (StockGrouping) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedGrouping == StockGrouping.BY_ALL,
                onClick = { onGroupingSelected(StockGrouping.BY_ALL) },
                label = { Text("Todos", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
        item {
            FilterChip(
                selected = selectedGrouping == StockGrouping.BY_CATEGORY,
                onClick = { onGroupingSelected(StockGrouping.BY_CATEGORY) },
                label = { Text("Por Categoría", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
        item {
            FilterChip(
                selected = selectedGrouping == StockGrouping.BY_BREED,
                onClick = { onGroupingSelected(StockGrouping.BY_BREED) },
                label = { Text("Por Raza", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}
