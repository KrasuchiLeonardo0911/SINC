package com.sinc.mobile.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.entities.EspecieEntity
import com.sinc.mobile.data.local.entities.RazaEntity // Changed import
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
class RazaDaoTest { // Changed class name

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: SincMobileDatabase

    @Inject
    lateinit var razaDao: RazaDao // Changed injected DAO

    @Inject
    lateinit var especieDao: EspecieDao // Inject EspecieDao to handle foreign key

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAllAndGetAllRazas() = runTest { // Changed method name
        val especie1 = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie1)) // Insert parent entity first

        val razas = listOf(
            RazaEntity(1, 1, "Merino"),
            RazaEntity(2, 1, "Corriedale")
        )
        razaDao.insertAll(razas)

        val allRazas = razaDao.getAllRazas().first() // Changed DAO method
        assertThat(allRazas).containsExactlyElementsIn(razas)
    }

    @Test
    fun clearAllRazas() = runTest { // Changed method name
        val especie1 = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie1)) // Insert parent entity first

        val razas = listOf(
            RazaEntity(1, 1, "Merino")
        )
        razaDao.insertAll(razas)

        razaDao.clearAll() // Changed DAO method

        val allRazas = razaDao.getAllRazas().first() // Changed DAO method
        assertThat(allRazas).isEmpty()
    }

    @Test
    fun insertOnConflictReplacesExisting() = runTest {
        val especie1 = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie1)) // Insert parent entity first

        val raza1 = RazaEntity(1, 1, "Merino")
        razaDao.insertAll(listOf(raza1))

        val updatedRaza1 = RazaEntity(1, 1, "Merino Actualizado")
        razaDao.insertAll(listOf(updatedRaza1))

        val allRazas = razaDao.getAllRazas().first()
        assertThat(allRazas).containsExactly(updatedRaza1)
        assertThat(allRazas.size).isEqualTo(1)
    }
}
