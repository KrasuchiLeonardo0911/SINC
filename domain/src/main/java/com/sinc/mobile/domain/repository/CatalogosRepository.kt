package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface CatalogosRepository {
    fun getCatalogos(): Flow<Catalogos>
    fun getMovimientoCatalogos(): Flow<Catalogos>
    suspend fun syncCatalogos(remoteVersion: String? = null): Result<Unit, Error>
}
