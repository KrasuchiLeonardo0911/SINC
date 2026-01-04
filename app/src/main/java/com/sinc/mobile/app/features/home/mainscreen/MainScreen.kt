package com.sinc.mobile.app.features.home.mainscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sinc.mobile.app.features.home.mainscreen.components.Header
import com.sinc.mobile.app.features.home.mainscreen.components.MyJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.QuickJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.WeekdaySelector
import com.sinc.mobile.app.features.settings.SettingsScreen
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
        containerColor = SoftGray,
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
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToMaqueta = { navController.navigate(Routes.MOVIMIENTO_FORM_MAQUETA) }
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
    onSettingsClick: () -> Unit,
    onNavigateToMaqueta: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Header(onSettingsClick = onSettingsClick)
        Spacer(modifier = Modifier.height(24.dp))
        WeekdaySelector()
        Spacer(modifier = Modifier.height(24.dp))
        MyJournalSection()
        Spacer(modifier = Modifier.height(24.dp))
        QuickJournalSection()
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onNavigateToMaqueta) {
            Text("Ir a Maqueta")
        }
    }
}