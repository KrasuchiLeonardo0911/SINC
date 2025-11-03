package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Catalogos
import kotlinx.coroutines.flow.Flow

interface CatalogosRepository {
    fun getCatalogos(): Flow<Catalogos>
    suspend fun syncCatalogos(): Result<Unit>
}
