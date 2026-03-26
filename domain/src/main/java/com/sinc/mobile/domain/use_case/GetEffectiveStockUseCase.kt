package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.DesgloseStock
import com.sinc.mobile.domain.model.EspecieStock
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductivaStock
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetEffectiveStockUseCase @Inject constructor(
    private val stockRepository: StockRepository,
    private val movimientoHistorialRepository: MovimientoHistorialRepository
) {
    operator fun invoke(): Flow<Stock> {
        return combine(
            stockRepository.getStock(),
            movimientoHistorialRepository.getMovimientos()
        ) { stockFromServer, movimientosHistorial ->
            
            // Only consider unsynced movements for effective calculation
            val unsyncedMovements = movimientosHistorial.filter { !it.sincronizado }
            
            if (unsyncedMovements.isEmpty()) {
                return@combine stockFromServer
            }

            // Create a mutable copy of the server stock structure to recalculate
            var currentStock = stockFromServer

            unsyncedMovements.forEach { mov ->
                currentStock = applyMovementToStock(currentStock, mov)
            }

            currentStock
        }
    }

    private fun applyMovementToStock(
        stock: Stock,
        mov: com.sinc.mobile.domain.model.MovimientoHistorial
    ): Stock {
        val isAlta = mov.tipoMovimiento.equals("Alta", ignoreCase = true)
        val amount = if (isAlta) mov.cantidad else -mov.cantidad

        // 1. Update or Add the specific UnidadProductiva
        val unitExists = stock.unidadesProductivas.any { it.id == mov.unidadProductivaId }
        val updatedUnits = if (unitExists) {
            stock.unidadesProductivas.map { unit ->
                if (unit.id == mov.unidadProductivaId) {
                    updateUnitWithMovement(unit, mov, amount)
                } else {
                    unit
                }
            }
        } else {
            // Add new Unit if it doesn't exist (only if it's an Alta or we allow starting from 0)
            val newUnit = UnidadProductivaStock(
                id = mov.unidadProductivaId,
                nombre = mov.unidadProductiva,
                stockTotal = 0,
                especies = emptyList()
            )
            stock.unidadesProductivas + updateUnitWithMovement(newUnit, mov, amount)
        }

        // Recalculate global total stock
        return Stock(
            unidadesProductivas = updatedUnits,
            stockTotalGeneral = updatedUnits.sumOf { it.stockTotal }
        )
    }

    private fun updateUnitWithMovement(
        unit: UnidadProductivaStock,
        mov: com.sinc.mobile.domain.model.MovimientoHistorial,
        amount: Int
    ): UnidadProductivaStock {
        // 2. Update or Add the specific Especie within the unit
        val especieExists = unit.especies.any { it.nombre.equals(mov.especie, ignoreCase = true) }
        val updatedEspecies = if (especieExists) {
            unit.especies.map { especie ->
                if (especie.nombre.equals(mov.especie, ignoreCase = true)) {
                    updateEspecieWithMovement(especie, mov, amount)
                } else {
                    especie
                }
            }
        } else {
            val newEspecie = EspecieStock(
                nombre = mov.especie,
                stockTotal = 0,
                desglose = emptyList()
            )
            unit.especies + updateEspecieWithMovement(newEspecie, mov, amount)
        }

        return unit.copy(
            especies = updatedEspecies,
            stockTotal = updatedEspecies.sumOf { it.stockTotal }
        )
    }

    private fun updateEspecieWithMovement(
        especie: EspecieStock,
        mov: com.sinc.mobile.domain.model.MovimientoHistorial,
        amount: Int
    ): EspecieStock {
        // 3. Update or Add the specific Desglose within the especie
        val desgloseExists = especie.desglose.any { 
            it.categoria.equals(mov.categoria, ignoreCase = true) && 
            it.raza.equals(mov.raza, ignoreCase = true) 
        }

        val updatedDesglose = if (desgloseExists) {
            especie.desglose.map { item ->
                if (item.categoria.equals(mov.categoria, ignoreCase = true) && 
                    item.raza.equals(mov.raza, ignoreCase = true)) {
                    item.copy(cantidad = (item.cantidad + amount).coerceAtLeast(0))
                } else {
                    item
                }
            }
        } else {
            val newItem = DesgloseStock(
                categoria = mov.categoria,
                raza = mov.raza,
                cantidad = amount.coerceAtLeast(0)
            )
            especie.desglose + newItem
        }

        return especie.copy(
            desglose = updatedDesglose,
            stockTotal = updatedDesglose.sumOf { it.cantidad }
        )
    }
}
