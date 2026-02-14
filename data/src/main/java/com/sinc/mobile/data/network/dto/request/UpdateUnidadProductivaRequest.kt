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
    @SerialName("habita") val habita: Int? = null,
    @SerialName("observaciones") val observaciones: String? = null,
    @SerialName("tipos_suelo") val tiposSuelo: List<SueloRequestDto>? = null,
    @SerialName("recursos_forrajeros") val recursosForrajeros: List<PastoRequestDto>? = null
)
