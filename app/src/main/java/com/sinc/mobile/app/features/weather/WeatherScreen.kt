package com.sinc.mobile.app.features.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.R
import com.sinc.mobile.app.features.weather.components.WeatherAlertCard
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.FullscreenLoader
import com.sinc.mobile.ui.theme.SincBackground
import com.sinc.mobile.ui.theme.SincTextPrimary
import com.sinc.mobile.ui.theme.SincTextSecondary
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeatherScreen(
    navController: NavController,
    onBackPress: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.refreshAlerts() }
    )

    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is WeatherEvent.NavigateToMap -> {
                    navController.navigate(Routes.createWindyMapRoute(event.layer))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Clima",
                onBackPress = onBackPress,
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = SincBackground
    ) { paddingValues ->

        if (uiState.isInitialLoad) {
            FullscreenLoader(
                message = "Cargando clima...",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // SECCIÓN ALERTAS (Tarjetas Arriba)
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Alertas Meteorológicas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SincTextPrimary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Outlined.NotificationsActive, null, tint = SincTextSecondary, modifier = Modifier.size(18.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.alerts.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    uiState.alerts.forEach { alert ->
                                        WeatherAlertCard(alert = alert)
                                    }
                                }
                            } else {
                                // ESTADO SIN ALERTAS
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2F4F7))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = CircleShape,
                                            color = Color(0xFFFEFCE8)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.WbSunny,
                                                contentDescription = null,
                                                tint = Color(0xFFEAB308),
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(20.dp))
                                        Column {
                                            Text(
                                                text = "Cielo despejado",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = SincTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "No hay alertas activas en este momento.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SincTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN MAPA INTERACTIVO (Mapa Abajo) - Versión Compacta Rectangular
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mapa en Tiempo Real",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SincTextPrimary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Outlined.Language, null, tint = SincTextSecondary, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clickable { viewModel.onLayerSelected(uiState.selectedLayer) },
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.windy_preview),
                                        contentDescription = "Mapa en vivo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                                    startY = 100f
                                                )
                                            )
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.ZoomOutMap, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ver Mapa", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Selector de Capas
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WindyLayer.values().forEach { layer ->
                                    val isSelected = uiState.selectedLayer == layer
                                    Surface(
                                        modifier = Modifier.clickable { viewModel.onLayerSelected(layer) },
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                        shape = CircleShape,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFF2F4F7))
                                    ) {
                                        Text(
                                            text = layer.label,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) Color.White else SincTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // FOOTER
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Outlined.Info, null, tint = SincTextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Reportes basados en datos oficiales del sistema central y Windy.com.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SincTextSecondary
                                )
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = uiState.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
