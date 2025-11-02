package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Catalogos

interface CatalogosRepository {
    suspend fun getCatalogos(): Result<Catalogos>
    suspend fun syncCatalogos(): Result<Unit>
}
