package com.sinc.mobile.data.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUnidadProductivaRequest(
    @SerialName("nombre") val nombre: String,
    @SerialName("identificador_local") val identificadorLocal: String,
    @SerialName("superficie") val superficie: Float,
    @SerialName("latitud") val latitud: Float,
    @SerialName("longitud") val longitud: Float,
    @SerialName("municipio_id") val municipioId: Int,
    @SerialName("condicion_tenencia_id") val condicionTenenciaId: Int?,
    @SerialName("fuente_agua_id") val fuenteAguaId: Int?,
    @SerialName("paraje_id") val parajeId: Int? = null,
    @SerialName("fecha_inicio") val fechaInicio: String? = null,
    @SerialName("tipos_suelo") val tiposSuelo: List<SueloRequestDto>? = null,
    @SerialName("recursos_forrajeros") val recursosForrajeros: List<PastoRequestDto>? = null
)
