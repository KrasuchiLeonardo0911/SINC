package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.shimmerBrush

@Composable
fun MovimientoSkeletonLoader(
    modifier: Modifier = Modifier,
    showShimmer: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Replicate the 5 form fields ---
        repeat(5) {
            Column {
                // Placeholder for the Label
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f) // Short width for label
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush(showShimmer = showShimmer))
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Placeholder for the Input Field (like SoftDropdown or QuantitySelector)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp) // Approximate height of a SoftDropdown
                        .clip(RoundedCornerShape(16.dp)) // Match SoftDropdown rounding
                        .background(shimmerBrush(showShimmer = showShimmer))
                )
            }
        }
    }
}