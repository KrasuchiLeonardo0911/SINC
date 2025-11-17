package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.CatalogosDao
import com.sinc.mobile.data.local.entities.*
import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.*
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.*
import com.sinc.mobile.domain.repository.CatalogosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.IOException
import javax.inject.Inject
import android.util.Log

class CatalogosRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val catalogosDao: CatalogosDao
) : CatalogosRepository {

    override fun getCatalogos(): Flow<Catalogos> {
        val combinedFlows1 = combine(
            catalogosDao.getAllEspecies(),
            catalogosDao.getAllRazas(),
            catalogosDao.getAllCategorias(),
            catalogosDao.getAllMotivosMovimiento(),
            catalogosDao.getAllMunicipios()
        ) { especies, razas, categorias, motivos, municipios ->
            Triple(especies, razas, Triple(categorias, motivos, municipios))
        }

        val combinedFlows2 = combine(
            catalogosDao.getAllCondicionesTenencia(),
            catalogosDao.getAllFuentesAgua(),
            catalogosDao.getAllTiposSuelo(),
            catalogosDao.getAllTiposPasto()
        ) { condiciones, fuentes, suelos, pastos ->
            Pair(condiciones, Triple(fuentes, suelos, pastos))
        }

        return combine(combinedFlows1, combinedFlows2) { triple1, pair1 ->
            val (especies, razas, triple2) = triple1
            val (categorias, motivos, municipios) = triple2
            val (condiciones, triple3) = pair1
            val (fuentes, suelos, pastos) = triple3

            Catalogos(
                especies = especies.map { it.toDomain() },
                razas = razas.map { it.toDomain() },
                categorias = categorias.map { it.toDomain() },
                motivosMovimiento = motivos.map { it.toDomain() },
                municipios = municipios.map { it.toDomain() },
                condicionesTenencia = condiciones.map { it.toDomain() },
                fuentesAgua = fuentes.map { it.toDomain() },
                tiposSuelo = suelos.map { it.toDomain() },
                tiposPasto = pastos.map { it.toDomain() }
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
                    // Insert new data, checking for nulls
                    catalogosDto.especies?.let { catalogosDao.insertAllEspecies(it.map { it.toEntity() }) }
                    catalogosDto.razas?.let { catalogosDao.insertAllRazas(it.map { it.toEntity() }) }
                    catalogosDto.categorias?.let { catalogosDao.insertAllCategorias(it.map { it.toEntity() }) }
                    catalogosDto.motivosMovimiento?.let { catalogosDao.insertAllMotivosMovimiento(it.map { it.toEntity() }) }
                    catalogosDto.municipios?.let { catalogosDao.insertAllMunicipios(it.map { it.toEntity() }) }
                    catalogosDto.condicionesTenencia?.let { catalogosDao.insertAllCondicionesTenencia(it.map { it.toEntity() }) }
                    catalogosDto.fuentesAgua?.let { catalogosDao.insertAllFuentesAgua(it.map { it.toEntity() }) }
                    catalogosDto.tiposSuelo?.let { catalogosDao.insertAllTiposSuelo(it.map { it.toEntity() }) }
                    catalogosDto.tiposPasto?.let { catalogosDao.insertAllTiposPasto(it.map { it.toEntity() }) }

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
private fun MunicipioEntity.toDomain(): Municipio = Municipio(id, nombre)
private fun CondicionTenenciaEntity.toDomain(): CondicionTenencia = CondicionTenencia(id, nombre)
private fun FuenteAguaEntity.toDomain(): FuenteAgua = FuenteAgua(id, nombre)
private fun TipoSueloEntity.toDomain(): TipoSuelo = TipoSuelo(id, nombre)
private fun TipoPastoEntity.toDomain(): TipoPasto = TipoPasto(id, nombre)

// Mappers from DTO to Entity
private fun EspecieDto.toEntity(): EspecieEntity = EspecieEntity(id, nombre)
private fun RazaDto.toEntity(): RazaEntity = RazaEntity(id = id, especie_id = especieId, nombre = nombre)
private fun CategoriaDto.toEntity(): CategoriaAnimalEntity = CategoriaAnimalEntity(id = id, especie_id = especieId, nombre = nombre)
private fun MotivoMovimientoDto.toEntity(): MotivoMovimientoEntity = MotivoMovimientoEntity(id, nombre, tipo)
private fun MunicipioDto.toEntity(): MunicipioEntity = MunicipioEntity(id, nombre)
private fun CondicionTenenciaDto.toEntity(): CondicionTenenciaEntity = CondicionTenenciaEntity(id, nombre)
private fun FuenteAguaDto.toEntity(): FuenteAguaEntity = FuenteAguaEntity(id, nombre)
private fun TipoSueloDto.toEntity(): TipoSueloEntity = TipoSueloEntity(id, nombre)
private fun TipoPastoDto.toEntity(): TipoPastoEntity = TipoPastoEntity(id, nombre)
