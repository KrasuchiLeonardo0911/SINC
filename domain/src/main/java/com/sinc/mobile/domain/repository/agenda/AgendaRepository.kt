package com.sinc.mobile.domain.repository.agenda

import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import kotlinx.coroutines.flow.Flow

interface AgendaRepository {
    fun getAgendaItems(): Flow<List<AgendaItem>>
    fun getAgendaItemById(id: Long): Flow<AgendaItem?>
    suspend fun syncAgendaItems(): Result<Unit, Error>
    suspend fun saveAgendaItem(item: AgendaItem): Result<AgendaItem, Error>
    suspend fun deleteAgendaItem(id: Long): Result<Unit, Error>
    suspend fun toggleAgendaStatus(id: Long, isCompleted: Boolean): Result<Unit, Error>
}
