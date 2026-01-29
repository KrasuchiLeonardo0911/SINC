package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.repository.TicketRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SyncCurrentTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(ticketId: Long): Result<Unit, Error> {
        return ticketRepository.syncTicket(ticketId)
    }
}
