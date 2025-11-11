package com.sinc.mobile.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.use_case.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            logoutUseCase().onSuccess {
                _navigationEvent.emit(NavigationEvent.NavigateToLogin)
            }.onFailure {
                // Handle error, e.g., log it or show a toast
                // For now, we'll just navigate to login even on failure to clear session
                _navigationEvent.emit(NavigationEvent.NavigateToLogin)
            }
        }
    }

    sealed class NavigationEvent {
        object NavigateToLogin : NavigationEvent()
    }
}
