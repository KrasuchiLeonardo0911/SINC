package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sinc.mobile.data.local.entities.DeclaracionVentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeclaracionVentaDao {
    @Query("SELECT * FROM declaraciones_venta ORDER BY fechaDeclaracion DESC")
    fun getAllDeclaraciones(): Flow<List<DeclaracionVentaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(declaraciones: List<DeclaracionVentaEntity>)

    @Query("DELETE FROM declaraciones_venta")
    suspend fun clearAll()

    @Transaction
    suspend fun clearAndInsert(declaraciones: List<DeclaracionVentaEntity>) {
        clearAll()
        insertAll(declaraciones)
    }

    @Query("""
        SELECT SUM(cantidad) FROM declaraciones_venta 
        WHERE unidadProductivaId = :upId 
        AND especieId = :especieId 
        AND razaId = :razaId 
        AND categoriaAnimalId = :categoriaId 
        AND estado = 'pendiente'
    """)
    suspend fun getSumPendientes(upId: Int, especieId: Int, razaId: Int, categoriaId: Int): Int?
}
