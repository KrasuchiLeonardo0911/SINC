package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogosDao {
    // Especie
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEspecies(especies: List<EspecieEntity>)

    @Query("SELECT * FROM especies")
    fun getAllEspecies(): Flow<List<EspecieEntity>>

    // Raza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRazas(razas: List<RazaEntity>)

    @Query("SELECT * FROM razas")
    fun getAllRazas(): Flow<List<RazaEntity>>

    // Categoria
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategorias(categorias: List<CategoriaAnimalEntity>)

    @Query("SELECT * FROM categoria_animals")
    fun getAllCategorias(): Flow<List<CategoriaAnimalEntity>>

    // Motivo Movimiento
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMotivosMovimiento(motivos: List<MotivoMovimientoEntity>)

    @Query("SELECT * FROM motivo_movimientos")
    fun getAllMotivosMovimiento(): Flow<List<MotivoMovimientoEntity>>

    // Municipio
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMunicipios(municipios: List<MunicipioEntity>)

    @Query("SELECT * FROM municipios")
    fun getAllMunicipios(): Flow<List<MunicipioEntity>>

    // Condicion Tenencia
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCondicionesTenencia(condiciones: List<CondicionTenenciaEntity>)

    @Query("SELECT * FROM condiciones_tenencia")
    fun getAllCondicionesTenencia(): Flow<List<CondicionTenenciaEntity>>

    // Fuente Agua
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFuentesAgua(fuentes: List<FuenteAguaEntity>)

    @Query("SELECT * FROM fuentes_agua")
    fun getAllFuentesAgua(): Flow<List<FuenteAguaEntity>>

    // Tipo Suelo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTiposSuelo(tipos: List<TipoSueloEntity>)

    @Query("SELECT * FROM tipos_suelo")
    fun getAllTiposSuelo(): Flow<List<TipoSueloEntity>>

    // Tipo Pasto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTiposPasto(tipos: List<TipoPastoEntity>)

    @Query("SELECT * FROM tipos_pasto")
    fun getAllTiposPasto(): Flow<List<TipoPastoEntity>>
}
