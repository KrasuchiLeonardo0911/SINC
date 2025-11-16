package com.sinc.mobile.data.network.dto.response

import com.google.gson.annotations.SerializedName

data class UnidadProductivaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("identificador_local") val identificadorLocal: String,
    @SerializedName("superficie") val superficie: Float,
    @SerializedName("latitud") val latitud: String?,
    @SerializedName("longitud") val longitud: String?,
    @SerializedName("municipio_id") val municipioId: Int,
    @SerializedName("condicion_tenencia_id") val condicionTenenciaId: Int?,
    @SerializedName("fuente_agua_id") val fuenteAguaId: Int?,
    @SerializedName("tipo_suelo_id") val tipoSueloId: Int?,
    @SerializedName("tipo_pasto_id") val tipoPastoId: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)