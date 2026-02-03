package com.sinc.mobile.app.features.tickets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.ticket.Message
import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.use_case.ticket.AddMessageToTicketUseCase
import com.sinc.mobile.domain.use_case.ticket.GetTicketUseCase
import com.sinc.mobile.domain.use_case.ticket.GetTicketsUseCase
import com.sinc.mobile.domain.use_case.ticket.SyncCurrentTicketUseCase
import com.sinc.mobile.domain.use_case.ticket.GetTicketFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
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
    private val addMessageToTicketUseCase: AddMessageToTicketUseCase,
    private val syncCurrentTicketUseCase: SyncCurrentTicketUseCase,
    private val getTicketFlowUseCase: GetTicketFlowUseCase
) : ViewModel() {

    private val ticketId: Long = savedStateHandle.get<Long>("ticketId")!!

    private val _uiState = MutableStateFlow(TicketConversationState())
    val uiState = _uiState.asStateFlow()

    private val currentUserId: Long = 4 // As hardcoded in repository

    init {
        loadConversation()
    }

    private fun getCurrentUserName(): String {
        return uiState.value.ticket?.messages?.firstOrNull { it.userId == currentUserId }?.userName
            ?: "Tú" // Placeholder if not found, should be provided by SessionManager
    }

    private fun loadConversation() {
        getTicketFlowUseCase(ticketId)
            .onEach { ticket ->
                Log.d("TicketVM", "loadConversation - Flow received ticket ${ticket?.id}, messages: ${ticket?.messages?.size}")
                _uiState.update {
                    it.copy(
                        ticket = ticket,
                        messages = ticket?.messages ?: emptyList(),
                        isLoading = false,
                        isRefreshing = false // Ensure refreshing is turned off after sync
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(message: String) {
        if (message.isBlank() || _uiState.value.isSendingMessage) return

        val optimisticMessage = Message(
            id = -(System.currentTimeMillis()), // Temporary ID for optimistic update
            ticketId = ticketId,
            userId = currentUserId,
            message = message,
            createdAt = LocalDateTime.now(),
            userName = getCurrentUserName(),
            isFromUser = true
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingMessage = true,
                    error = null,
                    messages = it.messages + optimisticMessage // Add optimistic message
                )
            }

            when (addMessageToTicketUseCase(ticketId, message)) {
                is com.sinc.mobile.domain.util.Result.Success -> {
                    // The `loadConversation` flow will automatically update the UI once the
                    // repository saves the real message to the database.
                    _uiState.update { it.copy(isSendingMessage = false) }
                }
                is com.sinc.mobile.domain.util.Result.Failure -> {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo enviar el mensaje.",
                            isSendingMessage = false,
                            messages = it.messages.filter { msg -> msg.id != optimisticMessage.id } // Remove optimistic on failure
                        )
                    }
                }
            }
        }
    }

    fun refreshConversation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            syncCurrentTicketUseCase(ticketId)
            // No need to set isRefreshing to false here, the `loadConversation` flow will do it.
        }
    }
}
