package com.sinc.mobile.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sinc.mobile.data.local.entities.MessageEntity
import com.sinc.mobile.data.local.entities.TicketEntity

data class TicketWithMessages(
    @Embedded val ticket: TicketEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ticketId"
    )
    val messages: List<MessageEntity>
)
