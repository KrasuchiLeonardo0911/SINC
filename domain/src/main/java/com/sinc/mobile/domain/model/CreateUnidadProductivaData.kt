package com.sinc.mobile.domain.model

data class CreateUnidadProductivaData(
    val nombre: String,
    val identificadorLocal: String,
    val superficie: Float,
    val latitud: Float,
    val longitud: Float,
    val municipioId: Int,
    val condicionTenenciaId: Int?,
    val fuenteAguaId: Int?,
    val tipoSueloId: Int?,
    val tipoPastoId: Int?
)
