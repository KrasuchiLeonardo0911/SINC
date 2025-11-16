package com.sinc.mobile.data.network.dto.request

import com.google.gson.annotations.SerializedName

data class CreateUnidadProductivaRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("identificador_local") val identificadorLocal: String,
    @SerializedName("superficie") val superficie: Float,
    @SerializedName("latitud") val latitud: Float,
    @SerializedName("longitud") val longitud: Float,
    @SerializedName("municipio_id") val municipioId: Int,
    @SerializedName("condicion_tenencia_id") val condicionTenenciaId: Int?,
    @SerializedName("fuente_agua_id") val fuenteAguaId: Int?,
    @SerializedName("tipo_suelo_id") val tipoSueloId: Int?,
    @SerializedName("tipo_pasto_id") val tipoPastoId: Int?
)
