package com.sinc.mobile.app.features.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sinc.mobile.app.features.movimiento.MovimientoScreen
import com.sinc.mobile.app.ui.components.Sidebar
import com.sinc.mobile.app.ui.components.TopBar
import kotlinx.coroutines.launch

object MainScreenRoutes {
    const val DASHBOARD = "main/dashboard"
    const val MOVIMIENTO = "main/movimiento"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val innerNavController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                navController = innerNavController,
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
            }
        ) { paddingValues ->
            NavHost(
                navController = innerNavController,
                startDestination = MainScreenRoutes.DASHBOARD,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(MainScreenRoutes.DASHBOARD) {
                    DashboardScreen()
                }
                composable(MainScreenRoutes.MOVIMIENTO) {
                    MovimientoScreen()
                }
            }
        }
    }
}