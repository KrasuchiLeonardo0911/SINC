package com.sinc.mobile.app.features.forgotpassword

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.ResetPasswordWithCodeData
import com.sinc.mobile.domain.use_case.RequestPasswordResetUseCase
import com.sinc.mobile.domain.use_case.ResetPasswordWithCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccessDialog: Boolean = false,
    val step: ForgotPasswordStep = ForgotPasswordStep.EnterEmail,
    val email: String = ""
)

enum class ForgotPasswordStep {
    EnterEmail,
    EnterCodeAndPassword
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordWithCodeUseCase: ResetPasswordWithCodeUseCase
) : ViewModel() {

    private val _state = mutableStateOf(ForgotPasswordState())
    val state: State<ForgotPasswordState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailEntered(email: String) {
        viewModelScope.launch {
            _state.value = state.value.copy(isLoading = true, email = email)
            val result = requestPasswordResetUseCase(email)
            result.onSuccess {
                _state.value = state.value.copy(
                    isLoading = false,
                    step = ForgotPasswordStep.EnterCodeAndPassword
                )
                _eventFlow.emit(UiEvent.ShowSnackbar("Se ha enviado un código a tu correo."))
            }.onFailure {
                _state.value = state.value.copy(isLoading = false, error = it.message)
                _eventFlow.emit(UiEvent.ShowSnackbar(it.message ?: "Error desconocido"))
            }
        }
    }

    fun onResetWithCode(code: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _state.value = state.value.copy(isLoading = true)
            val data = ResetPasswordWithCodeData(
                email = state.value.email,
                code = code,
                password = password,
                passwordConfirmation = passwordConfirmation
            )
            val result = resetPasswordWithCodeUseCase(data)
            result.onSuccess {
                _state.value = state.value.copy(isLoading = false, showSuccessDialog = true)
            }.onFailure {
                _state.value = state.value.copy(isLoading = false, error = it.message)
                _eventFlow.emit(UiEvent.ShowSnackbar(it.message ?: "Error desconocido"))
            }
        }
    }

    fun onSuccessDialogDismissed() {
        viewModelScope.launch {
            _state.value = state.value.copy(showSuccessDialog = false)
            _eventFlow.emit(UiEvent.NavigateToLogin)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateToLogin : UiEvent()
    }
}