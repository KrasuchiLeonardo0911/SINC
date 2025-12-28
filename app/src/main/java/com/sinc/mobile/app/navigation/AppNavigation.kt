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
import com.sinc.mobile.app.features.home.mainscreen.MainScreen
import com.sinc.mobile.app.features.login.LoginScreen
import com.sinc.mobile.app.features.movimiento.MovimientoFormScreen
import com.sinc.mobile.app.features.movimiento.SeleccionCampoScreen
import com.sinc.mobile.app.features.settings.SettingsScreen

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
        composable(Routes.HOME) {
            MainScreen(navController = navController)
        }
        composable(
            route = Routes.MOVIMIENTO,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -300 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            }
        ) {
            SeleccionCampoScreen(navController = navController)
        }
        composable(Routes.SETTINGS) {
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
        composable(Routes.CHANGE_PASSWORD) {
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
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CREATE_UNIDAD_PRODUCTIVA) {
            CreateUnidadProductivaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CAMPOS) {
            CamposScreen(
                onNavigateToCreateUnidadProductiva = {
                    navController.navigate(Routes.CREATE_UNIDAD_PRODUCTIVA)
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
        ) { backStackEntry ->
            val unidadId = backStackEntry.arguments?.getString("unidadId")
            // A null check is good practice, though the route requires the argument.
            if (unidadId != null) {
                MovimientoFormScreen(
                    navController = navController,
                    unidadId = unidadId
                )
            }
        }
    }
}