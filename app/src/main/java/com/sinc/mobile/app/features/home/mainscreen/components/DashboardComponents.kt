package com.sinc.mobile.app.features.home.mainscreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.sp

enum class DashboardTab(val label: String) {
    STOCK("Stock"),
    MOVIMIENTOS("Movimientos"),
    LOGISTICA("Logística"),
    CLIMA("Clima")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OperationsSummarySection() {
    val pagerState = rememberPagerState(pageCount = { DashboardTab.values().size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 32.dp),
        ) { page ->
            val tab = DashboardTab.values()[page]
            DashboardCard {
                when (tab) {
                    DashboardTab.STOCK -> StockSummary()
                    DashboardTab.MOVIMIENTOS -> MovementsSummary()
                    DashboardTab.LOGISTICA -> LogisticsSummary()
                    DashboardTab.CLIMA -> WeatherSummary()
                }
            }
        }

        // Dot Indicator
        PagerIndicator(
            pagerState = pagerState,
            pageCount = DashboardTab.values().size
        )
    }
}

@Composable
fun DashboardCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // Defined height for the cards
            .padding(horizontal = 8.dp) // Space between cards
    ) {
        content()
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicator(pagerState: PagerState, pageCount: Int) {
    Row(
        Modifier
            .height(20.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { iteration ->
            val color by animateColorAsState(
                targetValue = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray,
                animationSpec = tween(300), label = "indicator_color"
            )
            val width by animateDpAsState(
                targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp,
                animationSpec = tween(300), label = "indicator_width"
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(width = width, height = 8.dp)
            )
        }
    }
}


// --- Vistas de Resumen ---

@Composable
fun StockSummary() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gráfico Torta Simple (Mock)
            Box(
                modifier = Modifier.weight(1f), // Make it flexible
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) { // Slightly smaller graph
                    // Ovinos (60%)
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 216f,
                        useCenter = false,
                        style = Stroke(width = 30f)
                    )
                    // Caprinos (40%)
                    drawArc(
                        color = secondaryColor,
                        startAngle = 126f,
                        sweepAngle = 144f,
                        useCenter = false,
                        style = Stroke(width = 30f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                    Text("1,250", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KpiItem("Ovinos", "850", primaryColor)
                KpiItem("Caprinos", "400", secondaryColor)
            }
        }
    }
}

@Composable
fun KpiItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.Circle, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MovementsSummary() {
    val color = MaterialTheme.colorScheme.tertiary
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Resumen Enero", fontWeight = FontWeight.Bold, color = color)
                Text("Ver Historial", color = color, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Items
            TimelineItem(date = "Hoy", title = "Venta Lote #4", type = "Baja", amount = "-15")
            TimelineItem(date = "18 Ene", title = "Nacimientos", type = "Alta", amount = "+32")
            TimelineItem(date = "15 Ene", title = "Compra Reproductores", type = "Alta", amount = "+5")
        }
    }
}

@Composable
fun TimelineItem(date: String, title: String, type: String, amount: String) {
    val altaColor = Color(0xFF4CAF50)
    val bajaColor = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(50.dp)) {
            Text(date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (type == "Alta") altaColor else bajaColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text(
            amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (type == "Alta") altaColor else bajaColor
        )
    }
}

@Composable
fun LogisticsSummary() {
    val color = Color(0xFF1976D2) // Strong Blue
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), // Soft Blue
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Próxima Recolección", style = MaterialTheme.typography.labelMedium, color = color)
            Text("Jueves, 25 Ene", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Stock Pendiente: 45 animales", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun WeatherSummary() {
    val color = Color(0xFFEF6C00) // Strong Orange
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), // Soft Orange
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = color
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Acumulado Mensual", style = MaterialTheme.typography.labelMedium, color = color)
                    Text("45 mm", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
                }
            }

            // Heatmap Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFFE57373))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Mapa de Calor (Próximamente)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}