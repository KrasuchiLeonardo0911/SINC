package com.sinc.mobile.domain.navigation

import kotlinx.coroutines.flow.SharedFlow

sealed interface NavigationCommand {
    object NavigateToLogin : NavigationCommand
}

interface NavigationManager {
    val commands: SharedFlow<NavigationCommand>
    suspend fun navigate(command: NavigationCommand)
}
