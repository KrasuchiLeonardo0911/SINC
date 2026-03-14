package com.sinc.mobile.app.features.agenda.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.domain.model.agenda.AgendaItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AgendaItemCard(
    item: AgendaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val now = LocalDateTime.now()
    val isOverdue = !item.isCompleted && item.fechaProgramada.isBefore(now)
    val tipoColor = getTipoColor(item.tipo)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra Vertical de Color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (item.isCompleted) Color.LightGray else tipoColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Fecha/Hora en Gris
                    Text(
                        text = item.fechaProgramada.format(timeFormatter),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isOverdue) Color.Red else Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                    
                    if (!item.isSynced) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = "Pendiente",
                            tint = Color(0xFFFFA500).copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Título
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = when {
                        item.isCompleted -> Color.Gray
                        isOverdue -> Color.Red
                        else -> Color(0xFF1F2937)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.isCompleted) {
                // Indicador de Completado: Check Verde
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completado",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                // Chevron para Pendientes
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

private fun getTipoColor(tipo: String): Color {
    return when (tipo.lowercase()) {
        "sanidad" -> Color(0xFFE57373)
        "alimentacion" -> Color(0xFFFFB74D)
        "logistica" -> Color(0xFF64B5F6)
        else -> Color(0xFF9575CD)
    }
}
