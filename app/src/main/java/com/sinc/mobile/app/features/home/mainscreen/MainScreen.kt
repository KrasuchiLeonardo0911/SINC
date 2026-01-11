package com.sinc.mobile.app.features.home.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.campos.CamposScreen
import com.sinc.mobile.app.features.historial_movimientos.HistorialMovimientosScreen
import com.sinc.mobile.app.features.home.MainViewModel
import com.sinc.mobile.app.features.home.mainscreen.components.Header
import com.sinc.mobile.app.features.home.mainscreen.components.MyJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.QuickJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.WeekdaySelector
import com.sinc.mobile.app.features.logistics.LogisticsScreen
import com.sinc.mobile.app.features.movimiento.SeleccionCampoScreen
import com.sinc.mobile.app.features.logistics.components.LogisticsDraggableHandle
import com.sinc.mobile.app.features.stock.StockScreen
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes
import com.sinc.mobile.app.ui.theme.*
import com.sinc.mobile.app.ui.components.SlidingPanel


import java.time.LocalDate

@Composable
fun MainScreen(
    navController: NavHostController,
    startRoute: String = CozyBottomNavRoutes.HOME,
    viewModel: MainViewModel = hiltViewModel() // Inject MainViewModel
) {
    var currentRoute by rememberSaveable { mutableStateOf(startRoute) }
    var showLogisticsPanel by rememberSaveable { mutableStateOf(false) }

    val today = remember { LocalDate.now() } // Get current date once

    SlidingPanel(
        showPanel = showLogisticsPanel && (currentRoute == CozyBottomNavRoutes.HOME),
        onDismiss = { showLogisticsPanel = false },
        onFullyOpen = { /* No action needed */ },
        handleContent = {
            LogisticsDraggableHandle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 73.dp) // Adjusted to align with WeekdaySelector
            )
        },
        panelContent = {
            LogisticsScreen(onBackPress = { showLogisticsPanel = false }, today = today)
        }
    ) {
        // This is the main content that will be behind the panel
        Scaffold(
            modifier = Modifier.navigationBarsPadding(),
            containerColor = CozyMediumGray,
            bottomBar = {
                CozyBottomNavBar(
                    selectedRoute = currentRoute,
                    onItemSelected = { newRoute ->
                        currentRoute = newRoute
                    }
                )
            }
        ) { paddingValues ->
            Crossfade(targetState = currentRoute, label = "main_screen_crossfade") { route ->
                when (route) {
                    CozyBottomNavRoutes.HOME -> MainContent(
                        paddingValues = paddingValues,
                        onSettingsClick = { navController.navigate(Routes.CUENCA_INFO) },
                        onDateClick = { showLogisticsPanel = true },
                        today = today,
                        onStockClick = { currentRoute = CozyBottomNavRoutes.STOCK },
                        onAddClick = { currentRoute = CozyBottomNavRoutes.SELECCION_CAMPO },
                        onHistoryClick = { currentRoute = CozyBottomNavRoutes.HISTORIAL },
                        onCamposClick = { currentRoute = CozyBottomNavRoutes.CAMPOS }
                    )
                    CozyBottomNavRoutes.STOCK -> StockScreen(
                        mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                        onBack = { currentRoute = CozyBottomNavRoutes.HOME } // Pass onBack lambda
                    )
                    CozyBottomNavRoutes.HISTORIAL -> HistorialMovimientosScreen(
                        mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                        onBack = { currentRoute = CozyBottomNavRoutes.HOME }
                    )
                    CozyBottomNavRoutes.CAMPOS -> CamposScreen(
                        mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                        onNavigateToCreateUnidadProductiva = {
                            navController.navigate(Routes.CREATE_UNIDAD_PRODUCTIVA)
                        },
                        onBack = { currentRoute = CozyBottomNavRoutes.HOME },
                        navController = navController
                    )
                    CozyBottomNavRoutes.SELECCION_CAMPO -> SeleccionCampoScreen(
                        mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                        navController = navController,
                        onBack = { currentRoute = CozyBottomNavRoutes.HOME }
                    )
                    CozyBottomNavRoutes.PROFILE -> {
                        // Use LaunchedEffect to navigate to settings and then pop back to home
                        // This avoids showing a blank screen and keeps the bottom bar state consistent
                        LaunchedEffect(Unit) {
                            navController.navigate(Routes.SETTINGS)
                            currentRoute = CozyBottomNavRoutes.HOME // Go back to home state after navigating
                        }
                        // Display a blank box to cover the content and prevent clicks during navigation
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(paddingValues)
                        )
                    }
                    CozyBottomNavRoutes.HELP, CozyBottomNavRoutes.NOTIFICATIONS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Pantalla de '$route' en construcción")
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .padding(16.dp)
                        ) {
                            Text(text = "Screen for $route")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(
    paddingValues: PaddingValues,
    onSettingsClick: () -> Unit,
    onDateClick: () -> Unit,
    today: LocalDate,
    onStockClick: () -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onCamposClick: () -> Unit
) {
    // Main Screen Content is now just the UI
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Header(onSettingsClick = onSettingsClick)
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.LightGray)
            Column(modifier = Modifier.padding(16.dp)) {
                WeekdaySelector(
                    onDateClick = onDateClick,
                    today = today
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            MyJournalSection(
                onStockClick = onStockClick,
                onAddClick = onAddClick,
                onHistoryClick = onHistoryClick,
                onCamposClick = onCamposClick
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) { QuickJournalSection() }
    }
}