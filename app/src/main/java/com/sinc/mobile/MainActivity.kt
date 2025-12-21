package com.sinc.mobile

import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var sessionManager: SessionManager

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

                LaunchedEffect(Unit) {
                    navigationManager.commands.collectLatest { command ->
                        if (command is NavigationCommand.NavigateToLogin) {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
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
