package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.shimmerBrush
import com.sinc.mobile.ui.theme.CozyWhite

@Composable
fun MovimientoSkeletonLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Placeholder para 5 campos ---
        repeat(5) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CozyWhite)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Placeholder for the Label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f) // Short width for label
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush())
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Placeholder for the Input Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(shimmerBrush())
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Placeholder para Botón de Guardar ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(ButtonDefaults.shape)
                .background(shimmerBrush())
        )
    }
}
