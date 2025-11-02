package com.sinc.mobile.data.repository

import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.UnidadProductivaDto
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import java.io.IOException
import javax.inject.Inject

import com.sinc.mobile.data.session.SessionManager

class UnidadProductivaRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : UnidadProductivaRepository {

    override suspend fun getUnidadesProductivas(): Result<List<UnidadProductiva>> {
        val authToken = sessionManager.getAuthToken()
        if (authToken == null) {
            return Result.failure(Exception("No hay token de autenticación disponible."))
        }
        return try {
            val response = apiService.getUnidadesProductivas("Bearer $authToken")
            if (response.isSuccessful) {
                val dtos = response.body()
                if (dtos != null) {
                    val models = dtos.map { it.toDomain() }
                    Result.success(models)
                } else {
                    Result.failure(Exception("El cuerpo de la respuesta es nulo"))
                }
            } else {
                // Aquí podríamos usar el handleError que ya tenemos en AuthRepositoryImpl
                Result.failure(Exception("Error de API: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun UnidadProductivaDto.toDomain(): UnidadProductiva {
    return UnidadProductiva(
        id = this.id,
        nombre = this.nombre
    )
}
