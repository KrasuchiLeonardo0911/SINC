package com.sinc.mobile.app.features.cuaderno.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DateSelectionStep(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var internalSelectedDate by remember { mutableStateOf(selectedDate) }
    val today = remember { LocalDate.now() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Seleccionar el día",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Seleccione el día que ocurrió el evento",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selectores de Mes y Año (Igual que Agenda)
        CuadernoMonthSelector(
            selectedDate = currentMonth.atDay(1),
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Encabezado de Días (Igual que Agenda)
        CalendarHeader()
        
        Spacer(modifier = Modifier.height(8.dp))

        // Grid de Calendario (Igual que Agenda)
        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7) - (DayOfWeek.SUNDAY.value % 7)
        val startOffset = if (dayOfWeekOffset < 0) dayOfWeekOffset + 7 else dayOfWeekOffset
        
        SimpleCalendarGrid(
            startOffset = startOffset,
            days = (1..currentMonth.lengthOfMonth()).toList(),
            today = today,
            selectedDate = internalSelectedDate,
            currentMonth = currentMonth,
            onDateSelected = { internalSelectedDate = it }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onDateSelected(internalSelectedDate) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Siguiente", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CalendarHeader() {
    val daysOfWeek = listOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB")
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SimpleCalendarGrid(
    startOffset: Int,
    days: List<Int>,
    today: LocalDate,
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    onDateSelected: (LocalDate) -> Unit
) {
    val totalCells = startOffset + days.size
    val rows = if (totalCells % 7 == 0) totalCells / 7 else (totalCells / 7) + 1
    val gridHeight = (rows * 48).dp 

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(gridHeight),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until startOffset) {
            item { Box(modifier = Modifier.size(44.dp)) }
        }

        items(days) { day ->
            val date = currentMonth.atDay(day)
            val isSelected = date == selectedDate
            val isToday = date == today

            val backgroundColor = if (isToday) SincPrimary else Color.Transparent
            val textColor = if (isToday) Color.White else Color(0xFF374151)

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .then(
                        if (isSelected && !isToday) {
                            Modifier.border(BorderStroke(2.dp, SincPrimary), CircleShape)
                        } else if (isSelected) {
                            Modifier.border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.3f)), CircleShape)
                        } else Modifier
                    )
                    .clickable { onDateSelected(date) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    color = textColor,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}
