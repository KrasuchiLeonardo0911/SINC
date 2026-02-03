package com.sinc.mobile.app.features.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.use_case.ticket.CreateTicketUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SubmissionStatus {
    IDLE,
    IN_PROGRESS,
    SUCCESS,
    ERROR
}

data class CreateTicketState(
    val submissionStatus: SubmissionStatus = SubmissionStatus.IDLE,
    val error: String? = null
)

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val createTicketUseCase: CreateTicketUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTicketState())
    val uiState = _uiState.asStateFlow()

    fun createTicket(type: String, message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(submissionStatus = SubmissionStatus.IN_PROGRESS, error = null) }
            val result = createTicketUseCase(CreateTicketData(mensaje = message, tipo = type))

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(submissionStatus = SubmissionStatus.SUCCESS) }
                }
                is Result.Failure -> {
                    val errorMessage = "Error al crear el ticket" // Generic error for now
                    _uiState.update { it.copy(submissionStatus = SubmissionStatus.ERROR, error = errorMessage) }
                }
            }
        }
    }

    fun resetSubmissionStatus() {
        _uiState.update { it.copy(submissionStatus = SubmissionStatus.IDLE) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
