package com.sinc.mobile.app.features.home.mainscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.theme.*

@Composable
fun WeekdaySelector(
    onDateClick: () -> Unit
) {
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val dates = listOf("7", "8", "9", "10", "11", "12", "13")
    val selectedDate = "10" // Today's date is always selected

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly // Use SpaceEvenly for better alignment
        ) {
            days.forEach { day ->
                Column(
                    modifier = Modifier.width(36.dp), // Give each day a consistent width
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = day, style = MaterialTheme.typography.bodySmall.copy(color = DarkerGray))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly, // Use SpaceEvenly for dates as well
            verticalAlignment = Alignment.CenterVertically
        ) {
            dates.forEach { date ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (date == selectedDate) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onDateClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (date == selectedDate) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
