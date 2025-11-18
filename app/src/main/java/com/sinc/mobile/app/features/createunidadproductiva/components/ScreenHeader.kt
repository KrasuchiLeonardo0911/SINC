package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp) // Aumentamos la altura
            .background(MaterialTheme.colorScheme.primary) // Color sólido
    ) {
        Text(
            text = "Registrar Campo",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomStart) // Alineado abajo a la izquierda
                .padding(16.dp)
        )
    }
}