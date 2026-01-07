package com.sinc.mobile.app.features.home.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sinc.mobile.app.features.home.mainscreen.components.Header
import com.sinc.mobile.app.features.home.mainscreen.components.MyJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.QuickJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.WeekdaySelector
import com.sinc.mobile.app.features.stock.StockScreen
import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes
import com.sinc.mobile.app.ui.theme.*
import com.sinc.mobile.app.features.historial_movimientos.HistorialMovimientosScreen
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.campos.CamposScreen
import com.sinc.mobile.app.features.home.MainViewModel
import com.sinc.mobile.app.navigation.Routes
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement


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
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Combined Header and Weekday Selector Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Header content
            Column(modifier = Modifier.padding(16.dp)) {
                Header(onSettingsClick = onSettingsClick)
            }

            // Divider
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.LightGray)

            // Weekday Selector content
            Column(modifier = Modifier.padding(16.dp)) {
                WeekdaySelector()
            }
        }

        // MyJournal Section (Mis Campos)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            MyJournalSection()
        }

        // QuickJournal Section (Resumen)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            QuickJournalSection()
        }
    }
}