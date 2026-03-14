package com.sinc.mobile.domain.use_case.agenda

import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.repository.agenda.AgendaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAgendaItemsUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    operator fun invoke(): Flow<List<AgendaItem>> = repository.getAgendaItems()
}
