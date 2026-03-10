package com.sinc.mobile.app.features.weather.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.sinc.mobile.R
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
        else -> MaterialTheme.colorScheme.primary
    }

    val lottieRes = when (alert.nivel.lowercase()) {
        "rojo" -> R.raw.lottie_weather_warning
        "amarillo" -> R.raw.lottie_weather_storm
        "naranja" -> R.raw.lottie_weather_exclamation
        else -> R.raw.lottie_weather_exclamation
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = levelColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = alert.nivel.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = levelColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = alert.evento,
                    style = MaterialTheme.typography.titleLarge,
                    color = SincTextPrimary,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${alert.municipio} • ${alert.upNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SincTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = alert.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SincTextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 3
                )
            }
            
            // Animación Lottie a la derecha y más pequeña
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
