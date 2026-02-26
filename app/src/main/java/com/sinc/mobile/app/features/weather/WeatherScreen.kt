package com.sinc.mobile.app.features.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.sinc.mobile.ui.theme.SincPrimary
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
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // SECCIÓN ALERTAS (1/3 o Mitad de pantalla aprox)
                Box(modifier = Modifier.weight(0.45f)) {
                    if (uiState.alerts.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Alertas Meteorológicas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(uiState.alerts) { alert ->
                                    WeatherAlertCard(alert = alert)
                                }
                            }
                        }
                    } else {
                        // Estado Despejado / Sin Alertas Mejorado
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC5E1A5))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Todo en orden",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hay alertas activas en tu zona para las próximas horas. El cielo está despejado.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFF558B2F),
                                        lineHeight = 24.sp
                                    )
                                }
                                
                                // Ilustración de sol a la derecha
                                Image(
                                    painter = painterResource(id = R.drawable.img_weather_sunny),
                                    contentDescription = "Cielo despejado",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .padding(start = 12.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                // FRANJA GRIS DE SEPARACIÓN (Espaciador visual)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(Color(0xFFF5F5F7)) // Gris muy claro
                )

                // SECCIÓN MAPA (Resto de la pantalla)
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Mapa en Tiempo Real",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Preview limpia
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Que el mapa tome el espacio sobrante
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.LightGray)
                            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .clickable { viewModel.onLayerSelected(uiState.selectedLayer) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.windy_preview),
                            contentDescription = "Ver mapa meteorológico",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones de capas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WindyLayer.values().forEach { layer ->
                            Button(
                                onClick = { viewModel.onLayerSelected(layer) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.selectedLayer == layer) SincPrimary else Color(0xFFF5F5F5),
                                    contentColor = if (uiState.selectedLayer == layer) Color.White else Color.DarkGray
                                )
                            ) {
                                Text(layer.label, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = SincPrimary
            )
        }
    }
}
