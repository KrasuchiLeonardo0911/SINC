package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sinc.mobile.app.features.createunidadproductiva.CreateUnidadProductivaState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateUnidadProductivaPager(
    pagerState: PagerState,
    uiState: CreateUnidadProductivaState,
    onNombreChange: (String) -> Unit,
    onIdentificadorLocalChange: (String) -> Unit,
    onSuperficieChange: (String) -> Unit,
    onMunicipioSelected: (com.sinc.mobile.domain.model.Municipio) -> Unit,
    onCondicionTenenciaSelected: (com.sinc.mobile.domain.model.CondicionTenencia) -> Unit,
    onHabitaChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        when (page) {
            0 -> Step1_BasicDataForm(
                uiState = uiState,
                onNombreChange = onNombreChange,
                onIdentificadorLocalChange = onIdentificadorLocalChange,
                onSuperficieChange = onSuperficieChange,
                onMunicipioSelected = onMunicipioSelected,
                onCondicionTenenciaSelected = onCondicionTenenciaSelected,
                onHabitaChange = onHabitaChange
            )
            1 -> Step2_LocationMap()
            2 -> Step3_OptionalDetailsForm()
        }
    }
}
