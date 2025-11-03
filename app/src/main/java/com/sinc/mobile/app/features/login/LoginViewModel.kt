package com.sinc.mobile.app.features.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.AuthResult
import com.sinc.mobile.domain.use_case.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.sinc.mobile.domain.use_case.SyncDataUseCase

data class LoginState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null
)

sealed class NavigationEvent {
    object NavigateToHome : NavigationEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val syncDataUseCase: SyncDataUseCase
) : ViewModel() {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)

            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> {
                    _state.value = LoginState(isSyncing = true)
                    val syncResult = syncDataUseCase()
                    if (syncResult.isSuccess) {
                        _state.value = LoginState()
                        _navigationEvent.emit(NavigationEvent.NavigateToHome)
                    } else {
                        _state.value = LoginState(error = syncResult.exceptionOrNull()?.message ?: "Error durante la sincronización.")
                    }
                }
                is AuthResult.InvalidCredentials -> {
                    _state.value = LoginState(error = "Credenciales inválidas.")
                }
                is AuthResult.NetworkError -> {
                    _state.value = LoginState(error = "Error de red. Revisa tu conexión.")
                }
                is AuthResult.UnknownError -> {
                    _state.value = LoginState(error = result.message)
                }
            }
        }
    }
}
