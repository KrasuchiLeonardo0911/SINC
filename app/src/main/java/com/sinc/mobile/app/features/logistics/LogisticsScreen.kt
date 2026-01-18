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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincPrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun LogisticsScreen(
    onBackPress: () -> Unit,
    today: LocalDate,
    onNavigateToVentas: () -> Unit,
    viewModel: LogisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    // Current month and year based on today
    val currentMonth = YearMonth.of(today.year, today.month)
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()

    // Calculate offset for the first day of the week (Sunday)
    val firstDayOfWeek = DayOfWeek.SUNDAY
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7) - (firstDayOfWeek.value % 7)
    val startOffset = if (dayOfWeekOffset < 0) dayOfWeekOffset + 7 else dayOfWeekOffset

    val calendarDays = (1..daysInMonth).map { it }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MinimalHeader(
                onBackPress = onBackPress,
                modifier = Modifier.statusBarsPadding(),
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Ayuda",
                            tint = SincPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Título
            Text(
                text = "Ciclo de logística",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1F2937)
            )
            Text(
                text = "Fechas de recogida",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            MonthYearSelectors(currentMonth = currentMonth)

            Spacer(modifier = Modifier.height(24.dp))

            CalendarHeader()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Grid de Calendario con datos reales
            CalendarGrid(
                startOffset = startOffset,
                days = calendarDays,
                today = today,
                nextTruckDate = uiState.logisticsInfo?.proximaVisita
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Leyenda e Información
            if (uiState.logisticsInfo != null) {
                LogisticsLegend(
                    daysRemaining = uiState.daysRemaining,
                    frequencyDays = uiState.logisticsInfo?.frecuenciaDias
                )
            } else if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SincPrimary)
                }
            }
        }
    }

    if (showHelpDialog) {
        LogisticsHelpDialog(
            onDismiss = { showHelpDialog = false },
            onNavigateToVentas = {
                showHelpDialog = false
                onNavigateToVentas()
            }
        )
    }
}

@Composable
private fun LogisticsHelpDialog(
    onDismiss: () -> Unit,
    onNavigateToVentas: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Información de Logística", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            val text = buildAnnotatedString {
                append("El camión habilitado realiza recorridos periódicos para la recolección de animales.\n\n")
                append("Si desea vender parte de su stock, puede marcarlo en la pantalla ")
                
                pushStringAnnotation(tag = "VENTAS", annotation = "ventas")
                withStyle(style = SpanStyle(color = SincPrimary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                    append("Vender Stock")
                }
                pop() 
                
                append(".\n\nEsto nos permite planificar la ruta, marcar su ubicación y asegurar la visita a su establecimiento.")
            }

            ClickableText(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF374151)),
                onClick = { offset ->
                    text.getStringAnnotations(tag = "VENTAS", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onNavigateToVentas()
                        }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = SincPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LogisticsLegend(
    daysRemaining: Long?,
    frequencyDays: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Puntos de referencia
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF4CAF50))) // Verde
            Spacer(modifier = Modifier.width(8.dp))
            Text("Próxima pasada", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF374151))
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SincPrimary)) // Bordó
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fecha actual", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF374151))
        }

        HorizontalDivider(color = Color(0xFFE5E7EB))

        // Información de días
        if (daysRemaining != null && daysRemaining >= 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Faltan ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF374151)
                )
                Text(
                    text = "$daysRemaining días",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SincPrimary
                )
                Text(
                    text = " para la próxima visita.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF374151)
                )
            }
        }

        if (frequencyDays != null) {
            Text(
                text = "La frecuencia del recorrido se estableció a $frequencyDays días.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun MonthYearSelectors(currentMonth: YearMonth) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectorChip(
            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() },
            modifier = Modifier.weight(1f)
        )
        SelectorChip(
            text = currentMonth.year.toString(),
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
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = SincPrimary
            )
        }
    }
}

@Composable
private fun CalendarHeader() {
    val daysOfWeek = listOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
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
private fun CalendarGrid(
    startOffset: Int,
    days: List<Int>,
    today: LocalDate,
    nextTruckDate: LocalDate?
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
            // Check if this day is Today
            val isToday = (day == today.dayOfMonth && today.month == YearMonth.of(today.year, today.month).month && today.year == YearMonth.of(today.year, today.month).year)
            
            // Check if this day is the Next Truck Date
            val isNextTruck = nextTruckDate != null && 
                              day == nextTruckDate.dayOfMonth && 
                              nextTruckDate.month == YearMonth.of(today.year, today.month).month && 
                              nextTruckDate.year == YearMonth.of(today.year, today.month).year

            DayCell(
                day = day,
                isToday = isToday,
                isNextTruck = isNextTruck
            )
        }
    }
}

@Composable
private fun DayCell(day: Int, isToday: Boolean, isNextTruck: Boolean) {
    val backgroundColor = when {
        isToday -> SincPrimary
        isNextTruck -> Color(0xFF4CAF50) // Verde
        else -> Color.Transparent
    }
    
    val textColor = when {
        isToday || isNextTruck -> Color.White
        else -> Color(0xFF374151)
    }

    val fontWeight = if (isToday || isNextTruck) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = textColor,
            fontWeight = fontWeight,
            fontSize = 14.sp
        )
    }
}
