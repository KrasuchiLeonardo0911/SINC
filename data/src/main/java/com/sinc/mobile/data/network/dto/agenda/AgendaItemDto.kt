package com.sinc.mobile.data.network.dto.agenda

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgendaItemDto(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val tipo: String = "general",
    @SerialName("fecha_programada") val fechaProgramada: String,
    @SerialName("completada_en") val completadaEn: String? = null,
    val notificado: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AgendaResponseDto(
    val success: Boolean = true,
    val agenda: List<AgendaItemDto>? = null,
    val item: AgendaItemDto? = null,
    val message: String? = null
)
