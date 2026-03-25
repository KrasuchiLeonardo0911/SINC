package com.sinc.mobile.app.features.movimiento

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.ConfirmationDialog
import com.sinc.mobile.app.ui.components.LoadingOverlay
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.SyncResultOverlay
import com.sinc.mobile.ui.theme.SincMobileTheme
import com.sinc.mobile.ui.theme.SincBackground
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovimientoStepperScreen(
    onBackPress: () -> Unit,
    viewModel: MovimientoStepperViewModel = hiltViewModel(),
    initialPage: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for one-time page navigation events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.navigateToPage.collectLatest { page ->
            pagerState.animateScrollToPage(page)
        }
    }

    // Listen for stock validation errors
    LaunchedEffect(uiState.stockValidationError) {
        uiState.stockValidationError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearStockValidationError()
        }
    }

    // Listen for sync results to show Snackbars for errors
    LaunchedEffect(uiState.syncState.syncError) {
        val syncError = uiState.syncState.syncError
        if (syncError != null) {
            snackbarHostState.showSnackbar(
                message = "Error de sincronización: $syncError",
                duration = SnackbarDuration.Short
            )
        }
    }


    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<MovimientoAgrupado?>(null) }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            showDialog = true,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                itemToDelete?.let { viewModel.deleteMovimientoGroup(it) }
                showDeleteConfirmation = false
            },
            title = "Confirmar Eliminación",
            message = "El grupo de movimientos se eliminará de la lista y no se incluirá en la sincronización. ¿Estás seguro?"
        )
    }

    // --- Overlays for Syncing and Success ---
    // Replaced by inline saving screen

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MinimalHeader(
                title = "",
                onBackPress = onBackPress,
                actions = {
                    IconButton(onClick = { /* TODO: Show instructions modal */ }) {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = "Instrucciones")
                    }
                }
            )
        },
        containerColor = SincBackground,
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PagerIndicator(pagerState = pagerState)
                Spacer(modifier = Modifier.height(8.dp))
                BottomBarButton(
                    pagerState = pagerState,
                    isEnabled = if (pagerState.currentPage == 0) {
                        uiState.formManager?.formState?.value?.isFormValid ?: false
                    } else {
                        uiState.syncState.movimientosAgrupados.isNotEmpty()
                    },
                    // We removed the button loading state as we now have a full screen overlay
                    isLoading = false,
                    onClick = {
                        if (pagerState.currentPage == 0) {
                            viewModel.onAddToList()
                        } else {
                            viewModel.onSave()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) { page ->
                    when (page) {
                        0 -> {
                            if (uiState.formManager != null) {
                                MovimientoFormStepContent(
                                    snackbarHostState = snackbarHostState,
                                    formState = uiState.formManager!!.formState.value,
                                    onEspecieSelected = uiState.formManager!!::onEspecieSelected,
                                    onCategoriaSelected = uiState.formManager!!::onCategoriaSelected,
                                    onRazaSelected = uiState.formManager!!::onRazaSelected,
                                    onMotivoSelected = uiState.formManager!!::onMotivoSelected,
                                    onCantidadChanged = uiState.formManager!!::onCantidadChanged,
                                    onDestinoChanged = uiState.formManager!!::onDestinoChanged
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Error: El formulario no pudo ser inicializado.")
                                }
                            }
                        }
                        1 -> MovimientoReviewStepContent(
                            movimientosAgrupados = uiState.syncState.movimientosAgrupados,
                            catalogos = uiState.catalogos,
                            onDelete = { group ->
                                itemToDelete = group
                                showDeleteConfirmation = true
                            },
                            onEdit = { /* TODO */ }
                        )
                    }
                }
            }

            // Pantalla de Guardado Minimalista con Texto Alternado
            if (uiState.isSaving) {
                var showFirstText by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(800) // Cambia cada 800ms
                        showFirstText = !showFirstText
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AnimatedContent(
                            targetState = showFirstText,
                            transitionSpec = {
                                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) togetherWith androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                            },
                            label = "loading_text_animation"
                        ) { isFirst ->
                            Text(
                                text = if (isFirst) "Espere..." else "Guardando movimientos...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerIndicator(pagerState: PagerState) {
    Row(
        Modifier.height(24.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            val width = animateDpAsState(targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp, label = "Pager Indicator Width")
            Box(
                modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(color).height(8.dp).width(width.value)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun BottomBarButton(
    pagerState: PagerState,
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val buttonText = if (pagerState.currentPage == 0) "Añadir a la Lista" else "Guardar"

    Box(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(bottom = 8.dp, top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = isEnabled && !isLoading,
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            AnimatedContent(targetState = isLoading, label = "Button Loading Animation") { loading ->
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(text = buttonText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun MovimientoStepperScreenPreview() {
    SincMobileTheme {
       // MovimientoStepperScreen(onBackPress = {})
    }
}