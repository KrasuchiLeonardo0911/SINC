package com.sinc.mobile.app.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sinc.mobile.app.features.movimiento.MovimientoScreen
import com.sinc.mobile.app.ui.components.BottomNavBar
import com.sinc.mobile.app.ui.components.GlobalBanner
import com.sinc.mobile.app.ui.components.Sidebar
import com.sinc.mobile.app.ui.components.TopBar
import kotlinx.coroutines.launch

object MainScreenRoutes {
    const val DASHBOARD = "main/dashboard"
    const val MOVIMIENTO = "main/movimiento"
    const val NOTIFICATIONS = "main/notifications"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(MainScreenRoutes.DASHBOARD) }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Sidebar(
                    currentRoute = currentScreen,
                    onNavigate = { route ->
                        currentScreen = route
                    },
                    onCloseDrawer = {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopBar(
                        onNavigationIconClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onConfigurationIconClick = {
                            navController.navigate(com.sinc.mobile.app.navigation.Routes.SETTINGS)
                        }
                    )
                },
                bottomBar = {
                    BottomNavBar(
                        currentRoute = currentScreen,
                        onNavigate = { route ->
                            currentScreen = route
                        }
                    )
                }
            ) { paddingValues ->
                val modifier = Modifier.padding(paddingValues)
                when (currentScreen) {
                    MainScreenRoutes.DASHBOARD -> DashboardScreen(
                        modifier = modifier,
                        onNavigateToMaqueta = {
                            navController.navigate(com.sinc.mobile.app.navigation.Routes.MAQUETA_CUADERNO)
                        }
                    )
                    MainScreenRoutes.MOVIMIENTO -> MovimientoScreen(modifier = modifier)
                    MainScreenRoutes.NOTIFICATIONS -> NotificationsScreen(modifier = modifier)
                }
            }
        }

        GlobalBanner()
    }
}