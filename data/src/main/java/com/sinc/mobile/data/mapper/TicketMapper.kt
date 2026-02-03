package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.local.entities.MessageEntity
import com.sinc.mobile.data.local.entities.TicketEntity
import com.sinc.mobile.data.local.relation.TicketWithMessages
import com.sinc.mobile.data.network.dto.MessageDto
import com.sinc.mobile.data.network.dto.TicketDto
import com.sinc.mobile.domain.model.ticket.Message
import com.sinc.mobile.domain.model.ticket.Ticket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun TicketDto.toTicketWithMessages(currentUserId: Long): TicketWithMessages {
    val ticketEntity = TicketEntity(
        id = this.id,
        userId = this.userId,
        solicitableId = this.solicitableId,
        solicitableType = this.solicitableType,
        tipo = this.tipo,
        status = this.status,
        asunto = this.asunto,
        responderId = this.responderId,
        createdAt = LocalDateTime.parse(this.createdAt, DateTimeFormatter.ISO_DATE_TIME),
        updatedAt = LocalDateTime.parse(this.updatedAt, DateTimeFormatter.ISO_DATE_TIME),
        responderName = this.responder?.name
    )
    val messageEntities = this.messages.map { it.toEntity(this.id, currentUserId) }
    return TicketWithMessages(ticket = ticketEntity, messages = messageEntities)
}

fun MessageDto.toEntity(ticketId: Long, currentUserId: Long): MessageEntity {
    return MessageEntity(
        id = this.id,
        ticketId = ticketId,
        userId = this.userId,
        message = this.message,
        createdAt = LocalDateTime.parse(this.createdAt, DateTimeFormatter.ISO_DATE_TIME),
        userName = this.user.name,
        isFromUser = this.userId == currentUserId
    )
}

fun TicketWithMessages.toDomain(): Ticket {
    return Ticket(
        id = this.ticket.id,
        userId = this.ticket.userId,
        solicitableId = this.ticket.solicitableId,
        solicitableType = this.ticket.solicitableType,
        tipo = this.ticket.tipo,
        status = this.ticket.status,
        asunto = this.ticket.asunto,
        responderId = this.ticket.responderId,
        createdAt = this.ticket.createdAt,
        updatedAt = this.ticket.updatedAt,
        responderName = this.ticket.responderName,
        messages = this.messages.map { it.toDomain() }
    )
}

fun MessageEntity.toDomain(): Message {
    return Message(
        id = this.id,
        ticketId = this.ticketId,
        userId = this.userId,
        message = this.message,
        createdAt = this.createdAt,
        userName = this.userName,
        isFromUser = this.isFromUser
    )
}
