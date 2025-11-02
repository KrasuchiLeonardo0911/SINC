package com.sinc.mobile.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.entities.MotivoMovimientoEntity // Changed import
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

import com.sinc.mobile.data.di.DatabaseModule
import dagger.hilt.android.testing.UninstallModules

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class MotivoMovimientoDaoTest { // Changed class name

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: SincMobileDatabase

    @Inject
    lateinit var motivoMovimientoDao: MotivoMovimientoDao // Changed injected DAO

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAllAndGetAllMotivosMovimiento() = runTest { // Changed method name
        val motivos = listOf(
            MotivoMovimientoEntity(1, "Nacimiento", "alta"),
            MotivoMovimientoEntity(2, "Compra", "alta"),
            MotivoMovimientoEntity(3, "Venta", "baja")
        )
        motivoMovimientoDao.insertAll(motivos)

        val allMotivos = motivoMovimientoDao.getAllMotivosMovimiento().first() // Changed DAO method
        assertThat(allMotivos).containsExactlyElementsIn(motivos)
    }

    @Test
    fun clearAllMotivosMovimiento() = runTest { // Changed method name
        val motivos = listOf(
            MotivoMovimientoEntity(1, "Nacimiento", "alta")
        )
        motivoMovimientoDao.insertAll(motivos)

        motivoMovimientoDao.clearAll() // Changed DAO method

        val allMotivos = motivoMovimientoDao.getAllMotivosMovimiento().first() // Changed DAO method
        assertThat(allMotivos).isEmpty()
    }

    @Test
    fun insertOnConflictReplacesExisting() = runTest {
        val motivo1 = MotivoMovimientoEntity(1, "Nacimiento", "alta")
        motivoMovimientoDao.insertAll(listOf(motivo1))

        val updatedMotivo1 = MotivoMovimientoEntity(1, "Nacimiento Actualizado", "alta")
        motivoMovimientoDao.insertAll(listOf(updatedMotivo1))

        val allMotivos = motivoMovimientoDao.getAllMotivosMovimiento().first()
        assertThat(allMotivos).containsExactly(updatedMotivo1)
        assertThat(allMotivos.size).isEqualTo(1)
    }
}
