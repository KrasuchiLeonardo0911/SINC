package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.MovimientoPendienteDao
import com.sinc.mobile.data.local.entities.MovimientoPendienteEntity
import com.sinc.mobile.data.network.api.MovimientoApiService
import com.sinc.mobile.data.network.dto.MovimientoRequest
import com.sinc.mobile.data.network.dto.MovimientosBatchRequest
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.Movimiento
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovimientoRepositoryImpl @Inject constructor(
    private val movimientoPendienteDao: MovimientoPendienteDao,
    private val movimientoApiService: MovimientoApiService,
    private val sessionManager: SessionManager
) : MovimientoRepository {

    override suspend fun saveMovimientoLocal(movimiento: MovimientoPendiente): Result<Unit> {
        return try {
            val entity = movimiento.toEntity()
            movimientoPendienteDao.insert(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMovimientosPendientes(): Flow<List<MovimientoPendiente>> {
        return movimientoPendienteDao.getAllMovimientosPendientes().map {
            it.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun syncMovimientosPendientes(): Result<Unit> {
        val token = sessionManager.getAuthToken()
            ?: return Result.failure(Exception("No hay token de autenticación disponible"))

        return try {
            val unsyncedEntities = movimientoPendienteDao.getUnsyncedMovimientos().first()

            if (unsyncedEntities.isEmpty()) {
                return Result.success(Unit)
            }

            // Group by unidad_productiva_id
            val groupedByUp = unsyncedEntities.groupBy { it.unidad_productiva_id }

            groupedByUp.forEach { (upId, movimientosForUp) ->
                val apiMovimientos = movimientosForUp.map { it.toApiRequest() }
                val batchRequest = MovimientosBatchRequest(upId, apiMovimientos)
                val response = movimientoApiService.saveMovimientos(batchRequest)

                if (response.isSuccessful) {
                    movimientosForUp.forEach { entity ->
                        movimientoPendienteDao.markAsSynced(entity.id)
                    }
                } else {
                    // Handle API error, maybe log it or return a specific failure
                    val errorBody = response.errorBody()?.string()
                    return@syncMovimientosPendientes Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveMovimiento(movimiento: Movimiento): Result<Unit> {
        // This function might be removed or refactored later if all movements go through local persistence first.
        // For now, it's a placeholder or for direct API calls if needed.
        return Result.failure(UnsupportedOperationException("Direct API save not implemented for MovimientoRepositoryImpl"))
    }
}

// Mappers
fun MovimientoPendiente.toEntity(): MovimientoPendienteEntity {
    return MovimientoPendienteEntity(
        id = this.id,
        unidad_productiva_id = this.unidadProductivaId,
        especie_id = this.especieId,
        categoria_id = this.categoriaId,
        raza_id = this.razaId,
        cantidad = this.cantidad,
        motivo_movimiento_id = this.motivoMovimientoId,
        destino_traslado = this.destinoTraslado,
        observaciones = this.observaciones,
        fecha_registro = this.fechaRegistro,
        sincronizado = this.sincronizado,
        fecha_creacion_local = LocalDateTime.now() // Always set current time on entity creation
    )
}

fun MovimientoPendienteEntity.toDomain(): MovimientoPendiente {
    return MovimientoPendiente(
        id = this.id,
        unidadProductivaId = this.unidad_productiva_id,
        especieId = this.especie_id,
        categoriaId = this.categoria_id,
        razaId = this.raza_id,
        cantidad = this.cantidad,
        motivoMovimientoId = this.motivo_movimiento_id,
        destinoTraslado = this.destino_traslado,
        observaciones = this.observaciones,
        fechaRegistro = this.fecha_registro,
        sincronizado = this.sincronizado
    )
}

fun MovimientoPendienteEntity.toApiRequest(): MovimientoRequest {
    return MovimientoRequest(
        especie_id = this.especie_id,
        categoria_id = this.categoria_id,
        raza_id = this.raza_id,
        cantidad = this.cantidad,
        motivo_movimiento_id = this.motivo_movimiento_id,
        destino_traslado = this.destino_traslado,
        // observaciones is not part of the API request body as per API_MOVIL.md
    )
}