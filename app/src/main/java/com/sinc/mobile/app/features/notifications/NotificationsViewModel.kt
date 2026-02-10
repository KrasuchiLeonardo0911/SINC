package com.sinc.mobile.app.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.domain.model.Notification
import com.sinc.mobile.domain.use_case.notification.DeleteNotificationUseCase
import com.sinc.mobile.domain.use_case.notification.GetNotificationsUseCase
import com.sinc.mobile.domain.use_case.notification.MarkNotificationAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotificationsEvent {
    data class OnItemClick(val notification: Notification) : NotificationsEvent
    data class OnItemLongClick(val id: Long) : NotificationsEvent
    object OnDeleteSelected : NotificationsEvent
    object OnExitSelectionMode : NotificationsEvent
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsState())
    val uiState: StateFlow<NotificationsState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        loadNotifications()
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            is NotificationsEvent.OnItemClick -> {
                if (_uiState.value.isSelectionMode) {
                    toggleSelection(event.notification.id)
                } else {
                    handleNavigation(event.notification)
                }
            }
            is NotificationsEvent.OnItemLongClick -> {
                if (!_uiState.value.isSelectionMode) {
                    _uiState.update { it.copy(isSelectionMode = true) }
                }
                toggleSelection(event.id)
            }
            is NotificationsEvent.OnDeleteSelected -> {
                deleteSelectedNotifications()
            }
            is NotificationsEvent.OnExitSelectionMode -> {
                exitSelectionMode()
            }
        }
    }

    private fun handleNavigation(notification: Notification) {
        viewModelScope.launch {
            when (notification.type) {
                "ticket_response" -> {
                    notification.data?.get("ticket_id")?.let { ticketId ->
                        _navigationEvent.emit(Routes.TICKET_CONVERSATION.replace("{ticketId}", ticketId))
                    }
                }
                // Add other navigation cases here
            }
        }
    }


    private fun toggleSelection(id: Long) {
        _uiState.update { currentState ->
            val newSelectedIds = currentState.selectedIds.toMutableSet()
            if (newSelectedIds.contains(id)) {
                newSelectedIds.remove(id)
            } else {
                newSelectedIds.add(id)
            }
            // If the last selected item is deselected, exit selection mode
            val newSelectionMode = newSelectedIds.isNotEmpty()
            currentState.copy(selectedIds = newSelectedIds, isSelectionMode = newSelectionMode)
        }
    }

    private fun loadNotifications() {
        getNotificationsUseCase()
            .onEach { notifications ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notifications = notifications
                    )
                }
                markAllAsRead()
            }
            .launchIn(viewModelScope)
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value.notifications.forEach { notification ->
                if (!notification.isRead) {
                    markNotificationAsReadUseCase(notification.id)
                }
            }
        }
    }

    private fun deleteSelectedNotifications() {
        viewModelScope.launch {
            val idsToDelete = _uiState.value.selectedIds
            idsToDelete.forEach { id ->
                deleteNotificationUseCase(id)
            }
            exitSelectionMode()
        }
    }

    private fun exitSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = false, selectedIds = emptySet()) }
    }
}
