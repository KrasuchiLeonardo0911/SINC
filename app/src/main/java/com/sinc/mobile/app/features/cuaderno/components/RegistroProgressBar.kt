package com.sinc.mobile.app.features.cuaderno.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RegistroProgressBar(currentStep: Int) {
    // Dividimos por 2.0f porque ahora son solo 2 pasos (Fecha y Descripción)
    val progress by animateFloatAsState(
        targetValue = currentStep / 2f,
        animationSpec = tween(durationMillis = 500),
        label = "RegistroProgressBarAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(3.dp))
            .clip(RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(3.dp))
        )
    }
}
