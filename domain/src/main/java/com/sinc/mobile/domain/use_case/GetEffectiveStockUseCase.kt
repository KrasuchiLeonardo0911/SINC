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

        // 1. Find or update the specific UnidadProductiva
        val updatedUnits = stock.unidadesProductivas.map { unit ->
            if (unit.id == mov.unidadProductivaId) {
                
                // 2. Find or update the specific Especie within the unit
                val updatedEspecies = unit.especies.map { especie ->
                    if (especie.nombre.equals(mov.especie, ignoreCase = true)) {
                        
                        // 3. Find or update the specific Desglose within the especie
                        val updatedDesglose = especie.desglose.map { item ->
                            if (item.categoria.equals(mov.categoria, ignoreCase = true) && 
                                item.raza.equals(mov.raza, ignoreCase = true)) {
                                item.copy(cantidad = (item.cantidad + amount).coerceAtLeast(0))
                            } else {
                                item
                            }
                        }

                        // Recalculate total for this specie
                        especie.copy(
                            desglose = updatedDesglose,
                            stockTotal = updatedDesglose.sumOf { it.cantidad }
                        )
                    } else {
                        especie
                    }
                }

                // Recalculate total for this unit
                unit.copy(
                    especies = updatedEspecies,
                    stockTotal = updatedEspecies.sumOf { it.stockTotal }
                )
            } else {
                unit
            }
        }

        // Recalculate global total stock
        return Stock(
            unidadesProductivas = updatedUnits,
            stockTotalGeneral = updatedUnits.sumOf { it.stockTotal }
        )
    }
}
