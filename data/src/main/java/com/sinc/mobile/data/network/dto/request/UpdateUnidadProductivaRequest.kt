package com.sinc.mobile.data.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUnidadProductivaRequest(
    @SerialName("superficie") val superficie: Double? = null,
    @SerialName("condicion_tenencia_id") val condicionTenenciaId: Int? = null,
    @SerialName("agua_animal_fuente_id") val aguaAnimalFuenteId: Int? = null,
    @SerialName("agua_humano_fuente_id") val aguaHumanoFuenteId: Int? = null,
    @SerialName("agua_humano_en_casa") val aguaHumanoEnCasa: Int? = null,
    @SerialName("agua_humano_distancia") val aguaHumanoDistancia: Int? = null,
    @SerialName("agua_animal_distancia") val aguaAnimalDistancia: Int? = null,
    @SerialName("tipo_suelo_predominante_id") val tipoSueloId: Int? = null,
    @SerialName("tipo_pasto_predominante_id") val tipoPastoId: Int? = null,
    @SerialName("forrajeras_predominante") val forrajerasPredominante: Int? = null,
    @SerialName("habita") val habita: Int? = null,
    @SerialName("observaciones") val observaciones: String? = null
)
