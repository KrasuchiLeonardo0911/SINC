package com.sinc.mobile.domain.use_case.agenda

import com.sinc.mobile.domain.repository.agenda.AgendaRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class DeleteAgendaItemUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit, Error> = repository.deleteAgendaItem(id)
}
