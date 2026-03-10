package com.sinc.mobile.app.features.weather.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.domain.model.WeatherAlert
import com.sinc.mobile.ui.theme.SincTextPrimary
import com.sinc.mobile.ui.theme.SincTextSecondary

@Composable
fun WeatherAlertCard(
    alert: WeatherAlert,
    modifier: Modifier = Modifier
) {
    val levelColor = when (alert.nivel.lowercase()) {
        "rojo" -> Color(0xFFD32F2F)
        "naranja" -> Color(0xFFF57C00)
        "amarillo" -> Color(0xFFFBC02D)
        else -> Color(0xFF667085)
    }

    val icon = when (alert.nivel.lowercase()) {
        "rojo" -> Icons.Outlined.ErrorOutline
        "naranja" -> Icons.Outlined.WarningAmber
        else -> Icons.Outlined.Info
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2F4F7))
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(levelColor)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = alert.nivel.uppercase(),
                        color = levelColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = levelColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = alert.evento,
                    style = MaterialTheme.typography.titleMedium,
                    color = SincTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${alert.municipio} • ${alert.upNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SincTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = alert.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SincTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
