package com.sinc.mobile.app.features.stock.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    Row(
        modifier = modifier
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedGrouping == StockGrouping.BY_ALL,
            onClick = { onGroupingSelected(StockGrouping.BY_ALL) },
            label = { Text("Todos", maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        FilterChip(
            selected = selectedGrouping == StockGrouping.BY_CATEGORY,
            onClick = { onGroupingSelected(StockGrouping.BY_CATEGORY) },
            label = { Text("Por Categoría", maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        FilterChip(
            selected = selectedGrouping == StockGrouping.BY_BREED,
            onClick = { onGroupingSelected(StockGrouping.BY_BREED) },
            label = { Text("Por Raza", maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
    }
}
