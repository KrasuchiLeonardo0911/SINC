package com.sinc.mobile.app.features.historial_movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.historial_movimientos.components.HistorialMovimientosSkeletonLoader
import com.sinc.mobile.app.features.historial_movimientos.components.MovimientoHistorialItem
import com.sinc.mobile.app.ui.components.MinimalHeader

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistorialMovimientosScreen(
    viewModel: HistorialMovimientosViewModel = hiltViewModel(),
    mainScaffoldBottomPadding: Dp,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    if (state.isInitialLoad) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                MinimalHeader(
                    title = "Historial de Movimientos",
                    onBackPress = onBack,
                    modifier = Modifier.statusBarsPadding()
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(bottom = mainScaffoldBottomPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        }
    } else {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = state.isLoading,
            onRefresh = { viewModel.syncMovimientos() }
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                MinimalHeader(
                    title = "Historial de Movimientos",
                    onBackPress = onBack,
                    modifier = Modifier.statusBarsPadding()
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .pullRefresh(pullRefreshState)
                    .padding(paddingValues)
                    .padding(bottom = mainScaffoldBottomPadding)
                    .fillMaxSize()
            ) {
                if (state.movimientos.isEmpty() && !state.isLoading) {
                    Text(
                        text = "No hay movimientos para mostrar.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.movimientos) { movimiento ->
                        MovimientoHistorialItem(movimiento = movimiento)
                    }
                }

                PullRefreshIndicator(
                    refreshing = state.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
