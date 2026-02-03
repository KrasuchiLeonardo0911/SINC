package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.repository.TicketRepository
import javax.inject.Inject

class GetTicketUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(ticketId: Long): Ticket? {
        return repository.getTicket(ticketId)
    }
}
