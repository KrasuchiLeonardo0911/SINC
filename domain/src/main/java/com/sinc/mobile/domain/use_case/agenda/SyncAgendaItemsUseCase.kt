package com.sinc.mobile.domain.use_case.agenda

import com.sinc.mobile.domain.repository.agenda.AgendaRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SyncAgendaItemsUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(): Result<Unit, Error> = repository.syncAgendaItems()
}
