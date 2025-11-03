package com.sinc.mobile.app.navigation

import com.sinc.mobile.domain.navigation.NavigationCommand
import com.sinc.mobile.domain.navigation.NavigationManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNavigationManager @Inject constructor() : NavigationManager {
    private val _commands = MutableSharedFlow<NavigationCommand>()
    override val commands = _commands.asSharedFlow()

    override suspend fun navigate(command: NavigationCommand) {
        _commands.emit(command)
    }
}