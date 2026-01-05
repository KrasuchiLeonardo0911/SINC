package com.sinc.mobile.app.features.movimiento

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincMobileTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovimientoStepperScreen(
    onBackPress: () -> Unit,
    viewModel: MovimientoStepperViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            MinimalHeader(
                title = "",
                onBackPress = onBackPress,
                modifier = Modifier.statusBarsPadding(),
                actions = {
                    IconButton(onClick = { /* TODO: Show instructions modal */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Instrucciones"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PagerIndicator(pagerState = pagerState)
                Spacer(modifier = Modifier.height(8.dp))
                BottomBarButton(
                    pagerState = pagerState,
                    onClick = {
                        if (pagerState.currentPage == 0) {
                            viewModel.onAddToList(pagerState)
                        } else {
                            viewModel.onSync()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { page ->
                when (page) {
                    0 -> {
                        if (uiState.formManager != null) {
                            MovimientoFormStepContent(
                                formState = uiState.formManager!!.formState.value,
                                onEspecieSelected = uiState.formManager!!::onEspecieSelected,
                                onCategoriaSelected = uiState.formManager!!::onCategoriaSelected,
                                onRazaSelected = uiState.formManager!!::onRazaSelected,
                                onMotivoSelected = uiState.formManager!!::onMotivoSelected,
                                onCantidadChanged = uiState.formManager!!::onCantidadChanged,
                                onDestinoChanged = uiState.formManager!!::onDestinoChanged
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Error: El formulario no pudo ser inicializado.")
                            }
                        }
                    }
                    1 -> MovimientoReviewStepContent(
                        movimientos = uiState.syncState.movimientosAgrupados.mapNotNull { it.originales.firstOrNull() },
                        onDelete = viewModel::onDelete,
                        onEdit = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerIndicator(pagerState: PagerState) {
    Row(
        Modifier
            .height(24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            val width = animateDpAsState(
                targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp,
                label = "Pager Indicator Width"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .height(8.dp)
                    .width(width.value)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomBarButton(pagerState: PagerState, onClick: () -> Unit) {
    val buttonText = if (pagerState.currentPage == 0) "Añadir a la Lista" else "Sincronizar y Guardar"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 8.dp, top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = buttonText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Preview
@Composable
fun MovimientoStepperScreenPreview() {
    SincMobileTheme {
        MovimientoStepperScreen(onBackPress = {})
    }
}