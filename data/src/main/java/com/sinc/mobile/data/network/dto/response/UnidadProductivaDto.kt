package com.sinc.mobile.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PivotDto(
    @SerialName("productor_id") val productorId: Int? = null,
    @SerialName("unidad_productiva_id") val unidadProductivaId: Int? = null,
    @SerialName("condicion_tenencia_id") val condicionTenenciaId: Int? = null // Añadido aquí
)

@Serializable
data class UnidadProductivaDto(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String?,

    // --- Campos presentes en el JSON ---
    @SerialName("campo_id") val campoId: Int? = null,
    @SerialName("identificador_local") val identificadorLocal: String? = null,
    @SerialName("tipo_identificador_id") val tipoIdentificadorId: Int? = null,
    @SerialName("activo") val activo: Int? = null,
    @SerialName("completo") val completo: Int? = null,
    @SerialName("superficie") val superficie: String? = null,
    @SerialName("municipio_id") val municipioId: Int? = null,
    @SerialName("paraje_id") val parajeId: Int? = null,
    @SerialName("habita") val habita: Int? = null,
    @SerialName("latitud") val latitud: String?,
    @SerialName("longitud") val longitud: String?,
    @SerialName("agua_humano_fuente_id") val fuenteAguaId: Int? = null,
    @SerialName("agua_humano_en_casa") val aguaHumanoEnCasa: Int? = null,
    @SerialName("agua_humano_distancia") val aguaHumanoDistancia: Int? = null,
    @SerialName("agua_animal_fuente_id") val aguaAnimalFuenteId: Int? = null,
    @SerialName("agua_animal_distancia") val aguaAnimalDistancia: Int? = null,
    @SerialName("tipo_pasto_predominante_id") val tipoPastoId: Int? = null,
    @SerialName("tipo_suelo_predominante_id") val tipoSueloId: Int? = null,
    @SerialName("forrajeras_predominante") val forrajerasPredominante: Int? = null,
    @SerialName("observaciones") val observaciones: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("pivot") val pivot: PivotDto? = null
)
