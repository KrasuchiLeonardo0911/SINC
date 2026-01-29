package com.sinc.mobile.app.features.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sinc.mobile.domain.model.ticket.Message
import java.time.format.DateTimeFormatter

@Composable
fun MessageItem(
    message: Message,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (message.isFromUser) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    val textColor = if (message.isFromUser) Color.White else Color.Black

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isFromUser) 0.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.message,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${message.userName} • ${message.createdAt.format(DateTimeFormatter.ofPattern("HH:mm"))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
