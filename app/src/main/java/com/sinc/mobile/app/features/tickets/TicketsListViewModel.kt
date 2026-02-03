package com.sinc.mobile.app.features.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.use_case.ticket.GetTicketsOnceUseCase
import com.sinc.mobile.domain.use_case.ticket.GetTicketsUseCase
import com.sinc.mobile.domain.use_case.ticket.SyncTicketsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TicketsListState(
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TicketsListViewModel @Inject constructor(
    private val getTicketsUseCase: GetTicketsUseCase,
    private val syncTicketsUseCase: SyncTicketsUseCase,
    private val getTicketsOnceUseCase: GetTicketsOnceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsListState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTickets()
        syncTickets()
    }

    private fun loadTickets() {
        getTicketsUseCase()
            .onEach { tickets ->
                _uiState.update { it.copy(tickets = tickets, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun syncTickets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            syncTicketsUseCase()
            // After sync, imperatively fetch the list and update the state
            val tickets = getTicketsOnceUseCase()
            _uiState.update { it.copy(tickets = tickets, isLoading = false) }
        }
    }
}
