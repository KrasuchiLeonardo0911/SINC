package com.sinc.mobile.app.features.agenda.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val now = LocalDateTime.now()
    val isOverdue = !item.isCompleted && item.fechaProgramada.isBefore(now)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox Estilizado
        IconButton(
            onClick = onToggleStatus,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = when {
                    item.isCompleted -> Color(0xFF4CAF50)
                    isOverdue -> Color.Red
                    else -> Color.LightGray
                },
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Hora
                Text(
                    text = item.fechaProgramada.format(timeFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverdue) Color.Red.copy(alpha = 0.7f) else Color.Gray
                )
            }
            
            val description = item.descripcion
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Tag de Tipo
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(getTipoColor(item.tipo).copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.tipo.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = getTipoColor(item.tipo),
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botón Eliminar (Sutil)
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
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
