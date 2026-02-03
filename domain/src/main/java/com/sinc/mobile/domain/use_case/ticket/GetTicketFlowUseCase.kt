package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTicketFlowUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    operator fun invoke(ticketId: Long): Flow<Ticket?> {
        return repository.getTicketFlow(ticketId)
    }
}