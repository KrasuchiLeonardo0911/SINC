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
import android.util.Log
import com.sinc.mobile.BuildConfig
import com.sinc.mobile.domain.model.GenericError

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
            _state.value = _state.value.copy(isLoading = true, error = null) // Clear previous errors

            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isSyncing = true) // Update state correctly
                    val syncResult = syncDataUseCase()
                    if (syncResult is com.sinc.mobile.domain.util.Result.Success) {
                        _state.value = _state.value.copy(isSyncing = false) // Clear syncing state
                        _navigationEvent.emit(NavigationEvent.NavigateToHome)
                    } else if (syncResult is com.sinc.mobile.domain.util.Result.Failure) {
                        val errorMessage = (syncResult.error as? GenericError)?.message ?: "Error desconocido durante la sincronización."
                        if (BuildConfig.DEBUG) {
                            Log.e("LoginSyncError", "Sync failed: $errorMessage")
                        }
                        _state.value = _state.value.copy(
                            isSyncing = false,
                            error = "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde."
                        )
                    }
                }
                is AuthResult.InvalidCredentials -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Credenciales inválidas.")
                }
                is AuthResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Error de red. Revisa tu conexión.")
                }
                is AuthResult.UnknownError -> {
                    if (BuildConfig.DEBUG) {
                        Log.e("LoginError", "Unknown login error: ${result.message}")
                    }
                    _state.value = _state.value.copy(isLoading = false, error = "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.")
                }
            }
        }
    }
}
