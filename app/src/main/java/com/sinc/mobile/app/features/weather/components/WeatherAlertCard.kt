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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Fila Superior: Título y Animación
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = alert.evento,
                style = MaterialTheme.typography.headlineSmall,
                color = SincTextPrimary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                lineHeight = 28.sp
            )
            
            // Animación muy grande
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(16.dp))

        // Información Inferior
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${alert.municipio} • UP: ${alert.upNombre}",
                style = MaterialTheme.typography.titleSmall,
                color = levelColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = SincTextSecondary,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Badge de nivel
            Surface(
                color = levelColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "NIVEL ${alert.nivel.uppercase()}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = levelColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        // Divisor final opcional si hay varias alertas seguidas
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.2f))
    }
}
