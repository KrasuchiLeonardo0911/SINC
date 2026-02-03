package com.sinc.mobile.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sinc.mobile.app.features.campos.CamposScreen
import com.sinc.mobile.app.features.campos.EditUnidadProductivaScreen
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

import com.sinc.mobile.app.features.ventas.VentasScreen
import com.sinc.mobile.app.features.ventas.HistorialVentasScreen
import com.sinc.mobile.app.features.tickets.TicketsListScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val MOVIMIENTO = "movimiento"
    const val SETTINGS = "settings"
    const val CHANGE_PASSWORD = "change_password"
    const val FORGOT_PASSWORD = "forgot_password"
    const val SPLASH = "splash"
    const val CREATE_UNIDAD_PRODUCTIVA = "create_unidad_productiva"
    const val EDIT_UNIDAD_PRODUCTIVA = "edit_unidad_productiva/{unidadId}"
    fun createEditUnidadProductivaRoute(unidadId: Int) = "edit_unidad_productiva/$unidadId"
    const val CAMPOS = "campos"
    const val MOVIMIENTO_FORM = "movimiento_form?unidadId={unidadId}&initialPage={initialPage}"
    fun createMovimientoFormRoute(unidadId: String?, initialPage: Int = 0): String {
        val route = "movimiento_form?"
        val id_param = unidadId?.let { "unidadId=$it" } ?: ""
        val page_param = "initialPage=$initialPage"

        return if (id_param.isNotEmpty()) {
            "$route$id_param&$page_param"
        } else {
            "$route$page_param"
        }
    }
    const val HISTORIAL_MOVIMIENTOS = "historial_movimientos"
    const val LOGISTICS = "logistics"
    const val CUENCA_INFO = "cuenca_info"
    const val VENTAS = "ventas"
    const val VENTAS_HISTORIAL = "ventas_historial"
    const val RESUMEN_MOVIMIENTOS = "resumen_movimientos/{month}/{year}"
    fun createResumenMovimientosRoute(month: Int, year: Int) = "resumen_movimientos/$month/$year"
    const val HELP = "help"

    // Ticket Routes
    const val TICKETS_LIST = "tickets_list"
    const val CREATE_TICKET_TYPE = "create_ticket_type"
    const val CREATE_TICKET_MESSAGE = "create_ticket_message/{ticketType}"
    fun createTicketMessageRoute(ticketType: String) = "create_ticket_message/$ticketType"
    const val TICKET_CONVERSATION = "ticket_conversation/{ticketId}"
    fun createTicketConversationRoute(ticketId: Long) = "ticket_conversation/$ticketId"
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
            }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            }
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
                onNavigateToChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onNavigateToHelp = { navController.navigate(Routes.HELP) }
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
            route = Routes.EDIT_UNIDAD_PRODUCTIVA,
            arguments = listOf(navArgument("unidadId") { type = NavType.IntType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val unidadId = backStackEntry.arguments?.getInt("unidadId") ?: 0
            EditUnidadProductivaScreen(
                onNavigateBack = { navController.popBackStack() },
                unidadId = unidadId
            )
        }


        composable(
            route = Routes.MOVIMIENTO_FORM,
            arguments = listOf(
                navArgument("unidadId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("initialPage") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            ),
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
            val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0
            MovimientoStepperScreen(
                onBackPress = { navController.popBackStack() },
                initialPage = initialPage
            )
        }

        composable(
            route = Routes.VENTAS,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            VentasScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistorial = { navController.navigate(Routes.VENTAS_HISTORIAL) }
            )
        }

        composable(
            route = Routes.VENTAS_HISTORIAL,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            HistorialVentasScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.RESUMEN_MOVIMIENTOS,
            arguments = listOf(
                navArgument("month") { type = NavType.IntType },
                navArgument("year") { type = NavType.IntType }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            // Need to create this screen first, but I'll add the import later or rely on auto-import when I create the file
            com.sinc.mobile.app.features.historial_movimientos.resumen.ResumenMovimientosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.HELP,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            com.sinc.mobile.app.features.help.HelpScreen(
                onBackPress = { navController.popBackStack() },
                onNavigateToTickets = { navController.navigate(Routes.TICKETS_LIST) }
            )
        }

        // --- Ticket Routes ---
        composable(
            route = Routes.TICKETS_LIST,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
        ) {
            TicketsListScreen(
                navController = navController,
                onTicketClick = { ticketId -> navController.navigate(Routes.createTicketConversationRoute(ticketId)) },
                onNewTicketClick = { navController.navigate(Routes.CREATE_TICKET_TYPE) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CREATE_TICKET_TYPE,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
        ) {
            com.sinc.mobile.app.features.tickets.CreateTicketTypeScreen(
                onNavigateBack = { navController.popBackStack() },
                onTypeSelected = { ticketType -> navController.navigate(Routes.createTicketMessageRoute(ticketType)) }
            )
        }

        composable(
            route = Routes.CREATE_TICKET_MESSAGE,
            arguments = listOf(navArgument("ticketType") { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
        ) { backStackEntry ->
            val ticketType = backStackEntry.arguments?.getString("ticketType") ?: "general"

            com.sinc.mobile.app.features.tickets.CreateTicketMessageScreen(
                ticketType = ticketType,
                onNavigateBack = { navController.popBackStack() },
                onTicketCreated = {
                    // Set the result on the TicketsListScreen's entry
                    navController.getBackStackEntry(Routes.TICKETS_LIST)
                        .savedStateHandle
                        .set("snackbar_message", "Consulta enviada con éxito.")

                    // Pop backstack up to the list screen
                    navController.popBackStack(Routes.TICKETS_LIST, inclusive = false)
                }
            )
        }

        composable(
            route = Routes.TICKET_CONVERSATION,
            arguments = listOf(navArgument("ticketId") { type = NavType.LongType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
        ) {
            com.sinc.mobile.app.features.tickets.TicketConversationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}