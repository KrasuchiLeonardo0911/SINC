package com.sinc.mobile.app.features.agenda

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.agenda.components.AgendaItemCard
import com.sinc.mobile.app.features.agenda.components.AddEditAgendaSheet
import com.sinc.mobile.app.features.agenda.components.AgendaDetailSheet
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    onBackPress: () -> Unit,
    today: LocalDate,
    nextTruckDate: LocalDate? = null,
    orderDeadline: LocalDate? = null,
    onNavigateToVentas: () -> Unit,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado del Calendario
    var currentMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }
    
    // Estado de los Modales
    var showAddEditSheet by remember { mutableStateOf(false) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<AgendaItem?>(null) }
    var selectedItemForDetail by remember { mutableStateOf<AgendaItem?>(null) }

    val sheetState = rememberModalBottomSheetState()

    // Actividades del MES actual
    val monthActivities = remember(currentMonth, uiState.items, nextTruckDate, orderDeadline) {
        val agendaItems = uiState.items.filter { 
            it.fechaProgramada.year == currentMonth.year && it.fechaProgramada.month == currentMonth.month 
        }
        
        val logisticsEvents = mutableListOf<CombinedActivity>()
        nextTruckDate?.let { 
            if (YearMonth.from(it) == currentMonth) {
                logisticsEvents.add(CombinedActivity.Logistics(it, "Recogida de animales", Color(0xFF4CAF50), Icons.Default.LocalShipping))
            }
        }
        orderDeadline?.let { 
            if (YearMonth.from(it) == currentMonth) {
                logisticsEvents.add(CombinedActivity.Logistics(it, "Cierre de inscripciones", Color(0xFFFF9800), Icons.Default.Timer, onNavigateToVentas))
            }
        }

        (agendaItems.map { CombinedActivity.Agenda(it) } + logisticsEvents)
            .sortedBy { it.date }
    }

    if (uiState.isSuccess) {
        LaunchedEffect(Unit) {
            showAddEditSheet = false
            itemToEdit = null
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        containerColor = SincBackground,
        topBar = {
            MinimalHeader(
                title = "Calendario",
                onBackPress = onBackPress
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    itemToEdit = null
                    showAddEditSheet = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("Agendar") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MonthYearSelectors(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                CalendarHeaderComp(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                
                val firstDayOfMonth = currentMonth.atDay(1)
                val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7) - (DayOfWeek.SUNDAY.value % 7)
                val startOffset = if (dayOfWeekOffset < 0) dayOfWeekOffset + 7 else dayOfWeekOffset
                
                CalendarGrid(
                    startOffset = startOffset,
                    days = (1..currentMonth.lengthOfMonth()).toList(),
                    today = today,
                    selectedDate = selectedDate,
                    currentMonth = currentMonth,
                    nextTruckDate = nextTruckDate,
                    orderDeadline = orderDeadline,
                    items = uiState.items,
                    onDateSelected = { 
                        selectedDate = it 
                        if (!it.isBefore(today)) {
                            itemToEdit = null
                            showAddEditSheet = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Actividades de ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (monthActivities.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin actividades programadas para este mes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(monthActivities) { activity ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        when (activity) {
                            is CombinedActivity.Agenda -> {
                                AgendaItemCard(
                                    item = activity.item,
                                    onClick = {
                                        selectedItemForDetail = activity.item
                                        showDetailSheet = true
                                    }
                                )
                            }
                            is CombinedActivity.Logistics -> {
                                LogisticsEventRow(
                                    title = activity.title,
                                    date = activity.date,
                                    color = activity.color,
                                    icon = activity.icon,
                                    onClick = activity.onClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAddEditSheet = false
                itemToEdit = null
            },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AddEditAgendaSheet(
                initialDate = selectedDate,
                itemToEdit = itemToEdit,
                onDismiss = { 
                    showAddEditSheet = false
                    itemToEdit = null
                },
                onSave = { viewModel.onEvent(AgendaEvent.SaveItem(it)) }
            )
        }
    }

    if (showDetailSheet && selectedItemForDetail != null) {
        AgendaDetailSheet(
            item = selectedItemForDetail!!,
            onToggleStatus = { viewModel.onEvent(AgendaEvent.ToggleStatus(selectedItemForDetail!!.id, !selectedItemForDetail!!.isCompleted)) },
            onDelete = { viewModel.onEvent(AgendaEvent.DeleteItem(selectedItemForDetail!!.id)) },
            onDismiss = { 
                showDetailSheet = false
                selectedItemForDetail = null
            }
        )
    }
}

sealed class CombinedActivity {
    abstract val date: LocalDate
    data class Agenda(val item: AgendaItem) : CombinedActivity() {
        override val date: LocalDate = item.fechaProgramada.toLocalDate()
    }
    data class Logistics(
        override val date: LocalDate,
        val title: String,
        val color: Color,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val onClick: (() -> Unit)? = null
    ) : CombinedActivity()
}

@Composable
private fun LogisticsEventRow(
    title: String,
    date: LocalDate,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale("es", "ES"))).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
private fun MonthYearSelectors(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = SincPrimary)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
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

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SincPrimary)
        }
    }
}

@Composable
private fun SelectorChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(50), 
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
                tint = SincPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CalendarHeaderComp(modifier: Modifier = Modifier) {
    val daysOfWeek = listOf("DOM", "LUN", "MAR", "MIÃ‰", "JUE", "VIE", "SÃB")
    Row(modifier = modifier.fillMaxWidth()) {
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
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    nextTruckDate: LocalDate?,
    orderDeadline: LocalDate?,
    items: List<AgendaItem>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCells = startOffset + days.size
    val rows = if (totalCells % 7 == 0) totalCells / 7 else (totalCells / 7) + 1
    val gridHeight = (rows * 48).dp 

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.height(gridHeight),
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
            val isVisita = date == nextTruckDate
            val isCierre = date == orderDeadline
            val dayItems = items.filter { it.fechaProgramada.toLocalDate() == date }

            val backgroundColor = when {
                isToday -> SincPrimary
                isVisita -> Color(0xFF4CAF50)
                isCierre -> Color(0xFFFF9800)
                else -> Color.Transparent
            }
            
            val textColor = when {
                isToday || isVisita || isCierre -> Color.White
                else -> Color(0xFF374151)
            }

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .then(
                        if (isSelected && !isToday && !isVisita && !isCierre) {
                            Modifier.border(BorderStroke(2.dp, SincPrimary), CircleShape)
                        } else if (isSelected) {
                            Modifier.border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.3f)), CircleShape)
                        } else Modifier
                    )
                    .clickable { onDateSelected(date) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.toString(),
                        color = textColor,
                        fontWeight = if (isToday || isVisita || isCierre || isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                    
                    if (dayItems.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            dayItems.take(3).forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (backgroundColor != Color.Transparent) Color.White else getTipoColor(item.tipo))
                                )
                            }
                        }
                    }
                }
            }
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
