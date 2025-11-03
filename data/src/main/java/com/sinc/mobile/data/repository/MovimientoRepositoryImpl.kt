package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.MovimientoPendienteDao
import com.sinc.mobile.data.local.entities.MovimientoPendienteEntity
import com.sinc.mobile.data.network.api.MovimientoApiService
import com.sinc.mobile.data.network.dto.MovimientoRequest
import com.sinc.mobile.data.network.dto.MovimientosBatchRequest
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class MovimientoRepositoryImpl @Inject constructor(
    private val movimientoPendienteDao: MovimientoPendienteDao,
    private val movimientoApiService: MovimientoApiService,
    private val sessionManager: SessionManager
) : MovimientoRepository {

    override suspend fun saveMovimientoLocal(movimiento: MovimientoPendiente): Result<Unit> {
        return try {
            movimientoPendienteDao.insert(movimiento.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMovimientosPendientes(): Flow<List<MovimientoPendiente>> {
        return movimientoPendienteDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMovimientosPendientes(): Result<Unit> {
        return try {
            val pendientes = getMovimientosPendientes().first()
            if (pendientes.isEmpty()) {
                return Result.success(Unit)
            }

            val groupedByUp = pendientes.groupBy { it.unidadProductivaId }

            for ((upId, movimientos) in groupedByUp) {
                val batchRequest = MovimientosBatchRequest(
                    upId = upId,
                    movimientos = movimientos.map { it.toApiRequest() }
                )

                val response = movimientoApiService.saveMovimientos(batchRequest)

                if (response.isSuccessful) {
                    movimientos.forEach { movimiento ->
                        movimientoPendienteDao.update(movimiento.copy(sincronizado = true).toEntity())
                    }
                } else {
                    // If one batch fails, we stop and return failure. 
                    // A more robust implementation could collect failures and continue.
                    return Result.failure(Exception("Error al sincronizar movimientos para la UP $upId"))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMovimientoLocal(movimiento: MovimientoPendiente): Result<Unit> {
        return try {
            movimientoPendienteDao.delete(movimiento.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        sincronizado = this.sincronizado
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

fun MovimientoPendiente.toApiRequest(): MovimientoRequest {
    return MovimientoRequest(
        especie_id = this.especieId,
        categoria_id = this.categoriaId,
        raza_id = this.razaId,
        cantidad = this.cantidad,
        motivo_movimiento_id = this.motivoMovimientoId,
        destino_traslado = this.destinoTraslado
    )
}