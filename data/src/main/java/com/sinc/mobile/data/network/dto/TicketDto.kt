package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketDto(
    @SerialName("id") val id: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("tipo") val tipo: String,
    @SerialName("status") val status: String,
    @SerialName("mensaje") val asunto: String, // "mensaje" in JSON is the subject/title
    @SerialName("responder_id") val responderId: Long?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("messages") val messages: List<MessageDto> = emptyList(),
    @SerialName("responder") val responder: UserDto?
)
