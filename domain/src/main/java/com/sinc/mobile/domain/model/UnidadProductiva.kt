package com.sinc.mobile.domain.model

data class UnidadProductiva(
    val id: Int,
    val nombre: String?,
    val identificadorLocal: String?,
    val superficie: Float?,
    val latitud: Double?,
    val longitud: Double?,
    val municipioId: Int?,
    val condicionTenenciaId: Int?,
    val fuenteAguaId: Int?,
    val tipoSueloId: Int?,
    val tipoPastoId: Int?
)