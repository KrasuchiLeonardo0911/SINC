package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.TicketDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toTicketWithMessages
import com.sinc.mobile.data.network.api.TicketApiService
import com.sinc.mobile.data.network.dto.AddMessageRequest
import com.sinc.mobile.data.network.dto.CreateTicketRequest
import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.repository.TicketRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result.Success
import com.sinc.mobile.domain.util.Result.Failure
import com.sinc.mobile.domain.model.GenericError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val apiService: TicketApiService,
    private val ticketDao: TicketDao
) : TicketRepository {

    // TODO: Inject a session manager to get the real user ID
    private val currentUserId: Long = 4 // Hardcoded for now

    override fun getTickets(): Flow<List<Ticket>> {
        return ticketDao.getAllTicketsWithMessages().map { list ->
            list.map { it.toDomain() }
        }
        
    }

    override suspend fun syncTickets(): Result<Unit, Error> {
        return try {
            val remoteTickets = apiService.getTickets()
            val ticketWithMessagesList = remoteTickets.map { it.toTicketWithMessages(currentUserId) }
            ticketDao.clearAndInsert(ticketWithMessagesList)
            Success(Unit)
        } catch (e: IOException) {
            Failure(GenericError("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Failure(GenericError("Error inesperado al sincronizar tickets: ${e.message}"))
        }
    }

    override suspend fun createTicket(data: CreateTicketData): Result<Ticket, Error> {
        return try {
            val request = CreateTicketRequest(
                mensaje = data.mensaje,
                tipoSolicitud = data.tipo
            )
            val response = apiService.createTicket(request)
            val ticketWithMessages = response.ticket.toTicketWithMessages(currentUserId)
            ticketDao.upsertTicket(ticketWithMessages)
            Success(ticketWithMessages.toDomain())
        } catch (e: IOException) {
            Failure(GenericError("Error de red al crear el ticket: ${e.message}"))
        } catch (e: Exception) {
            Failure(GenericError("Error inesperado al crear el ticket: ${e.message}"))
        }
    }

    override suspend fun addMessage(ticketId: Long, message: String): Result<Ticket, Error> {
        return try {
            val request = AddMessageRequest(message = message)
            val updatedTicketDto = apiService.addMessage(ticketId, request)
            val ticketWithMessages = updatedTicketDto.toTicketWithMessages(currentUserId)
            ticketDao.upsertTicket(ticketWithMessages)
            Success(ticketWithMessages.toDomain())
        } catch (e: IOException) {
            Failure(GenericError("Error de red al enviar el mensaje: ${e.message}"))
        } catch (e: Exception) {
            Failure(GenericError("Error inesperado al enviar el mensaje: ${e.message}"))
        }
    }

    override suspend fun syncTicket(ticketId: Long): Result<Unit, Error> {
        return try {
            val remoteTicket = apiService.getTicket(ticketId)
            val ticketWithMessages = remoteTicket.toTicketWithMessages(currentUserId)
            ticketDao.upsertTicket(ticketWithMessages)
            Success(Unit)
        } catch (e: IOException) {
            Failure(GenericError("Error de red al sincronizar el ticket: ${e.message}"))
        } catch (e: Exception) {
            Failure(GenericError("Error inesperado al sincronizar el ticket: ${e.message}"))
        }
    }
}
