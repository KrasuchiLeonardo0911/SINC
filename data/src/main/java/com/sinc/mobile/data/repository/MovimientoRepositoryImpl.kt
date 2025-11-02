package com.sinc.mobile.data.repository

import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.MovimientoItemRequest
import com.sinc.mobile.data.network.dto.MovimientoRequest
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.Movimiento
import com.sinc.mobile.domain.repository.MovimientoRepository
import java.io.IOException
import javax.inject.Inject

class MovimientoRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : MovimientoRepository {

    override suspend fun saveMovimiento(movimiento: Movimiento): Result<Unit> {
        val authToken = sessionManager.getAuthToken()
        if (authToken == null) {
            return Result.failure(Exception("No hay token de autenticación disponible."))
        }

        return try {
            val movimientoItemRequest = MovimientoItemRequest(
                especieId = movimiento.especieId,
                categoriaId = movimiento.categoriaId,
                razaId = movimiento.razaId,
                cantidad = movimiento.cantidad,
                motivoMovimientoId = movimiento.motivoMovimientoId,
                destinoTraslado = movimiento.destinoTraslado
            )
            val request = MovimientoRequest(
                upId = 0, // This will be set in the ViewModel
                movimientos = listOf(movimientoItemRequest)
            )

            val response = apiService.saveMovimientos("Bearer $authToken", request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error de API al guardar movimiento: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
