package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.CategoriaAnimalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaAnimalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategorias(categorias: List<CategoriaAnimalEntity>)

    @Query("SELECT * FROM categoria_animals")
    fun getAllCategorias(): Flow<List<CategoriaAnimalEntity>>
}