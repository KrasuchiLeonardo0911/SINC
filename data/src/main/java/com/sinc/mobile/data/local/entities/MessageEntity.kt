package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "ticket_messages",
    foreignKeys = [
        ForeignKey(
            entity = TicketEntity::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ticketId"])]
)
data class MessageEntity(
    @PrimaryKey val id: Long,
    val ticketId: Long,
    val userId: Long,
    val message: String,
    val createdAt: LocalDateTime,
    val userName: String,
    val isFromUser: Boolean
)
