package com.sinc.mobile.app.features.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.use_case.ticket.CreateTicketUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTicketState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class CreateTicketEvent {
    data class TicketCreated(val ticket: Ticket) : CreateTicketEvent()
    data class ShowError(val message: String) : CreateTicketEvent()
}

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val createTicketUseCase: CreateTicketUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTicketState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CreateTicketEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun createTicket(type: String, message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = createTicketUseCase(CreateTicketData(mensaje = message, tipo = type))
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> {
                    _eventFlow.emit(CreateTicketEvent.TicketCreated(result.data))
                }
                is Result.Failure -> {
                    val errorMessage = "Error al crear el ticket" // Generic error for now
                    _uiState.update { it.copy(error = errorMessage) }
                    _eventFlow.emit(CreateTicketEvent.ShowError(errorMessage))
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
