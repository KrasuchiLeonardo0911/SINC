package com.sinc.mobile.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sinc.mobile.app.features.campos.CamposScreen
import com.sinc.mobile.app.features.changepassword.ChangePasswordScreen
import com.sinc.mobile.app.features.createunidadproductiva.CreateUnidadProductivaScreen
import com.sinc.mobile.app.features.forgotpassword.ForgotPasswordScreen
import com.sinc.mobile.app.features.historial_movimientos.HistorialMovimientosScreen
import com.sinc.mobile.app.features.home.mainscreen.MainScreen
import com.sinc.mobile.app.features.login.LoginScreen
import com.sinc.mobile.app.features.movimiento.MovimientoStepperScreen
import com.sinc.mobile.app.features.cuenca.CuencaInfoScreen
import com.sinc.mobile.app.features.logistics.LogisticsScreen
import com.sinc.mobile.app.features.movimiento.SeleccionCampoScreen
import com.sinc.mobile.app.features.settings.SettingsScreen
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val MOVIMIENTO = "movimiento"
    const val SETTINGS = "settings"
    const val CHANGE_PASSWORD = "change_password"
    const val FORGOT_PASSWORD = "forgot_password"
    const val SPLASH = "splash"
    const val CREATE_UNIDAD_PRODUCTIVA = "create_unidad_productiva"
    const val CAMPOS = "campos"
    const val MOVIMIENTO_FORM = "movimiento_form/{unidadId}"
    fun createMovimientoFormRoute(unidadId: String) = "movimiento_form/$unidadId"
    const val HISTORIAL_MOVIMIENTOS = "historial_movimientos"
    const val LOGISTICS = "logistics"
    const val CUENCA_INFO = "cuenca_info"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            // This is a temporary destination, the real navigation happens in MainActivity's LaunchedEffect
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        composable(
            route = Routes.HOME + "?startRoute={startRoute}",
            arguments = listOf(navArgument("startRoute") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val startRoute = backStackEntry.arguments?.getString("startRoute")
            MainScreen(
                navController = navController,
                startRoute = startRoute ?: CozyBottomNavRoutes.HOME
            )
        }
        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true } // Pop up to home to clear backstack
                    }
                },
                onNavigateToChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) }
            )
        }
        composable(
            route = Routes.CUENCA_INFO,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            CuencaInfoScreen(navController = navController)
        }
        composable(
            route = Routes.CHANGE_PASSWORD,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true } // Pop up to home to clear backstack
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }
        composable(
            route = Routes.FORGOT_PASSWORD,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Routes.CREATE_UNIDAD_PRODUCTIVA,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            CreateUnidadProductivaScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccessfulCreation = {
                    // Set a result on the previous screen's SavedStateHandle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_ups", true)
                    navController.popBackStack()
                }
            )
        }


        composable(
            route = Routes.MOVIMIENTO_FORM,
            arguments = listOf(navArgument("unidadId") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            MovimientoStepperScreen(
                onBackPress = { navController.popBackStack() },
                navController = navController
            )
        }
    }
}