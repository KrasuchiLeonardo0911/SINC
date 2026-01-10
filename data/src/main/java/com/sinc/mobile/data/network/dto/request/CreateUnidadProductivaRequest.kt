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
    @SerialName("tipo_suelo_id") val tipoSueloId: Int?,
    @SerialName("tipo_pasto_id") val tipoPastoId: Int?
)
