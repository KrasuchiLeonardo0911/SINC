package com.sinc.mobile.data.network.dto.agenda

import kotlinx.serialization.Serializable

@Serializable
data class UpdateAgendaStatusRequest(
    val completada: Boolean
)
