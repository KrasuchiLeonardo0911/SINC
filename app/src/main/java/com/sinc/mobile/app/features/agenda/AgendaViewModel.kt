package com.sinc.mobile.app.features.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.use_case.agenda.*
import com.sinc.mobile.domain.use_case.profile.GetUserProfileUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val getAgendaItemsUseCase: GetAgendaItemsUseCase,
    private val syncAgendaItemsUseCase: SyncAgendaItemsUseCase,
    private val toggleAgendaStatusUseCase: ToggleAgendaStatusUseCase,
    private val deleteAgendaItemUseCase: DeleteAgendaItemUseCase,
    private val saveAgendaItemUseCase: SaveAgendaItemUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaState())
    val uiState: StateFlow<AgendaState> = _uiState.asStateFlow()

    private var currentUserId: Long = 0

    init {
        fetchUserId()
        loadAgenda()
        refreshAgenda()
    }

    private fun fetchUserId() {
        viewModelScope.launch {
            when (val result = getUserProfileUseCase()) {
                is Result.Success -> {
                    currentUserId = result.data.id.toLong()
                }
                is Result.Failure -> { }
            }
        }
    }

    private fun loadAgenda() {
        viewModelScope.launch {
            getAgendaItemsUseCase().collect { items ->
                _uiState.update { it.copy(items = items, isInitialLoad = false) }
            }
        }
    }

    fun onEvent(event: AgendaEvent) {
        when (event) {
            is AgendaEvent.Refresh -> refreshAgenda()
            is AgendaEvent.ToggleStatus -> toggleStatus(event.id, event.isCompleted)
            is AgendaEvent.DeleteItem -> deleteItem(event.id)
            is AgendaEvent.SaveItem -> saveItem(event.item)
        }
    }

    private fun saveItem(item: AgendaItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            if (currentUserId == 0L) {
                fetchUserId()
            }

            val itemWithUser = item.copy(userId = currentUserId)
            
            when (val result = saveAgendaItemUseCase(itemWithUser)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, error = null, isSuccess = true) }
                }
                is Result.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    private fun refreshAgenda() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = syncAgendaItemsUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, error = null) }
                is Result.Failure -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
            }
        }
    }

    private fun toggleStatus(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            when (val result = toggleAgendaStatusUseCase(id, isCompleted)) {
                is Result.Success -> { }
                is Result.Failure -> _uiState.update { it.copy(error = result.error.message) }
            }
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            when (val result = deleteAgendaItemUseCase(id)) {
                is Result.Success -> { }
                is Result.Failure -> _uiState.update { it.copy(error = result.error.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
