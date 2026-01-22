package com.sinc.mobile.app.features.home.mainscreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data classes for the mockup
data class SanitaryAlert(val title: String, val source: String, val content: String)
data class UpcomingTask(val title: String, val description: String, val dueText: String)
data class Reminder(val title: String, val description: String, val dueText: String)

@Composable
fun AlertsAndTasksDashboard() {
    // Hardcoded data for the mockup
    val sanitaryAlert = SanitaryAlert(
        title = "Riesgo de Fiebre Aftosa",
        source = "Fuente: SENASA",
        content = "Vacunación obligatoria para todo el ganado en la Zona Norte antes del 15/02."
    )
    val upcomingTask = UpcomingTask(
        title = "Desparasitación de Corderos",
        description = "Lote nacido en primavera 2025.",
        dueText = "En 5 días"
    )
    val reminder = Reminder(
        title = "Declaración Jurada Anual",
        description = "Presentación de stock y movimientos del período 2025.",
        dueText = "Vence en 12 días"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SanitaryAlertCard(alert = sanitaryAlert)
        UpcomingTaskCard(task = upcomingTask)
        ReminderCard(reminder = reminder)
    }
}

@Composable
fun SanitaryAlertCard(alert: SanitaryAlert) {
    val alertColor = MaterialTheme.colorScheme.error
    InfoCard(
        icon = Icons.Default.Warning,
        iconTint = alertColor,
        title = alert.title,
        subtitle = alert.source,
        borderColor = alertColor.copy(alpha = 0.5f)
    ) {
        Text(
            text = alert.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UpcomingTaskCard(task: UpcomingTask) {
    val taskColor = MaterialTheme.colorScheme.primary
    InfoCard(
        icon = Icons.Default.CalendarMonth,
        iconTint = taskColor,
        title = task.title,
        subtitle = task.description
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.dueText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = taskColor
            )
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder) {
    val reminderColor = Color(0xFFEF6C00) // A strong orange for attention
    InfoCard(
        icon = Icons.Default.Description,
        iconTint = reminderColor,
        title = reminder.title,
        subtitle = reminder.description,
        borderColor = reminderColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reminder.dueText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = reminderColor
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    content: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (content != null) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}
