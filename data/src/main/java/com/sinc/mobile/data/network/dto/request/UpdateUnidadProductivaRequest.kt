package com.sinc.mobile.data.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUnidadProductivaRequest(
    @SerialName("superficie") val superficie: Double? = null,
    @SerialName("condicion_tenencia_id") val condicionTenenciaId: Int? = null,
    @SerialName("agua_animal_fuente_id") val aguaAnimalFuenteId: Int? = null,
    @SerialName("observaciones") val observaciones: String? = null
)
