package com.sinc.mobile.domain.use_case.ventas

import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.domain.model.*
import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.repository.StockRepository
import com.sinc.mobile.domain.repository.VentasRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ValidateStockForVentaUseCaseTest {

    private lateinit var stockRepository: StockRepository
    private lateinit var ventasRepository: VentasRepository
    private lateinit var catalogosRepository: CatalogosRepository
    private lateinit var validateStockForVenta: ValidateStockForVentaUseCase

    @Before
    fun setUp() {
        stockRepository = mock()
        ventasRepository = mock()
        catalogosRepository = mock()
        validateStockForVenta = ValidateStockForVentaUseCase(
            stockRepository,
            ventasRepository,
            catalogosRepository
        )
    }

    @Test
    fun `invoke returns Success when stock is sufficient`() = runTest {
        // Arrange
        val upId = 1
        val especieId = 1
        val razaId = 1
        val categoriaId = 1
        val cantidadSolicitada = 5
        val stockReal = 10
        val stockPendiente = 2

        // Mock Catalogos
        val mockCatalogos = Catalogos(
            especies = listOf(Especie(1, "Ovino")),
            razas = listOf(Raza(1, "Merino", 1)),
            categorias = listOf(Categoria(1, "Cordero", 1)),
            motivosMovimiento = emptyList(),
            municipios = emptyList(),
            condicionesTenencia = emptyList(),
            fuentesAgua = emptyList(),
            tiposSuelo = emptyList(),
            tiposPasto = emptyList()
        )
        whenever(catalogosRepository.getCatalogos()).thenReturn(flowOf(mockCatalogos))

        // Mock Stock
        val mockStock = Stock(
            stockTotalGeneral = 10,
            unidadesProductivas = listOf(
                UnidadProductivaStock(
                    id = upId,
                    nombre = "Mi Campo",
                    stockTotal = 10,
                    especies = listOf(
                        EspecieStock(
                            nombre = "Ovino",
                            stockTotal = 10,
                            desglose = listOf(
                                DesgloseStock(
                                    categoria = "Cordero",
                                    raza = "Merino",
                                    cantidad = stockReal
                                )
                            )
                        )
                    )
                )
            )
        )
        whenever(stockRepository.getStock()).thenReturn(flowOf(mockStock))

        // Mock Ventas Pendientes
        whenever(ventasRepository.getPendingQuantity(upId, especieId, razaId, categoriaId)).thenReturn(stockPendiente)

        // Act
        val result = validateStockForVenta(upId, especieId, razaId, categoriaId, cantidadSolicitada)

        // Assert (Disponible = 10 - 2 = 8. Solicitado 5. 5 <= 8 -> Success)
        assertThat(result).isInstanceOf(ValidateStockForVentaUseCase.ValidationResult.Success::class.java)
    }

    @Test
    fun `invoke returns InsufficientStock when requested quantity exceeds available`() = runTest {
        // Arrange
        val upId = 1
        val especieId = 1
        val razaId = 1
        val categoriaId = 1
        val cantidadSolicitada = 9
        val stockReal = 10
        val stockPendiente = 2

        // Reuse Mocks structure
        val mockCatalogos = Catalogos(
            especies = listOf(Especie(1, "Ovino")),
            razas = listOf(Raza(1, "Merino", 1)),
            categorias = listOf(Categoria(1, "Cordero", 1)),
            motivosMovimiento = emptyList(),
            municipios = emptyList(),
            condicionesTenencia = emptyList(),
            fuentesAgua = emptyList(),
            tiposSuelo = emptyList(),
            tiposPasto = emptyList()
        )
        whenever(catalogosRepository.getCatalogos()).thenReturn(flowOf(mockCatalogos))

        val mockStock = Stock(
            stockTotalGeneral = 10,
            unidadesProductivas = listOf(
                UnidadProductivaStock(
                    id = upId, nombre="C", stockTotal=10,
                    especies = listOf(
                        EspecieStock(
                            nombre = "Ovino", stockTotal=10,
                            desglose = listOf(
                                DesgloseStock(categoria = "Cordero", raza = "Merino", cantidad = stockReal)
                            )
                        )
                    )
                )
            )
        )
        whenever(stockRepository.getStock()).thenReturn(flowOf(mockStock))

        whenever(ventasRepository.getPendingQuantity(upId, especieId, razaId, categoriaId)).thenReturn(stockPendiente)

        // Act
        val result = validateStockForVenta(upId, especieId, razaId, categoriaId, cantidadSolicitada)

        // Assert (Disponible = 10 - 2 = 8. Solicitado 9. 9 > 8 -> Insufficient)
        assertThat(result).isInstanceOf(ValidateStockForVentaUseCase.ValidationResult.InsufficientStock::class.java)
        
        val error = result as ValidateStockForVentaUseCase.ValidationResult.InsufficientStock
        assertThat(error.real).isEqualTo(10)
        assertThat(error.pendiente).isEqualTo(2)
    }
}
