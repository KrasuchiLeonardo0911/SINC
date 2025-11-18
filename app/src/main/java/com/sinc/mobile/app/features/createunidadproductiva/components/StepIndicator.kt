package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StepIndicator(
    modifier: Modifier = Modifier,
    currentStep: Int,
    onStepSelected: (Int) -> Unit
) {
    val steps = listOf("Datos Básicos", "Ubicación", "Detalles")
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White, // Fondo blanco
        shadowElevation = 4.dp // Sombra sutil
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            steps.forEachIndexed { index, title ->
                val stepNumber = index + 1
                StepCard(
                    modifier = Modifier.weight(1f),
                    title = title,
                    isActive = currentStep == stepNumber,
                    onClick = { onStepSelected(stepNumber) }
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(300),
        label = "color"
    )
    val lineHeight by animateDpAsState(
        targetValue = if (isActive) 3.dp else 1.dp,
        animationSpec = tween(300),
        label = "height"
    )

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = color,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Divider(
            color = color,
            thickness = lineHeight,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}