package com.sinc.mobile.app.features.tickets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.ticket.Message
import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.use_case.ticket.AddMessageToTicketUseCase
import com.sinc.mobile.domain.use_case.ticket.GetTicketsUseCase
import com.sinc.mobile.domain.use_case.ticket.SyncCurrentTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TicketConversationState(
    val ticket: Ticket? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val isSendingMessage: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TicketConversationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getTicketsUseCase: GetTicketsUseCase,
    private val addMessageToTicketUseCase: AddMessageToTicketUseCase,
    private val syncCurrentTicketUseCase: SyncCurrentTicketUseCase
) : ViewModel() {

    private val ticketId: Long = savedStateHandle.get<Long>("ticketId")!!

    private val _uiState = MutableStateFlow(TicketConversationState())
    val uiState = _uiState.asStateFlow()

    init {
        loadConversation()
    }

    private fun loadConversation() {
        getTicketsUseCase()
            .map { tickets -> tickets.find { it.id == ticketId } }
            .onEach { ticket ->
                _uiState.update {
                    it.copy(
                        ticket = ticket,
                        messages = ticket?.messages ?: emptyList(),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingMessage = true) }

            val result = addMessageToTicketUseCase(ticketId, message)

            if (result is com.sinc.mobile.domain.util.Result.Success) {
                // If the message was sent successfully, update the UI with the returned ticket object
                _uiState.update {
                    it.copy(
                        ticket = result.data,
                        messages = result.data.messages
                    )
                }
            } else if (result is com.sinc.mobile.domain.util.Result.Failure) {
                // If the call fails, show an error
                _uiState.update {
                    it.copy(error = "No se pudo enviar el mensaje.")
                }
            }

            _uiState.update { it.copy(isSendingMessage = false) }
        }
    }

    fun refreshConversation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            syncCurrentTicketUseCase(ticketId)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
