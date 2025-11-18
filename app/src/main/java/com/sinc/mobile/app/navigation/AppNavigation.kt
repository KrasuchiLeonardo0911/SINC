package com.sinc.mobile.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sinc.mobile.app.features.changepassword.ChangePasswordScreen
import com.sinc.mobile.app.features.forgotpassword.ForgotPasswordScreen
import com.sinc.mobile.app.features.home.MainScreen
import com.sinc.mobile.app.features.login.LoginScreen
import com.sinc.mobile.app.features.maquetas.CuadernoDeCampoMaquetaScreen
import com.sinc.mobile.app.features.movimiento.MovimientoScreen
import com.sinc.mobile.app.features.settings.SettingsScreen
import com.sinc.mobile.app.features.campos.CamposScreen
import com.sinc.mobile.app.features.createunidadproductiva.CreateUnidadProductivaScreen

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
    const val MAQUETA_CREATE_UP = "maqueta_create_up"
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
        composable(Routes.MOVIMIENTO) {
            MovimientoScreen()
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
                onUnidadProductivaCreated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.CREATE_UNIDAD_PRODUCTIVA) { inclusive = true }
                    }
                },
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
        composable(Routes.MAQUETA_CREATE_UP) {
            com.sinc.mobile.app.features.maquetas.CreateUpMaquetaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
