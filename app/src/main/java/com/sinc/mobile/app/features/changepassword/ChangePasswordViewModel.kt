package com.sinc.mobile.app.features.changepassword

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.ChangePasswordData
import com.sinc.mobile.domain.use_case.ChangePasswordUseCase
import com.sinc.mobile.domain.use_case.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccessDialog: Boolean = false
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = mutableStateOf(ChangePasswordState())
    val state: State<ChangePasswordState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onPasswordChange(passwordData: ChangePasswordData) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = changePasswordUseCase(passwordData)
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, showSuccessDialog = true)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, error = it.message)
                _eventFlow.emit(UiEvent.ShowSnackbar(it.message ?: "Error desconocido"))
            }
        }
    }

    fun onSuccessDialogDismissed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(showSuccessDialog = false)
            logoutUseCase() // Clear local token
            _eventFlow.emit(UiEvent.NavigateToLogin) // Trigger navigation to login
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateToLogin : UiEvent()
    }
}
