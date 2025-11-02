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
import android.util.Log
import com.sinc.mobile.data.local.dao.EspecieDao
import com.sinc.mobile.data.local.dao.RazaDao
import com.sinc.mobile.data.local.dao.CategoriaAnimalDao
import com.sinc.mobile.data.local.dao.MotivoMovimientoDao
import com.sinc.mobile.data.local.entities.EspecieEntity
import com.sinc.mobile.data.local.entities.RazaEntity
import com.sinc.mobile.data.local.entities.CategoriaAnimalEntity
import com.sinc.mobile.data.local.entities.MotivoMovimientoEntity

class CatalogosRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val especieDao: EspecieDao,
    private val razaDao: RazaDao,
    private val categoriaAnimalDao: CategoriaAnimalDao,
    private val motivoMovimientoDao: MotivoMovimientoDao
) : CatalogosRepository {

    override suspend fun getCatalogos(): Result<Catalogos> {
        val authToken = sessionManager.getAuthToken()
        if (authToken == null) {
            return Result.failure(Exception("No hay token de autenticación disponible."))
        }

        // This method will now fetch from local DB first, or trigger sync if needed.
        // For now, we'll keep it as is, but the syncCatalogos will be the primary way to update.
        // We will refine this logic later to implement the SSOT pattern.
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

    override suspend fun syncCatalogos(): Result<Unit> {
        Log.d("CatalogosRepo", "Iniciando syncCatalogos")
        val authToken = sessionManager.getAuthToken()
        if (authToken == null) {
            Log.d("CatalogosRepo", "No hay token de autenticación disponible.")
            return Result.failure(Exception("No hay token de autenticación disponible para sincronizar catálogos."))
        }

        return try {
            Log.d("CatalogosRepo", "Realizando llamada a la API para catálogos")
            val response = apiService.getCatalogos("Bearer $authToken")
            Log.d("CatalogosRepo", "Respuesta de la API: ${response.code()}")
            if (response.isSuccessful) {
                val catalogosDto = response.body()
                if (catalogosDto != null) {
                    // Clear existing data
                    especieDao.clearAll()
                    razaDao.clearAll()
                    categoriaAnimalDao.clearAll()
                    motivoMovimientoDao.clearAll()

                    // Insert new data
                    especieDao.insertAll(catalogosDto.especies.map { it.toEntity() })
                    razaDao.insertAll(catalogosDto.razas.map { it.toEntity() })
                    categoriaAnimalDao.insertAll(catalogosDto.categorias.map { it.toEntity() })
                    motivoMovimientoDao.insertAll(catalogosDto.motivosMovimiento.map { it.toEntity() })

                    Log.d("CatalogosRepo", "Sincronización exitosa")
                    Result.success(Unit)
                } else {
                    Log.d("CatalogosRepo", "Sincronización fallida: El cuerpo de la respuesta de catálogos es nulo durante la sincronización.")
                    Result.failure(Exception("El cuerpo de la respuesta de catálogos es nulo durante la sincronización."))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                Log.d("CatalogosRepo", "Sincronización fallida: Error de API al sincronizar catálogos: ${response.code()} - $errorBody")
                Result.failure(Exception("Error de API al sincronizar catálogos: ${response.code()} - $errorBody"))
            }
        } catch (e: IOException) {
            Log.e("CatalogosRepositoryImpl", "IOException in syncCatalogos", e)
            Log.d("CatalogosRepo", "Sincronización fallida: Error de red al sincronizar catálogos: ${e.message}")
            Result.failure(Exception("Error de red al sincronizar catálogos: ${e.message}"))
        } catch (e: Exception) {
            Log.e("CatalogosRepositoryImpl", "Exception in syncCatalogos", e)
            Log.d("CatalogosRepo", "Sincronización fallida: Error desconocido al sincronizar catálogos: ${e.message}")
            Result.failure(Exception("Error desconocido al sincronizar catálogos: ${e.message}"))
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

// Mapping functions from DTO to Entity for local persistence
private fun EspecieDto.toEntity(): EspecieEntity = EspecieEntity(id, nombre)
private fun RazaDto.toEntity(): RazaEntity = RazaEntity(id = id, especie_id = especieId, nombre = nombre)
private fun CategoriaDto.toEntity(): CategoriaAnimalEntity = CategoriaAnimalEntity(id = id, especie_id = especieId, nombre = nombre)
private fun MotivoMovimientoDto.toEntity(): MotivoMovimientoEntity = MotivoMovimientoEntity(id, nombre, tipo)
