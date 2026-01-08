package com.sinc.mobile.app.features.home.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import com.sinc.mobile.app.features.logistics.components.LogisticsDraggableHandle
import com.sinc.mobile.app.features.stock.StockScreen
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes
import com.sinc.mobile.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.sinc.mobile.app.ui.components.SlidingPanel
import kotlinx.coroutines.delay


@Composable
fun MainScreen(
    navController: NavHostController,
    startRoute: String = CozyBottomNavRoutes.HOME,
    viewModel: MainViewModel = hiltViewModel() // Inject MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState() // Observe uiState

    var currentRoute by rememberSaveable { mutableStateOf(startRoute) }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = CozyMediumGray,
        bottomBar = {
            CozyBottomNavBar(
                selectedRoute = currentRoute,
                onItemSelected = { newRoute ->
                    if (newRoute != CozyBottomNavRoutes.ADD) {
                        currentRoute = newRoute
                    } else {
                        navController.navigate(com.sinc.mobile.app.navigation.Routes.MOVIMIENTO)
                    }
                }
            )
        }
    ) { paddingValues ->
        Crossfade(targetState = currentRoute, label = "main_screen_crossfade") { route ->
            when (route) {
                CozyBottomNavRoutes.HOME -> MainContent(
                    paddingValues = paddingValues,
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
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
                    onBack = { currentRoute = CozyBottomNavRoutes.HOME }
                )
                // Add placeholders for other routes
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

@Composable
fun MainContent(
    paddingValues: PaddingValues,
    onSettingsClick: () -> Unit
) {
    var showLogisticsPanel by rememberSaveable { mutableStateOf(false) }

    SlidingPanel(
        showPanel = showLogisticsPanel,
        onDismiss = { showLogisticsPanel = false },
        onFullyOpen = {
            // No action needed on fully open for now, as content is pre-rendered.
        },
        handleContent = {
            LogisticsDraggableHandle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 90.dp) // Adjusted to align with WeekdaySelector
            )
        },
        panelContent = {
            LogisticsScreen(onBackPress = { showLogisticsPanel = false })
        }
    ) {
        // Main Screen Content
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
                        onDateClick = {
                            if (!showLogisticsPanel) {
                                showLogisticsPanel = true
                            }
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) { MyJournalSection() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) { QuickJournalSection() }
        }
    }
}