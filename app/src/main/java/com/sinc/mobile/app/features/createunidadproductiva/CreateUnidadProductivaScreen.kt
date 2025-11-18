package com.sinc.mobile.app.features.createunidadproductiva

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.createunidadproductiva.components.CreateUnidadProductivaPager
import com.sinc.mobile.app.features.createunidadproductiva.components.NavigationFooter
import com.sinc.mobile.app.features.createunidadproductiva.components.ScreenHeader
import com.sinc.mobile.app.features.createunidadproductiva.components.StepIndicator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateUnidadProductivaScreen(
    viewModel: CreateUnidadProductivaViewModel = hiltViewModel(),
    onUnidadProductivaCreated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })

    // Sincronizar el pager con el ViewModel
    LaunchedEffect(uiState.currentStep) {
        pagerState.animateScrollToPage(uiState.currentStep - 1)
    }

    // Sincronizar el ViewModel con el pager (cuando el usuario hace swipe)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onStepSelected(page + 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant, // Un tono gris claro
        bottomBar = {
            NavigationFooter(
                currentStep = uiState.currentStep,
                onNextClick = {
                    if (uiState.currentStep == 3) {
                        onUnidadProductivaCreated()
                    } else {
                        viewModel.onNextStep()
                    }
                },
                onPreviousClick = {
                    viewModel.onPreviousStep()
                },
                onCancelClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScreenHeader()

            StepIndicator(
                currentStep = uiState.currentStep,
                onStepSelected = { viewModel.onStepSelected(it) }
            )



            CreateUnidadProductivaPager(
                pagerState = pagerState,
                uiState = uiState,
                onNombreChange = viewModel::onNombreChange,
                onIdentificadorLocalChange = viewModel::onIdentificadorLocalChange,
                onSuperficieChange = viewModel::onSuperficieChange,
                onMunicipioSelected = viewModel::onMunicipioSelected,
                onCondicionTenenciaSelected = viewModel::onCondicionTenenciaSelected,
                onHabitaChange = viewModel::onHabitaChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
