package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import com.sinc.mobile.data.local.entities.UnidadProductivaTipoPastoCrossRef
import com.sinc.mobile.data.local.entities.UnidadProductivaTipoSueloCrossRef
import com.sinc.mobile.data.local.entities.TipoPastoEntity
import com.sinc.mobile.data.local.entities.TipoSueloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadProductivaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnidadProductiva(unidad: UnidadProductivaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(unidades: List<UnidadProductivaEntity>)

    @Query("SELECT * FROM unidades_productivas")
    fun getAllUnidadesProductivas(): Flow<List<UnidadProductivaEntity>>

    @Query("SELECT * FROM unidades_productivas WHERE id = :id")
    fun getUnidadProductivaById(id: Int): Flow<UnidadProductivaEntity?>

    @Query("SELECT * FROM unidad_productiva_tipo_suelo")
    fun getAllSuelosCrossRef(): Flow<List<UnidadProductivaTipoSueloCrossRef>>

    @Query("SELECT * FROM unidad_productiva_tipo_pasto")
    fun getAllPastosCrossRef(): Flow<List<UnidadProductivaTipoPastoCrossRef>>

    @Query("SELECT * FROM tipos_suelo")
    fun getAllTiposSuelo(): Flow<List<TipoSueloEntity>>

    @Query("SELECT * FROM tipos_pasto")
    fun getAllTiposPasto(): Flow<List<TipoPastoEntity>>

    @Query("DELETE FROM unidades_productivas")
    suspend fun clearAllUnidadesProductivas()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuelos(suelos: List<UnidadProductivaTipoSueloCrossRef>)

    @Query("DELETE FROM unidad_productiva_tipo_suelo")
    suspend fun clearAllSuelos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastos(pastos: List<UnidadProductivaTipoPastoCrossRef>)

    @Query("DELETE FROM unidad_productiva_tipo_pasto")
    suspend fun clearAllPastos()
    
    @Transaction
    suspend fun clearAndInsert(
        unidades: List<UnidadProductivaEntity>,
        suelos: List<UnidadProductivaTipoSueloCrossRef>,
        pastos: List<UnidadProductivaTipoPastoCrossRef>
    ) {
        // Clear all related data first
        clearAllSuelos()
        clearAllPastos()
        clearAllUnidadesProductivas()

        // Insert new data
        if (unidades.isNotEmpty()) {
            insertAll(unidades)
        }
        if (suelos.isNotEmpty()) {
            insertSuelos(suelos)
        }
        if (pastos.isNotEmpty()) {
            insertPastos(pastos)
        }
    }
}
