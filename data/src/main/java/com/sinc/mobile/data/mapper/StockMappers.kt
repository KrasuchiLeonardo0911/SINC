package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.local.entities.StockEntity
import com.sinc.mobile.data.model.DesgloseStock
import com.sinc.mobile.data.model.DesgloseStockDto
import com.sinc.mobile.data.model.EspecieStock
import com.sinc.mobile.data.model.EspecieStockDto
import com.sinc.mobile.data.model.StockDataDto
import com.sinc.mobile.data.model.UnidadProductivaStock
import com.sinc.mobile.data.model.UnidadProductivaStockDto
import com.sinc.mobile.domain.model.Stock as DomainStock
import com.sinc.mobile.domain.model.UnidadProductivaStock as DomainUnidadProductivaStock
import com.sinc.mobile.domain.model.EspecieStock as DomainEspecieStock
import com.sinc.mobile.domain.model.DesgloseStock as DomainDesgloseStock

// --- DTO to Data Model (for StockEntity) ---
fun DesgloseStockDto.toDataModel(): DesgloseStock {
    return DesgloseStock(
        categoria = this.categoria,
        raza = this.raza,
        cantidad = this.cantidad
    )
}

fun EspecieStockDto.toDataModel(): EspecieStock {
    return EspecieStock(
        nombre = this.nombre,
        stockTotal = this.stockTotal,
        desglose = this.desglose.map { it.toDataModel() }
    )
}

fun UnidadProductivaStockDto.toDataModel(): UnidadProductivaStock {
    return UnidadProductivaStock(
        id = this.id,
        nombre = this.nombre,
        stockTotal = this.stockTotal,
        especies = this.especies.map { it.toDataModel() }
    )
}

fun StockDataDto.toEntity(): StockEntity {
    return StockEntity(
        stockTotalGeneral = this.stockTotalGeneral,
        unidadesProductivas = this.unidadesProductivas.map { it.toDataModel() }
    )
}


// --- Data Model to Domain Model ---
fun DesgloseStock.toDomain(): DomainDesgloseStock {
    return DomainDesgloseStock(
        categoria = this.categoria,
        raza = this.raza,
        cantidad = this.cantidad
    )
}

fun EspecieStock.toDomain(): DomainEspecieStock {
    return DomainEspecieStock(
        nombre = this.nombre,
        stockTotal = this.stockTotal,
        desglose = this.desglose.map { it.toDomain() }
    )
}

fun UnidadProductivaStock.toDomain(): DomainUnidadProductivaStock {
    return DomainUnidadProductivaStock(
        id = this.id,
        nombre = this.nombre,
        stockTotal = this.stockTotal,
        especies = this.especies.map { it.toDomain() }
    )
}

fun StockEntity.toDomain(): DomainStock {
    return DomainStock(
        stockTotalGeneral = this.stockTotalGeneral,
        unidadesProductivas = this.unidadesProductivas.map { it.toDomain() }
    )
}
