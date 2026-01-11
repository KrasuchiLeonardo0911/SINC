package com.sinc.mobile.data.repository

import android.util.Log
import com.sinc.mobile.data.local.dao.*
import com.sinc.mobile.data.local.entities.*
import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.*
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.*
import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.model.DomainGeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.*
import java.io.IOException
import javax.inject.Inject

class CatalogosRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val especieDao: EspecieDao,
    private val razaDao: RazaDao,
    private val categoriaAnimalDao: CategoriaAnimalDao,
    private val motivoMovimientoDao: MotivoMovimientoDao,
    private val municipioDao: MunicipioDao,
    private val condicionTenenciaDao: CondicionTenenciaDao,
    private val fuenteAguaDao: FuenteAguaDao,
    private val tipoSueloDao: TipoSueloDao,
    private val tipoPastoDao: TipoPastoDao,
    private val json: Json // Inject Kotlinx.serialization Json
) : CatalogosRepository {

    // Mappers from Entity to Domain
    private fun EspecieEntity.toDomain(): Especie = Especie(id, nombre)
    private fun RazaEntity.toDomain(): Raza = Raza(id, nombre, especie_id)
    private fun CategoriaAnimalEntity.toDomain(): Categoria = Categoria(id, nombre, especie_id)
    private fun MotivoMovimientoEntity.toDomain(): MotivoMovimiento = MotivoMovimiento(id, nombre, tipo)

    private fun MunicipioEntity.toDomain(): Municipio {
        val centroide = if (latitud != null && longitud != null) {
            DomainGeoPoint(latitud, longitud)
        } else null

        val poligono = geojson_boundary?.let { jsonString ->
            try {
                val geoJson = json.parseToJsonElement(jsonString).jsonObject
                val type = geoJson["type"]?.jsonPrimitive?.content
                val coordinates = geoJson["coordinates"] as? JsonArray

                if (coordinates == null) {
                    Log.e("CatalogosRepo", "Coordinates are null for municipio $nombre")
                    return@let null
                }

                val ring: JsonArray? = when (type) {
                    "Polygon" -> {
                        // For Polygon, coordinates are [ ring, hole1, ... ]
                        // We take the first element, which is the exterior ring.
                        coordinates.getOrNull(0) as? JsonArray
                    }
                    "MultiPolygon" -> {
                        // For MultiPolygon, coordinates are [ polygon1, polygon2, ... ]
                        // where a polygon is [ ring, hole1, ... ]
                        // We take the first ring of the first polygon.
                        val firstPolygon = coordinates.getOrNull(0) as? JsonArray
                        firstPolygon?.getOrNull(0) as? JsonArray
                    }
                    else -> {
                        Log.e("CatalogosRepo", "Unknown GeoJSON type '$type' for municipio $nombre")
                        null
                    }
                }

                ring?.mapNotNull { pointElement ->
                    val point = pointElement as? JsonArray
                    if (point != null && point.size >= 2) {
                        // GeoJSON standard is [longitude, latitude]
                        DomainGeoPoint(
                            latitude = point[1].jsonPrimitive.double,
                            longitude = point[0].jsonPrimitive.double
                        )
                    } else {
                        Log.w("CatalogosRepo", "Invalid point in ring for municipio $nombre: $pointElement")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("CatalogosRepo", "Failed to parse geojson for municipio $nombre: ${e.message}")
                Log.d("CatalogosRepo", "Problematic JSON: $jsonString")
                null
            }
        }
        return Municipio(id, nombre, centroide, poligono)
    }
    private fun CondicionTenenciaEntity.toDomain(): CondicionTenencia = CondicionTenencia(id, nombre)
    private fun FuenteAguaEntity.toDomain(): FuenteAgua = FuenteAgua(id, nombre)
    private fun TipoSueloEntity.toDomain(): TipoSuelo = TipoSuelo(id, nombre)
    private fun TipoPastoEntity.toDomain(): TipoPasto = TipoPasto(id, nombre)

    // Mappers from DTO to Entity
    private fun EspecieDto.toEntity(): EspecieEntity = EspecieEntity(id, nombre)
    private fun RazaDto.toEntity(): RazaEntity = RazaEntity(id = id, especie_id = especieId, nombre = nombre)
    private fun CategoriaDto.toEntity(): CategoriaAnimalEntity = CategoriaAnimalEntity(id = id, especie_id = especieId, nombre = nombre)
    private fun MotivoMovimientoDto.toEntity(): MotivoMovimientoEntity = MotivoMovimientoEntity(id, nombre, tipo)
    private fun MunicipioDto.toEntity(): MunicipioEntity = MunicipioEntity(id, nombre, latitud, longitud, geojsonBoundary)
    private fun CondicionTenenciaDto.toEntity(): CondicionTenenciaEntity = CondicionTenenciaEntity(id, nombre)
    private fun FuenteAguaDto.toEntity(): FuenteAguaEntity = FuenteAguaEntity(id, nombre)
    private fun TipoSueloDto.toEntity(): TipoSueloEntity = TipoSueloEntity(id, nombre)
    private fun TipoPastoDto.toEntity(): TipoPastoEntity = TipoPastoEntity(id, nombre)

    override fun getCatalogos(): Flow<Catalogos> {
        return combine(
            especieDao.getAllEspecies(),
            razaDao.getAllRazas(),
            categoriaAnimalDao.getAllCategorias(),
            motivoMovimientoDao.getAllMotivosMovimiento(),
            municipioDao.getAllMunicipios(),
            condicionTenenciaDao.getAllCondicionesTenencia(),
            fuenteAguaDao.getAllFuentesAgua(),
            tipoSueloDao.getAllTiposSuelo(),
            tipoPastoDao.getAllTiposPasto()
        ) { results ->
            @Suppress("UNCHECKED_CAST")
            Catalogos(
                especies = (results[0] as List<EspecieEntity>).map { it.toDomain() },
                razas = (results[1] as List<RazaEntity>).map { it.toDomain() },
                categorias = (results[2] as List<CategoriaAnimalEntity>).map { it.toDomain() },
                motivosMovimiento = (results[3] as List<MotivoMovimientoEntity>).map { it.toDomain() },
                municipios = (results[4] as List<MunicipioEntity>).map { it.toDomain() },
                condicionesTenencia = (results[5] as List<CondicionTenenciaEntity>).map { it.toDomain() },
                fuentesAgua = (results[6] as List<FuenteAguaEntity>).map { it.toDomain() },
                tiposSuelo = (results[7] as List<TipoSueloEntity>).map { it.toDomain() },
                tiposPasto = (results[8] as List<TipoPastoEntity>).map { it.toDomain() }
            )
        }
    }

    override fun getMovimientoCatalogos(): Flow<Catalogos> {
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
                motivosMovimiento = motivos.map { it.toDomain() },
                // Return empty lists for unused catalogs
                municipios = emptyList(),
                condicionesTenencia = emptyList(),
                fuentesAgua = emptyList(),
                tiposSuelo = emptyList(),
                tiposPasto = emptyList()
            )
        }
    }

    override suspend fun syncCatalogos(): com.sinc.mobile.domain.util.Result<Unit, com.sinc.mobile.domain.util.Error> {
        Log.d("CatalogosRepo", "Iniciando syncCatalogos")
        val authToken = sessionManager.getAuthToken()
            ?: return com.sinc.mobile.domain.util.Result.Failure(GenericError("No hay token de autenticación disponible para sincronizar catálogos."))

        return try {
            Log.d("CatalogosRepo", "Realizando llamada a la API para catálogos")
            val response = apiService.getCatalogos()
            Log.d("CatalogosRepo", "Respuesta de la API: ${response.code()}")
            if (response.isSuccessful) {
                val catalogosDto = response.body()
                if (catalogosDto != null) {
                    // Insert new data, checking for nulls
                    catalogosDto.especies?.let { especieDao.insertAllEspecies(it.map { it.toEntity() }) }
                    catalogosDto.razas?.let { razaDao.insertAllRazas(it.map { it.toEntity() }) }
                    catalogosDto.categorias?.let { categoriaAnimalDao.insertAllCategorias(it.map { it.toEntity() }) }
                    catalogosDto.motivosMovimiento?.let { motivoMovimientoDao.insertAllMotivosMovimiento(it.map { it.toEntity() }) }
                    catalogosDto.municipios?.let { municipioDao.insertAllMunicipios(it.map { it.toEntity() }) }
                    catalogosDto.condicionesTenencia?.let { condicionTenenciaDao.insertAllCondicionesTenencia(it.map { it.toEntity() }) }
                    catalogosDto.fuentesAgua?.let { fuenteAguaDao.insertAllFuentesAgua(it.map { it.toEntity() }) }
                    catalogosDto.tiposSuelo?.let { tipoSueloDao.insertAllTiposSuelo(it.map { it.toEntity() }) }
                    catalogosDto.tiposPasto?.let { tipoPastoDao.insertAllTiposPasto(it.map { it.toEntity() }) }

                    Log.d("CatalogosRepo", "Sincronización exitosa")
                    com.sinc.mobile.domain.util.Result.Success(Unit)
                } else {
                    Log.d("CatalogosRepo", "Sincronización fallida: El cuerpo de la respuesta de catálogos es nulo durante la sincronización.")
                    com.sinc.mobile.domain.util.Result.Failure(GenericError("El cuerpo de la respuesta de catálogos es nulo durante la sincronización."))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                Log.d("CatalogosRepo", "Sincronización fallida: Error de API al sincronizar catálogos: ${response.code()} - $errorBody")
                com.sinc.mobile.domain.util.Result.Failure(GenericError("Error de API al sincronizar catálogos: ${response.code()} - $errorBody"))
            }
        } catch (e: IOException) {
            Log.e("CatalogosRepositoryImpl", "IOException in syncCatalogos", e)
            Log.d("CatalogosRepo", "Sincronización fallida: Error de red al sincronizar catálogos: ${e.message}")
            com.sinc.mobile.domain.util.Result.Failure(GenericError("Error de red al sincronizar catálogos: ${e.message}"))
        } catch (e: Exception) {
            Log.e("CatalogosRepositoryImpl", "Sincronización fallida: Error desconocido al sincronizar catálogos: ${e.message}")
            com.sinc.mobile.domain.util.Result.Failure(GenericError("Error desconocido al sincronizar catálogos: ${e.message}"))
        }
    }
}