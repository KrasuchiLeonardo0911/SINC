package com.sinc.mobile.domain.use_case.agenda

import com.sinc.mobile.domain.repository.agenda.AgendaRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class ToggleAgendaStatusUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(id: Long, isCompleted: Boolean): Result<Unit, Error> = 
        repository.toggleAgendaStatus(id, isCompleted)
}
