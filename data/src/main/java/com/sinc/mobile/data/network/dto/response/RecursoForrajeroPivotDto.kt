package com.sinc.mobile.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecursoForrajeroPivotDetailDto(
    @SerialName("unidad_productiva_id") val unidadProductivaId: Int,
    @SerialName("tipo_pasto_id") val tipoPastoId: Int,
    @SerialName("porcentaje") val porcentaje: Int?
)

@Serializable
data class RecursoForrajeroPivotDto(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("pivot") val pivot: RecursoForrajeroPivotDetailDto
)
