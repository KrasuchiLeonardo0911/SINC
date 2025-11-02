package com.sinc.mobile.data.repository

import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.CatalogosDto
import com.sinc.mobile.data.network.dto.CategoriaDto
import com.sinc.mobile.data.network.dto.EspecieDto
import com.sinc.mobile.data.network.dto.MotivoMovimientoDto
import com.sinc.mobile.data.network.dto.RazaDto
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.MotivoMovimiento
import com.sinc.mobile.domain.model.Raza
import com.sinc.mobile.domain.repository.CatalogosRepository
import java.io.IOException
import javax.inject.Inject

class CatalogosRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : CatalogosRepository {

    override suspend fun getCatalogos(): Result<Catalogos> {
        val authToken = sessionManager.getAuthToken()
        if (authToken == null) {
            return Result.failure(Exception("No hay token de autenticación disponible."))
        }

        return try {
            val response = apiService.getCatalogos("Bearer $authToken")
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    Result.success(dto.toDomain())
                } else {
                    Result.failure(Exception("El cuerpo de la respuesta de catálogos es nulo"))
                }
            } else {
                Result.failure(Exception("Error de API al obtener catálogos: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun CatalogosDto.toDomain(): Catalogos {
    return Catalogos(
        especies = this.especies.map { it.toDomain() },
        razas = this.razas.map { it.toDomain() },
        categorias = this.categorias.map { it.toDomain() },
        motivosMovimiento = this.motivosMovimiento.map { it.toDomain() }
    )
}

private fun EspecieDto.toDomain(): Especie = Especie(id, nombre)
private fun RazaDto.toDomain(): Raza = Raza(id, nombre, especieId)
private fun CategoriaDto.toDomain(): Categoria = Categoria(id, nombre, especieId)
private fun MotivoMovimientoDto.toDomain(): MotivoMovimiento = MotivoMovimiento(id, nombre, tipo)
