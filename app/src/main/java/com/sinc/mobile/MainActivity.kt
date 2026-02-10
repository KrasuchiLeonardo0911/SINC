package com.sinc.mobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.sinc.mobile.app.navigation.AppNavigation
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.navigation.NavigationCommand
import com.sinc.mobile.domain.navigation.NavigationManager
import com.sinc.mobile.ui.theme.SincMobileTheme
import androidx.compose.foundation.layout.Box
import com.sinc.mobile.app.ui.components.GlobalBanner
import com.google.firebase.messaging.FirebaseMessaging
import com.sinc.mobile.app.features.home.MainViewModel
import com.sinc.mobile.app.firebase.MyFirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var sessionManager: SessionManager

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val startDestination = if (sessionManager.getAuthToken() != null) {
            Routes.HOME
        } else {
            Routes.LOGIN
        }

        enableEdgeToEdge()
        setContent {
            SincMobileTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                // LaunchedEffect to collect navigation commands
                LaunchedEffect(Unit) {
                    navigationManager.commands.collectLatest { command ->
                        if (command is NavigationCommand.NavigateToLogin) {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                // LaunchedEffect to handle notification clicks
                LaunchedEffect(context) {
                    val intent = (context as? MainActivity)?.intent
                    intent?.let {
                        if (it.action == MyFirebaseMessagingService.ACTION_OPEN_TICKET_CONVERSATION) {
                            val ticketId = it.extras?.getLong(MyFirebaseMessagingService.EXTRA_TICKET_ID)
                            if (ticketId != null) {
                                Log.d("MainActivity", "Navigating to ticket conversation for ID: $ticketId")
                                navController.navigate(Routes.createTicketConversationRoute(ticketId))
                                // Clear the intent action and extras to prevent re-triggering
                                it.action = ""
                                it.replaceExtras(Bundle())
                            }
                        }
                    }
                }
                
                // LaunchedEffect to get and log FCM token
                LaunchedEffect(Unit) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
                            return@addOnCompleteListener
                        }
                        val token = task.result
                        Log.d("FCM_TOKEN", "FCM Token retrieved: $token")
                        mainViewModel.sendFcmToken(token)
                    }
                }

                Box {
                    AppNavigation(navController, startDestination = startDestination)
                    GlobalBanner()
                }

            }
        }
    }
}
