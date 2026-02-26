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
        "rojo", "naranja" -> R.raw.lottie_weather_storm
        "amarillo" -> R.raw.lottie_weather_warning
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
            .heightIn(min = 200.dp), // Aumentamos tamaño mínimo
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFF333333)) // Borde un poco más grueso y oscuro
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Texto a la izquierda
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = alert.evento,
                    style = MaterialTheme.typography.headlineSmall,
                    color = SincTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${alert.municipio}\nUP: ${alert.upNombre}",
                    style = MaterialTheme.typography.titleSmall,
                    color = levelColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = alert.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SincTextSecondary,
                    lineHeight = 20.sp,
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Badge de nivel
                Surface(
                    color = levelColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "NIVEL ${alert.nivel.uppercase()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = levelColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Animación a la derecha
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(start = 8.dp),
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
