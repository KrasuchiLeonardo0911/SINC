package com.sinc.mobile.data

import com.sinc.mobile.data.local.dao.WeatherAlertDao
import com.sinc.mobile.data.network.api.WeatherAlertApiService
import com.sinc.mobile.data.network.dto.WeatherAlertDto
import com.sinc.mobile.data.network.dto.WeatherAlertResponseDto
import com.sinc.mobile.data.repository.WeatherAlertRepositoryImpl
import com.sinc.mobile.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

class WeatherAlertRepositoryTest {

    private lateinit var apiService: WeatherAlertApiService
    private lateinit var dao: WeatherAlertDao
    private lateinit var repository: WeatherAlertRepositoryImpl

    @Before
    fun setup() {
        apiService = mockk()
        dao = mockk(relaxed = true)
        repository = WeatherAlertRepositoryImpl(apiService, dao)
    }

    @Test
    fun `syncAlerts successful - deletes old and inserts new`() = runBlocking {
        // Given
        val mockDto = WeatherAlertDto(
            upId = 1,
            upNombre = "UP 1",
            municipio = "Oberá",
            evento = "Tormenta",
            nivel = "rojo",
            inicio = "2026-02-24T18:00:00Z",
            fin = "2026-02-25T18:00:00Z",
            descripcion = "Test"
        )
        val response = WeatherAlertResponseDto(true, listOf(mockDto), 1)
        coEvery { apiService.getActiveAlerts() } returns Response.success(response)

        // When
        val result = repository.syncAlerts()

        // Then
        assertTrue(result is Result.Success)
        coVerify { dao.deleteAll() }
        coVerify { dao.insertAlerts(any()) }
    }

    @Test
    fun `syncAlerts failure - returns error result`() = runBlocking {
        // Given
        coEvery { apiService.getActiveAlerts() } returns Response.error(500, mockk(relaxed = true))

        // When
        val result = repository.syncAlerts()

        // Then
        assertTrue(result is Result.Failure)
        coVerify(exactly = 0) { dao.insertAlerts(any()) }
    }
}
