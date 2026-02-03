package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.repository.TicketRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTicketsOnceUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(): List<Ticket> {
        return repository.getTickets().first()
    }
}
