package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTicketsUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    operator fun invoke(): Flow<List<Ticket>> {
        return ticketRepository.getTickets()
    }
}
