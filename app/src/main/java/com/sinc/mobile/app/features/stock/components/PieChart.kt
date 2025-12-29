package com.sinc.mobile.app.features.stock.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class PieChartData(
    val value: Float,
    val color: Color
)

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 20f
) {
    val totalValue = data.sumOf { it.value.toDouble() }.toFloat()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val canvasSize = minOf(maxWidth, maxHeight)

        Canvas(modifier = Modifier.size(canvasSize)) {
            var startAngle = -90f
            
            data.forEach { slice ->
                val sweepAngle = (slice.value / totalValue) * 360f
                if (sweepAngle > 0.1f) { // Draw only if slice is visible enough
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}
