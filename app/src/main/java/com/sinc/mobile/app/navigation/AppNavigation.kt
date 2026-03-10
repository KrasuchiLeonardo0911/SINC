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
import androidx.compose.ui.unit.dp
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

import com.sinc.mobile.app.features.weather.WindyMapScreen
import com.sinc.mobile.app.features.weather.WeatherScreen
import com.sinc.mobile.app.features.ventas.VentasScreen
import com.sinc.mobile.app.features.ventas.HistorialVentasScreen
import com.sinc.mobile.app.features.tickets.TicketsListScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val MOVIMIENTO = "movimiento"
    const val SETTINGS = "settings"
    const val CHANGE_PASSWORD = "change_password"
    const val FORGOT_PASSWORD = "forgot_password?isFirstTime={isFirstTime}"
    fun createForgotPasswordRoute(isFirstTime: Boolean = false) = "forgot_password?isFirstTime=$isFirstTime"
    const val SPLASH = "splash"
    const val CREATE_UNIDAD_PRODUCTIVA = "create_unidad_productiva"
    const val EDIT_UNIDAD_PRODUCTIVA = "edit_unidad_productiva/{unidadId}"
    fun createEditUnidadProductivaRoute(unidadId: Int) = "edit_unidad_productiva/$unidadId"
    const val CAMPOS = "campos"
    const val MOVIMIENTO_FORM = "movimiento_form?unidadId={unidadId}&initialPage={initialPage}&especieId={especieId}&razaId={razaId}&categoriaId={categoriaId}"
    fun createMovimientoFormRoute(
        unidadId: String?, 
        initialPage: Int = 0,
        especieId: Int? = null,
        razaId: Int? = null,
        categoriaId: Int? = null
    ): String {
        val route = "movimiento_form?"
        val params = mutableListOf<String>()
        unidadId?.let { params.add("unidadId=$it") }
        params.add("initialPage=$initialPage")
        especieId?.let { params.add("especieId=$it") }
        razaId?.let { params.add("razaId=$it") }
        categoriaId?.let { params.add("categoriaId=$it") }

        return route + params.joinToString("&")
    }
    const val HISTORIAL_MOVIMIENTOS = "historial_movimientos"
    const val LOGISTICS = "logistics"
    const val CUENCA_INFO = "cuenca_info"
    const val VENTAS = "ventas"
    const val VENTAS_HISTORIAL = "ventas_historial"
    const val RESUMEN_MOVIMIENTOS = "resumen_movimientos/{month}/{year}"
    fun createResumenMovimientosRoute(month: Int, year: Int) = "resumen_movimientos/$month/$year"
    const val HELP = "help"
    const val SELECCION_CAMPO = "seleccion_campo"
    const val TERMS_OF_SERVICE = "terms_of_service"
    const val STOCK_DETAIL = "stock_detail/{speciesName}/{grouping}?unidadId={unidadId}"
    fun createStockDetailRoute(speciesName: String, grouping: String, unidadId: Int? = null): String {
        val base = "stock_detail/$speciesName/$grouping"
        return if (unidadId != null) "$base?unidadId=$unidadId" else base
    }

    // Ticket Routes
    const val TICKETS_LIST = "tickets_list"
    const val CREATE_TICKET_TYPE = "create_ticket_type"
    const val CREATE_TICKET_MESSAGE = "create_ticket_message/{ticketType}"
    fun createTicketMessageRoute(ticketType: String) = "create_ticket_message/$ticketType"
    const val TICKET_CONVERSATION = "ticket_conversation/{ticketId}"
    fun createTicketConversationRoute(ticketId: Long) = "ticket_conversation/$ticketId"
    const val NOTIFICATIONS = "notifications"
    const val WEATHER = "weather"
    const val WINDY_MAP = "windy_map/{layer}"
    fun createWindyMapRoute(layer: String) = "windy_map/$layer"
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
                    navController.navigate(Routes.createForgotPasswordRoute(isFirstTime = false))
                },
                onNavigateToFirstTime = {
                    navController.navigate(Routes.createForgotPasswordRoute(isFirstTime = true))
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
                onNavigateToHelp = { navController.navigate(Routes.HELP) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToTerms = { navController.navigate(Routes.TERMS_OF_SERVICE) }
            )
        }
        composable(
            route = Routes.PROFILE,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            com.sinc.mobile.app.features.profile.ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTerms = { navController.navigate(Routes.TERMS_OF_SERVICE) }
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
                    navController.navigate(Routes.createForgotPasswordRoute(isFirstTime = false))
                }
            )
        }
        composable(
            route = Routes.FORGOT_PASSWORD,
            arguments = listOf(navArgument("isFirstTime") {
                type = NavType.BoolType
                defaultValue = false
            }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val isFirstTime = backStackEntry.arguments?.getBoolean("isFirstTime") ?: false
            ForgotPasswordScreen(
                isFirstTime = isFirstTime,
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
                },
                navArgument("especieId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("razaId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("categoriaId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(300))
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

        composable(
            route = Routes.SELECCION_CAMPO,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(300))
            }
        ) {
            SeleccionCampoScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                mainScaffoldBottomPadding = 0.dp // Not needed for standalone screen
            )
        }

        composable(
            route = Routes.STOCK_DETAIL,
            arguments = listOf(
                navArgument("speciesName") { type = NavType.StringType },
                navArgument("grouping") { type = NavType.StringType },
                navArgument("unidadId") { 
                    type = NavType.IntType
                    defaultValue = -1 // Usamos -1 para indicar "ninguno" o "todos"
                }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val speciesName = backStackEntry.arguments?.getString("speciesName") ?: ""
            val grouping = backStackEntry.arguments?.getString("grouping") ?: "BY_ALL"
            val unidadId = backStackEntry.arguments?.getInt("unidadId").takeIf { it != -1 }
            
            com.sinc.mobile.app.features.stock.StockDetailScreen(
                speciesName = speciesName,
                grouping = grouping,
                unidadId = unidadId,
                onBack = { navController.popBackStack() },
                navController = navController
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

        composable(
            route = Routes.NOTIFICATIONS,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            com.sinc.mobile.app.features.notifications.NotificationsScreen(
                navController = navController,
                onBackPress = { navController.popBackStack() }
            )
        }

                composable(
                    route = Routes.WINDY_MAP,
                    arguments = listOf(navArgument("layer") { type = NavType.StringType }),
                    enterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) { backStackEntry ->
                    val layer = backStackEntry.arguments?.getString("layer") ?: "radar"
                    val baseLat = -27.367
                    val baseLong = -55.896
                    val defaultZoom = 8
                    // Construir la URL para el modo embed limpio
                    val windyUrl = "https://embed.windy.com/embed2.html?lat=$baseLat&lon=$baseLong&detailLat=$baseLat&detailLon=$baseLong&zoom=$defaultZoom&level=surface&overlay=$layer&menu=&message=&marker=true&calendar=now&pressure=&type=map&location=coordinates&detail=true&metricWind=km/h&metricTemp=°C&radarRange=-1"
        
                    WindyMapScreen(
                        windyUrl = windyUrl,
                        onBackPress = { navController.popBackStack() }
                    )
                }
        
                composable(
                    route = Routes.TERMS_OF_SERVICE,
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    }
                ) {
                    com.sinc.mobile.app.features.profile.TermsOfServiceScreen(
                        onBackPress = { navController.popBackStack() }
                    )
                }
            }
        }