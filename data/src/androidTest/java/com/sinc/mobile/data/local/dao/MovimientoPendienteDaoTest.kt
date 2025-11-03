package com.sinc.mobile.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.di.DatabaseModule
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.entities.MovimientoPendienteEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import javax.inject.Inject

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class MovimientoPendienteDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: SincMobileDatabase

    @Inject
    lateinit var movimientoPendienteDao: MovimientoPendienteDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetMovimiento() = runTest {
        val now = LocalDateTime.now()
        val movimiento = MovimientoPendienteEntity(
            id = 1,
            unidad_productiva_id = 1,
            especie_id = 1,
            categoria_id = 1,
            raza_id = 1,
            cantidad = 10,
            motivo_movimiento_id = 1,
            destino_traslado = "Vecino",
            observaciones = "Animales sanos",
            fecha_registro = now,
            sincronizado = false,
            fecha_creacion_local = now
        )
        movimientoPendienteDao.insert(movimiento)

        val allMovimientos = movimientoPendienteDao.getAllMovimientosPendientes().first()
        assertThat(allMovimientos).contains(movimiento)
    }

    @Test
    fun insertWithNullObservaciones() = runTest {
        val now = LocalDateTime.now()
        val movimiento = MovimientoPendienteEntity(
            id = 1,
            unidad_productiva_id = 1,
            especie_id = 1,
            categoria_id = 1,
            raza_id = 1,
            cantidad = 5,
            motivo_movimiento_id = 2,
            destino_traslado = null,
            observaciones = null,
            fecha_registro = now,
            sincronizado = false,
            fecha_creacion_local = now
        )
        movimientoPendienteDao.insert(movimiento)

        val result = movimientoPendienteDao.getAllMovimientosPendientes().first().first()
        assertThat(result.id).isEqualTo(1)
        assertThat(result.observaciones).isNull()
    }

    @Test
    fun getUnsyncedMovimientos() = runTest {
        val now = LocalDateTime.now()
        val syncedMovimiento = MovimientoPendienteEntity(
            id = 1, unidad_productiva_id = 1, especie_id = 1, categoria_id = 1, raza_id = 1,
            cantidad = 10, motivo_movimiento_id = 1, destino_traslado = null, observaciones = null,
            fecha_registro = now, sincronizado = true, fecha_creacion_local = now
        )
        val unsyncedMovimiento = MovimientoPendienteEntity(
            id = 2, unidad_productiva_id = 1, especie_id = 1, categoria_id = 1, raza_id = 1,
            cantidad = 5, motivo_movimiento_id = 2, destino_traslado = null, observaciones = null,
            fecha_registro = now, sincronizado = false, fecha_creacion_local = now
        )
        movimientoPendienteDao.insertAll(listOf(syncedMovimiento, unsyncedMovimiento))

        val unsynced = movimientoPendienteDao.getUnsyncedMovimientos().first()
        assertThat(unsynced).containsExactly(unsyncedMovimiento)
    }

    @Test
    fun markAsSynced() = runTest {
        val now = LocalDateTime.now()
        val movimiento = MovimientoPendienteEntity(
            id = 1, unidad_productiva_id = 1, especie_id = 1, categoria_id = 1, raza_id = 1,
            cantidad = 10, motivo_movimiento_id = 1, destino_traslado = null, observaciones = null,
            fecha_registro = now, sincronizado = false, fecha_creacion_local = now
        )
        val insertedId = movimientoPendienteDao.insert(movimiento)

        movimientoPendienteDao.markAsSynced(insertedId)

        val result = movimientoPendienteDao.getAllMovimientosPendientes().first().first()
        assertThat(result.sincronizado).isTrue()
    }

    @Test
    fun clearAll() = runTest {
        val now = LocalDateTime.now()
        val movimiento1 = MovimientoPendienteEntity(
            id = 1, unidad_productiva_id = 1, especie_id = 1, categoria_id = 1, raza_id = 1,
            cantidad = 10, motivo_movimiento_id = 1, destino_traslado = null, observaciones = null,
            fecha_registro = now, sincronizado = false, fecha_creacion_local = now
        )
        val movimiento2 = MovimientoPendienteEntity(
            id = 2, unidad_productiva_id = 1, especie_id = 1, categoria_id = 1, raza_id = 1,
            cantidad = 5, motivo_movimiento_id = 2, destino_traslado = null, observaciones = null,
            fecha_registro = now, sincronizado = false, fecha_creacion_local = now
        )
        movimientoPendienteDao.insertAll(listOf(movimiento1, movimiento2))

        movimientoPendienteDao.clearAll()

        val allMovimientos = movimientoPendienteDao.getAllMovimientosPendientes().first()
        assertThat(allMovimientos).isEmpty()
    }
}