package com.sinc.mobile.data.repository

import android.content.SharedPreferences
import com.sinc.mobile.data.local.dao.MovimientoHistorialDao
import com.sinc.mobile.data.local.entities.MovimientoHistorialEntity
import com.sinc.mobile.data.model.MovimientoHistorialDto
import com.sinc.mobile.data.network.api.HistorialMovimientosApiService
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class MovimientoHistorialRepositoryImplTest {

    private lateinit var apiService: HistorialMovimientosApiService
    private lateinit var dao: MovimientoHistorialDao
    private lateinit var prefs: SharedPreferences
    private lateinit var repository: MovimientoHistorialRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        dao = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        repository = MovimientoHistorialRepositoryImpl(apiService, dao, prefs)
    }

    @Test
    fun `syncMovimientos calls api with correct timestamp parameter`() = runTest {
        // Arrange
        val timestamp = "2026-01-20 10:00:00"
        coEvery { apiService.getHistorialMovimientos(timestamp) } returns Response.success(emptyList())

        // Act
        repository.syncMovimientos(timestamp)

        // Assert
        coVerify { apiService.getHistorialMovimientos(timestamp) }
    }

    @Test
    fun `syncMovimientos inserts data when api returns new records`() = runTest {
        // Arrange
        val timestamp = "2026-01-20 10:00:00"
        val newRecordDto = MovimientoHistorialDto(
            id = 100,
            fechaRegistro = "2026-01-20T12:00:00",
            cantidad = 5,
            especie = "Ovino",
            categoria = "Cordero",
            raza = "Merino",
            motivo = "Nacimiento",
            tipoMovimiento = "Alta",
            unidadProductiva = "Campo 1",
            destinoTraslado = null
        )
        
        coEvery { apiService.getHistorialMovimientos(timestamp) } returns Response.success(listOf(newRecordDto))
        
        // Act
        val result = repository.syncMovimientos(timestamp)

        // Assert
        assertTrue(result is Result.Success)
        coVerify { dao.insertAll(any()) }
    }

    @Test
    fun `syncMovimientos clears data on initial sync (null timestamp)`() = runTest {
        // Arrange
        val timestamp: String? = null
        val recordDto = MovimientoHistorialDto(
            id = 101,
            fechaRegistro = "2026-01-20T12:00:00",
            cantidad = 10,
            especie = "Caprino",
            categoria = "Cabra",
            raza = "Angora",
            motivo = "Compra",
            tipoMovimiento = "Alta",
            unidadProductiva = "Campo 1",
            destinoTraslado = null
        )

        coEvery { apiService.getHistorialMovimientos(null) } returns Response.success(listOf(recordDto))

        // Act
        repository.syncMovimientos(timestamp)

        // Assert
        coVerify { dao.clearAndInsert(any()) }
    }
}
