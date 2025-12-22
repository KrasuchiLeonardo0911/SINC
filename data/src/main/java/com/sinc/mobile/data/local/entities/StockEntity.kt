package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sinc.mobile.data.model.UnidadProductivaStock

@Entity(tableName = "stock")
data class StockEntity(
    // Usamos un ID fijo para que esta tabla siempre tenga una sola fila (patrón Singleton)
    @PrimaryKey val id: Int = 1,
    val stockTotalGeneral: Int,
    // La lista completa de UPs con sus detalles se guarda como un solo texto JSON.
    val unidadesProductivas: List<UnidadProductivaStock>
)
