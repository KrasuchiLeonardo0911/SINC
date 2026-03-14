package com.sinc.mobile.domain.use_case.agenda

import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.repository.agenda.AgendaRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SaveAgendaItemUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(item: AgendaItem): Result<AgendaItem, Error> = repository.saveAgendaItem(item)
}
