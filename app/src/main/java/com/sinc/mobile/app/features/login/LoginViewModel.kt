package com.sinc.mobile.app.features.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.AuthResult
import com.sinc.mobile.domain.use_case.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)

            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> {
                    _state.value = LoginState(loginSuccess = true)
                    // Aquí en el futuro guardaríamos el token
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
