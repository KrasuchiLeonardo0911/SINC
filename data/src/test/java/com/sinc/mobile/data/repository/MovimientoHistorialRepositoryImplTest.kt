package com.sinc.mobile.data.repository

import android.content.SharedPreferences
import com.sinc.mobile.data.local.dao.MovimientoHistorialDao
import com.sinc.mobile.data.model.MovimientoHistorialDto
import com.sinc.mobile.data.network.api.HistorialMovimientosApiService
import com.sinc.mobile.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `syncMovimientos performs full sync when local count is 0`() = runTest {
        // Arrange
        val timestamp = "2026-01-20 10:00:00"
        coEvery { dao.getMovimientoCount() } returns 0
        coEvery { apiService.getHistorialMovimientos(null) } returns Response.success(emptyList())

        // Act
        repository.syncMovimientos(timestamp)

        // Assert
        // Should call API with null because count is 0
        coVerify { apiService.getHistorialMovimientos(null) }
    }

    @Test
    fun `syncMovimientos performs delta sync when local count is greater than 0`() = runTest {
        // Arrange
        val timestamp = "2026-01-20 10:00:00"
        coEvery { dao.getMovimientoCount() } returns 5
        coEvery { apiService.getHistorialMovimientos(timestamp) } returns Response.success(emptyList())

        // Act
        repository.syncMovimientos(timestamp)

        // Assert
        // Should call API with provided timestamp
        coVerify { apiService.getHistorialMovimientos(timestamp) }
    }

    @Test
    fun `syncMovimientos updates watermark only if new data is newer`() = runTest {
        // Arrange
        val currentWatermark = "2026-01-20T10:00:00"
        coEvery { dao.getMovimientoCount() } returns 10
        
        // Data from API is older than current watermark
        val oldRecordDto = MovimientoHistorialDto(
            id = 100,
            fechaRegistro = "2026-01-19T10:00:00", // Older
            cantidad = 5,
            especie = "Ovino",
            categoria = "Cordero",
            raza = "Merino",
            motivo = "Nacimiento",
            tipoMovimiento = "Alta",
            unidadProductiva = "Campo 1",
            destinoTraslado = null
        )
        
        coEvery { apiService.getHistorialMovimientos(currentWatermark) } returns Response.success(listOf(oldRecordDto))
        
        // Act
        repository.syncMovimientos(currentWatermark)

        // Assert
        // Should NOT save the older timestamp
        coVerify(exactly = 0) { prefs.edit().putString("last_sync_movimientos", any()) }
    }

    @Test
    fun `syncMovimientos updates watermark if new data is newer`() = runTest {
        // Arrange
        val currentWatermark = "2026-01-20T10:00:00"
        coEvery { dao.getMovimientoCount() } returns 10
        
        // Data from API is newer
        val newRecordDto = MovimientoHistorialDto(
            id = 101,
            fechaRegistro = "2026-01-21T10:00:00", // Newer
            cantidad = 5,
            especie = "Ovino",
            categoria = "Cordero",
            raza = "Merino",
            motivo = "Nacimiento",
            tipoMovimiento = "Alta",
            unidadProductiva = "Campo 1",
            destinoTraslado = null
        )
        
        coEvery { apiService.getHistorialMovimientos(currentWatermark) } returns Response.success(listOf(newRecordDto))
        
        // Act
        repository.syncMovimientos(currentWatermark)

        // Assert
        // Should save the newer timestamp
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { prefs.edit() } returns editor
        
        repository.syncMovimientos(currentWatermark)
        
        verify { editor.putString("last_sync_movimientos", "2026-01-21T10:00:00") }
    }

    @Test
    fun `syncMovimientos handles API error correctly`() = runTest {
        // Arrange
        coEvery { dao.getMovimientoCount() } returns 5
        coEvery { apiService.getHistorialMovimientos(any()) } returns Response.error(500, mockk(relaxed = true))

        // Act
        val result = repository.syncMovimientos("ts")

        // Assert
        assertTrue(result is Result.Failure)
        assertEquals("API Error: 500", (result as Result.Failure).error.message)
    }
}
