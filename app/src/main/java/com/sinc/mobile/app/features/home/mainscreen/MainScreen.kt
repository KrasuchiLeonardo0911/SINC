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

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.home.MainViewModel // Import MainViewModel

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel() // Inject MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState() // Observe uiState

    var currentRoute by remember { mutableStateOf(CozyBottomNavRoutes.HOME) }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = SoftGray,
        bottomBar = {
            CozyBottomNavBar(
                selectedRoute = currentRoute,
                onItemSelected = { newRoute ->
                    // Handle navigation for main sections.
                    // The "add" button might trigger a separate action, not just a screen change.
                    if (newRoute != CozyBottomNavRoutes.ADD) {
                        currentRoute = newRoute
                    } else {
                        // Navigate to the Movimiento screen when "add" is tapped
                        navController.navigate(com.sinc.mobile.app.navigation.Routes.MOVIMIENTO)
                    }
                }
            )
        }
    ) { paddingValues ->
        when (currentRoute) {
            CozyBottomNavRoutes.HOME -> MainContent(paddingValues = paddingValues)
            CozyBottomNavRoutes.STOCK -> StockScreen(
                modifier = Modifier.padding(paddingValues),
                navController = navController
            )
            CozyBottomNavRoutes.HISTORIAL -> HistorialMovimientosScreen(
                onBack = { currentRoute = CozyBottomNavRoutes.HOME }
            )
            CozyBottomNavRoutes.PROFILE -> SettingsScreen(
                onNavigateBack = { currentRoute = CozyBottomNavRoutes.HOME }, // Go back to home
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToChangePassword = {
                    navController.navigate("change_password")
                }
            )
            // Add placeholders for other routes
            else -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(text = "Screen for $currentRoute")
                }
            }
        }
    }
}

@Composable
fun MainContent(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Header()
        Spacer(modifier = Modifier.height(24.dp))
        WeekdaySelector()
        Spacer(modifier = Modifier.height(24.dp))
        MyJournalSection()
        Spacer(modifier = Modifier.height(24.dp))
        QuickJournalSection()
    }
}