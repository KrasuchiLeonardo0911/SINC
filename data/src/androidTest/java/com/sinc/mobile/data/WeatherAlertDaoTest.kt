package com.sinc.mobile.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.dao.WeatherAlertDao
import com.sinc.mobile.data.local.entities.WeatherAlertEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class WeatherAlertDaoTest {

    private lateinit var database: SincMobileDatabase
    private lateinit var dao: WeatherAlertDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SincMobileDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.weatherAlertDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAllAlerts() = runBlocking {
        val now = LocalDateTime.now()
        val alert = WeatherAlertEntity(
            upId = 1,
            upNombre = "UP Test",
            municipio = "Oberá",
            evento = "Tormenta",
            nivel = "rojo",
            inicio = now,
            fin = now.plusDays(1),
            descripcion = "Alerta de prueba"
        )

        dao.insertAlert(alert)
        val alerts = dao.getAllAlerts().first()

        assertThat(alerts).hasSize(1)
        assertThat(alerts[0].evento).isEqualTo("Tormenta")
        assertThat(alerts[0].nivel).isEqualTo("rojo")
        // Verificar que el TypeConverter de LocalDateTime funcionó
        assertThat(alerts[0].inicio).isEqualTo(now)
    }

    @Test
    fun deleteOldAlerts() = runBlocking {
        val past = LocalDateTime.now().minusDays(2)
        val future = LocalDateTime.now().plusDays(2)

        val oldAlert = WeatherAlertEntity(
            upId = 1, upNombre = "Old", municipio = "M1", evento = "E1", nivel = "amarillo",
            inicio = past.minusDays(1), fin = past, descripcion = "Old"
        )
        val newAlert = WeatherAlertEntity(
            upId = 2, upNombre = "New", municipio = "M2", evento = "E2", nivel = "rojo",
            inicio = LocalDateTime.now(), fin = future, descripcion = "New"
        )

        dao.insertAlert(oldAlert)
        dao.insertAlert(newAlert)

        dao.deleteOldAlerts(LocalDateTime.now().toString())

        val alerts = dao.getAllAlerts().first()
        assertThat(alerts).hasSize(1)
        assertThat(alerts[0].upNombre).isEqualTo("New")
    }
}
