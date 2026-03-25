package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.repository.MovimientoRepository
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ConfirmMovimientosUseCase @Inject constructor(
    private val movimientoRepository: MovimientoRepository,
    private val historialRepository: MovimientoHistorialRepository,
    private val catalogosRepository: CatalogosRepository
) {
    suspend operator fun invoke(): Result<Unit, GenericError> {
        return try {
            val pendientes = movimientoRepository.getMovimientosPendientes().first()
            if (pendientes.isEmpty()) return Result.Success(Unit)

            val catalogos = catalogosRepository.getMovimientoCatalogos().first()

            pendientes.forEach { pend ->
                val especie = catalogos.especies.find { it.id == pend.especieId }?.nombre ?: "Desconocido"
                val categoria = catalogos.categorias.find { it.id == pend.categoriaId }?.nombre ?: "Desconocido"
                val raza = catalogos.razas.find { it.id == pend.razaId }?.nombre ?: "Desconocido"
                val motivoObj = catalogos.motivosMovimiento.find { it.id == pend.motivoMovimientoId }
                val motivoNombre = motivoObj?.nombre ?: "Desconocido"
                val tipoMovimiento = motivoObj?.tipo ?: "Alta" // Default to Alta if not found
                
                // We don't have the UP name easily here without another lookup, 
                // but we can pass a generic one or fetch it. 
                // For the effective stock calculation, the IDs are what matters.
                
                val historialItem = MovimientoHistorial(
                    localId = 0,
                    id = null,
                    fechaRegistro = pend.fechaRegistro,
                    cantidad = pend.cantidad,
                    especie = especie,
                    especieId = pend.especieId,
                    categoria = categoria,
                    categoriaId = pend.categoriaId,
                    raza = raza,
                    razaId = pend.razaId,
                    motivo = motivoNombre,
                    motivoId = pend.motivoMovimientoId,
                    tipoMovimiento = tipoMovimiento,
                    unidadProductiva = "Campo Local", // Will be updated on sync
                    unidadProductivaId = pend.unidadProductivaId,
                    destinoTraslado = pend.destinoTraslado,
                    sincronizado = false
                )

                historialRepository.saveLocalMovimiento(historialItem)
                movimientoRepository.deleteMovimientoLocal(pend)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(GenericError("Error al confirmar movimientos: ${e.message}"))
        }
    }
}
