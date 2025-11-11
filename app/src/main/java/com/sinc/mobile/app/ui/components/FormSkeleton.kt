package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.shimmer

@Composable
fun FormSkeleton(
    modifier: Modifier = Modifier
) {
    // Define el efecto shimmer una vez
    val shimmerHighlight = PlaceholderHighlight.shimmer(
        highlightColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    Column(modifier = modifier.padding(top = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        // Placeholder for Especie Selector
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .placeholder(visible = true, highlight = shimmerHighlight)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Placeholder for Categoria Selector
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .placeholder(visible = true, highlight = shimmerHighlight)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Placeholder for Raza Selector
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .placeholder(visible = true, highlight = shimmerHighlight)
        )
    }
}
