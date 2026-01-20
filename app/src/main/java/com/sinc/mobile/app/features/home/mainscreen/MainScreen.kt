package com.sinc.mobile.app.features.home.mainscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sinc.mobile.app.features.campos.CamposScreen
import com.sinc.mobile.app.features.historial_movimientos.HistorialMovimientosScreen
import com.sinc.mobile.app.features.home.MainViewModel
import com.sinc.mobile.app.features.home.mainscreen.components.Header
import com.sinc.mobile.app.features.home.mainscreen.components.MyJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.QuickJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.WeekdaySelector
import com.sinc.mobile.app.features.logistics.LogisticsScreen
import com.sinc.mobile.app.features.logistics.components.LogisticsDraggableHandle
import com.sinc.mobile.app.features.movimiento.SeleccionCampoScreen
import com.sinc.mobile.app.features.stock.StockScreen
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes
import com.sinc.mobile.app.ui.components.LoadingOverlay
import com.sinc.mobile.app.ui.components.SlidingPanel
import com.sinc.mobile.app.ui.theme.CozyMediumGray
import java.time.LocalDate
import android.content.Intent
import android.net.Uri

@Composable
fun MainScreen(
    navController: NavHostController,
    startRoute: String = CozyBottomNavRoutes.HOME,
    viewModel: MainViewModel = hiltViewModel()
) {
    var currentRoute by rememberSaveable { mutableStateOf(startRoute) }
    var showLogisticsPanel by rememberSaveable { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.isInitialized,
            label = "loading_transition",
            transitionSpec = {
                if (targetState) {
                    // Initialized (Show Dashboard): Slide in from right
                    (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(500)))
                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(500)))
                } else {
                    // Loading (Show Spinner): Default
                    (slideInHorizontally { width -> -width } + fadeIn(animationSpec = tween(500)))
                        .togetherWith(slideOutHorizontally { width -> width } + fadeOut(animationSpec = tween(500)))
                }
            }
        ) { isInitialized ->
            if (!isInitialized) {
                // Loading State: White Screen with Spinner
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                // Initialized State: Show Dashboard
                Scaffold(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = CozyMediumGray,
                    bottomBar = {
                        CozyBottomNavBar(
                            selectedRoute = currentRoute,
                            onItemSelected = { newRoute ->
                                if (newRoute == CozyBottomNavRoutes.PROFILE) {
                                    navController.navigate(Routes.SETTINGS)
                                } else {
                                    currentRoute = newRoute
                                }
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
                                onBack = { currentRoute = CozyBottomNavRoutes.HOME },
                                onNavigateToVentas = { navController.navigate(Routes.VENTAS) }
                            )
                            CozyBottomNavRoutes.HISTORIAL -> HistorialMovimientosScreen(
                                mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                                onBack = { currentRoute = CozyBottomNavRoutes.HOME },
                                onNavigateToResumen = { month, year ->
                                    navController.navigate(Routes.createResumenMovimientosRoute(month, year))
                                }
                            )
                            CozyBottomNavRoutes.CAMPOS -> CamposScreen(
                                mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                                onNavigateToCreateUnidadProductiva = {
                                    navController.navigate(Routes.CREATE_UNIDAD_PRODUCTIVA)
                                },
                                onNavigateToEditUnidadProductiva = { unidadId ->
                                    navController.navigate(Routes.createEditUnidadProductivaRoute(unidadId))
                                },
                                onBack = { currentRoute = CozyBottomNavRoutes.HOME },
                                navController = navController
                            )
                            CozyBottomNavRoutes.SELECCION_CAMPO -> SeleccionCampoScreen(
                                mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                                navController = navController,
                                onBack = { currentRoute = CozyBottomNavRoutes.HOME }
                            )
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

        if (showLogisticsPanel && (currentRoute == CozyBottomNavRoutes.HOME) && uiState.isInitialized) {
            SlidingPanel(
                showPanel = showLogisticsPanel && (currentRoute == CozyBottomNavRoutes.HOME),
                onDismiss = { showLogisticsPanel = false },
                onFullyOpen = { /* No action needed */ },
                handleContent = {
                    LogisticsDraggableHandle(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 73.dp)
                    )
                },
                panelContent = {
                    LogisticsScreen(
                        onBackPress = { showLogisticsPanel = false },
                        today = today,
                        onNavigateToVentas = {
                            showLogisticsPanel = false
                            navController.navigate(Routes.VENTAS)
                        }
                    )
                }
            )
        }

        // Removed LoadingOverlay("Iniciando...") as it is replaced by the initial white screen state

        if (uiState.appControl?.updateRequired == true) {
            AlertDialog(
                onDismissRequest = { /* No cancelable */ },
                title = { Text("Actualización Requerida") },
                text = { Text(uiState.appControl?.message ?: "Existe una nueva versión de la aplicación que es obligatoria para continuar.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            uiState.appControl?.storeUrl?.let { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }
                    ) {
                        Text("Actualizar")
                    }
                }
            )
        }

        if (uiState.appControl?.maintenanceMode == true) {
            AlertDialog(
                onDismissRequest = { /* No cancelable */ },
                title = { Text("Mantenimiento") },
                text = { Text(uiState.appControl?.message ?: "El sistema se encuentra en mantenimiento. Por favor intente más tarde.") },
                confirmButton = {}
            )
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
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 2.dp)) {
                Header(onSettingsClick = onSettingsClick)
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.LightGray)
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
