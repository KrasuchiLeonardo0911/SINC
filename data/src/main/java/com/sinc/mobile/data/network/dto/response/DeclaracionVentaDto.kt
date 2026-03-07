package com.sinc.mobile.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeclaracionVentaDto(
    @SerialName("id") val id: Int,
    @SerialName("productor_id") val productorId: Int,
    @SerialName("unidad_productiva_id") val unidadProductivaId: Int,
    @SerialName("especie_id") val especieId: Int,
    @SerialName("raza_id") val razaId: Int,
    @SerialName("categoria_animal_id") val categoriaAnimalId: Int,
    @SerialName("venta_lote_id") val ventaLoteId: Int? = null,
    @SerialName("historial_ciclo_id") val historialCicloId: Int? = null,
    @SerialName("cantidad") val cantidad: Int,
    @SerialName("estado") val estado: String,
    @SerialName("fecha_declaracion") val fechaDeclaracion: String,
    @SerialName("fecha_recogida") val fechaRecogida: String? = null,
    @SerialName("fecha_matadero") val fechaMatadero: String? = null,
    @SerialName("fecha_entrega") val fechaEntrega: String? = null,
    @SerialName("motivo_rechazo") val motivoRechazo: String? = null,
    @SerialName("observaciones") val observaciones: String? = null,
    @SerialName("peso_aproximado_kg") val pesoAproximadoKg: Float? = null
)
