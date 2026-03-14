package com.sinc.mobile.app.features.agenda

import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error

data class AgendaState(
    val items: List<AgendaItem> = emptyList(),
    val isLoading: Boolean = false,
    val isInitialLoad: Boolean = true,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class AgendaEvent {
    data class ToggleStatus(val id: Long, val isCompleted: Boolean) : AgendaEvent()
    data class DeleteItem(val id: Long) : AgendaEvent()
    data class SaveItem(val item: AgendaItem) : AgendaEvent()
    object Refresh : AgendaEvent()
}
