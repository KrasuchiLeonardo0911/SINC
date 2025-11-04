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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CatalogosRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val especieDao: EspecieDao,
    private val razaDao: RazaDao,
    private val categoriaAnimalDao: CategoriaAnimalDao,
    private val motivoMovimientoDao: MotivoMovimientoDao
) : CatalogosRepository {

    override fun getCatalogos(): Flow<Catalogos> {
        return combine(
            especieDao.getAllEspecies(),
            razaDao.getAllRazas(),
            categoriaAnimalDao.getAllCategorias(),
            motivoMovimientoDao.getAllMotivosMovimiento()
        ) { especies, razas, categorias, motivos ->
            Catalogos(
                especies = especies.map { it.toDomain() },
                razas = razas.map { it.toDomain() },
                categorias = categorias.map { it.toDomain() },
                motivosMovimiento = motivos.map { it.toDomain() }
            )
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
            val response = apiService.getCatalogos()
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

// Mappers from Entity to Domain
private fun EspecieEntity.toDomain(): Especie = Especie(id, nombre)
private fun RazaEntity.toDomain(): Raza = Raza(id, nombre, especie_id)
private fun CategoriaAnimalEntity.toDomain(): Categoria = Categoria(id, nombre, especie_id)
private fun MotivoMovimientoEntity.toDomain(): MotivoMovimiento = MotivoMovimiento(id, nombre, tipo)

// Mappers from DTO to Entity
private fun EspecieDto.toEntity(): EspecieEntity = EspecieEntity(id, nombre)
private fun RazaDto.toEntity(): RazaEntity = RazaEntity(id = id, especie_id = especieId, nombre = nombre)
private fun CategoriaDto.toEntity(): CategoriaAnimalEntity = CategoriaAnimalEntity(id = id, especie_id = especieId, nombre = nombre)
private fun MotivoMovimientoDto.toEntity(): MotivoMovimientoEntity = MotivoMovimientoEntity(id, nombre, tipo)
