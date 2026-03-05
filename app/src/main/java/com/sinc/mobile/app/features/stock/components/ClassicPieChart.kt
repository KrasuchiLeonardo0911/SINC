package com.sinc.mobile.app.features.stock.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClassicPieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    val totalValue = data.sumOf { it.value.toDouble() }.toFloat()
    val largestIndex = data.indices.maxByOrNull { data[it].value } ?: -1

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val chartSize = size.minDimension
            
            // 1. Dibujar Sombras Primero (Capa de fondo)
            // Esto asegura que la sombra esté bajo todas las porciones
            var shadowStartAngle = -90f
            data.forEachIndexed { index, slice ->
                val sweepAngle = (slice.value / totalValue) * 360f
                if (sweepAngle > 0.1f) {
                    val isExploded = index == largestIndex
                    val angleInRadians = Math.toRadians((shadowStartAngle + sweepAngle / 2).toDouble())
                    val offsetDistance = if (isExploded) 14f else 4f // Sombra un poco más lejos para la explotada
                    
                    val offset = Offset(
                        x = (cos(angleInRadians) * offsetDistance).toFloat() + 4f,
                        y = (sin(angleInRadians) * offsetDistance).toFloat() + 4f
                    )

                    drawArc(
                        color = Color.Black.copy(alpha = 0.15f),
                        startAngle = shadowStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = Fill,
                        topLeft = offset,
                        size = Size(chartSize, chartSize)
                    )
                    shadowStartAngle += sweepAngle
                }
            }

            // 2. Dibujar Porciones Reales
            startAngle = -90f
            data.forEachIndexed { index, slice ->
                val sweepAngle = (slice.value / totalValue) * 360f
                if (sweepAngle > 0.1f) {
                    val isExploded = index == largestIndex
                    val angleInRadians = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val offsetDistance = if (isExploded) 12f else 0f
                    
                    val offset = Offset(
                        x = (cos(angleInRadians) * offsetDistance).toFloat(),
                        y = (sin(angleInRadians) * offsetDistance).toFloat()
                    )

                    // Porción de Pizza
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = Fill,
                        topLeft = offset,
                        size = Size(chartSize, chartSize)
                    )

                    // Borde blanco sutil para separar
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = Stroke(width = 3f),
                        topLeft = offset,
                        size = Size(chartSize, chartSize)
                    )
                    
                    startAngle += sweepAngle
                }
            }
        }
    }
}
