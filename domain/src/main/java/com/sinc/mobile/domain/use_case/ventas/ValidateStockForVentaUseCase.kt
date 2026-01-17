package com.sinc.mobile.domain.use_case.ventas

import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.repository.StockRepository
import com.sinc.mobile.domain.repository.VentasRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ValidateStockForVentaUseCase @Inject constructor(
    private val stockRepository: StockRepository,
    private val ventasRepository: VentasRepository,
    private val catalogosRepository: CatalogosRepository
) {
    /**
     * Valida si el stock es suficiente considerando las declaraciones pendientes.
     */
    suspend operator fun invoke(
        unidadProductivaId: Int,
        especieId: Int,
        razaId: Int,
        categoriaAnimalId: Int,
        cantidadSolicitada: Int
    ): ValidationResult {
        
        // 1. Obtener nombres del Catálogo
        val catalogos = catalogosRepository.getCatalogos().first()
        
        val especie = catalogos.especies.find { it.id == especieId }
        val raza = catalogos.razas.find { it.id == razaId }
        val categoria = catalogos.categorias.find { it.id == categoriaAnimalId }

        if (especie == null || raza == null || categoria == null) {
             return ValidationResult.Error("Datos de catálogo no encontrados. Intente sincronizar.")
        }

        // 2. Obtener Stock Actual
        val stock = stockRepository.getStock().first()
        
        // 3. Buscar Cantidad en Stock (Si no existe la UP o el desglose, asumimos 0)
        val stockReal = stock.unidadesProductivas.find { it.id == unidadProductivaId }
            ?.especies?.find { it.nombre.equals(especie.nombre, ignoreCase = true) }
            ?.desglose?.find { 
                it.categoria.equals(categoria.nombre, ignoreCase = true) && 
                it.raza.equals(raza.nombre, ignoreCase = true) 
            }?.cantidad ?: 0

        // 4. Obtener Declaraciones Pendientes
        val stockPendiente = ventasRepository.getPendingQuantity(
            unidadProductivaId, especieId, razaId, categoriaAnimalId
        )

        val disponible = stockReal - stockPendiente

        return if (cantidadSolicitada <= disponible) {
            ValidationResult.Success
        } else {
            ValidationResult.InsufficientStock(stockReal, stockPendiente)
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class InsufficientStock(val real: Int, val pendiente: Int) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}