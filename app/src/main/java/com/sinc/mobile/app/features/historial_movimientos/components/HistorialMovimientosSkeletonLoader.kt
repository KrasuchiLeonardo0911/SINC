package com.sinc.mobile.app.features.historial_movimientos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.shimmerBrush

@Composable
fun HistorialMovimientosSkeletonLoader() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerBrush())
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(shimmerBrush(false)) // Use a slightly different shimmer
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Text placeholders
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.7f)
                            .background(shimmerBrush(false))
                    )
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.5f)
                            .background(shimmerBrush(false))
                    )
                }
            }
        }
    }
}
