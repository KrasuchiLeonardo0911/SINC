package com.sinc.mobile.app.features.logistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import com.sinc.mobile.app.ui.components.MinimalHeader
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun LogisticsScreen(
    onBackPress: () -> Unit
) {
    // Mock data for visual representation
    val currentMonth = YearMonth.of(2026, 1)
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    var selectedDate by remember { mutableStateOf(LocalDate.of(2026, 1, 15)) }

    // Calculate offset for the first day of the week (Sunday)
    val firstDayOfWeek = DayOfWeek.SUNDAY
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7) - (firstDayOfWeek.value % 7)
    val startOffset = if (dayOfWeekOffset < 0) dayOfWeekOffset + 7 else dayOfWeekOffset

    val calendarDays = (1..daysInMonth).map { it }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MinimalHeader(
                title = "Ciclo de logistica",
                onBackPress = onBackPress,
                modifier = Modifier.statusBarsPadding() // Ensure it respects the status bar
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ciclo de logistica",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = "Fechas de recogida",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            MonthYearSelectors()

            Spacer(modifier = Modifier.height(24.dp))

            CalendarHeader()
            Spacer(modifier = Modifier.height(8.dp))
            CalendarGrid(
                startOffset = startOffset,
                days = calendarDays,
                selectedDate = selectedDate,
                onDateSelected = { day ->
                    selectedDate = currentMonth.atDay(day)
                }
            )
        }
    }
}

@Composable
private fun MonthYearSelectors() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectorChip(
            text = "Enero",
            modifier = Modifier.weight(1f)
        )
        SelectorChip(
            text = "2026",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SelectorChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(50), // Pill shape
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CalendarHeader() {
    val daysOfWeek = listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    startOffset: Int,
    days: List<Int>,
    selectedDate: LocalDate,
    onDateSelected: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Empty cells for offset
        for (i in 0 until startOffset) {
            item { Box(modifier = Modifier.size(40.dp)) }
        }

        items(days) { day ->
            val isSelected = selectedDate.dayOfMonth == day
            DayCell(
                day = day,
                isSelected = isSelected,
                onClick = { onDateSelected(day) }
            )
        }
    }
}

@Composable
private fun DayCell(day: Int, isSelected: Boolean, onClick: () -> Unit) {
    val cellColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(cellColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}