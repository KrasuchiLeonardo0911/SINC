package com.sinc.mobile.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SueloPivotDetailDto(
    @SerialName("unidad_productiva_id") val unidadProductivaId: Int,
    @SerialName("tipo_suelo_id") val tipoSueloId: Int,
    @SerialName("porcentaje") val porcentaje: Int
)

@Serializable
data class TipoSueloPivotDto(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("pivot") val pivot: SueloPivotDetailDto
)
