package com.sinc.mobile.app.features.cuaderno

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.cuaderno.components.*
import com.sinc.mobile.app.features.createunidadproductiva.components.ProgressBar
import com.sinc.mobile.app.ui.components.BannerManager
import com.sinc.mobile.app.ui.components.BannerType
import com.sinc.mobile.app.ui.components.ConfirmationDialog
import com.sinc.mobile.app.ui.components.EmptyState
import com.sinc.mobile.app.ui.components.FullscreenLoader
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.domain.model.Bitacora
import com.sinc.mobile.ui.theme.SincBackground
import com.sinc.mobile.ui.theme.SincGrayBackground
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuadernoScreen(
    onBack: () -> Unit,
    mainScaffoldBottomPadding: Dp = 0.dp,
    onViewChange: (CuadernoView) -> Unit,
    viewModel: BitacoraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var selectedBitacora by remember { mutableStateOf<Bitacora?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    // Notificar cambios de vista a la pantalla padre (MainScreen)
    LaunchedEffect(state.currentView) {
        onViewChange(state.currentView)
    }

    // Manejo de botón atrás físico/gesto
    BackHandler(enabled = state.currentView != CuadernoView.LISTADO) {
        when (state.currentView) {
            CuadernoView.CATEGORIAS -> viewModel.setView(CuadernoView.LISTADO)
            CuadernoView.PASO_FECHA -> viewModel.setView(CuadernoView.CATEGORIAS)
            CuadernoView.PASO_DESCRIPCION -> viewModel.setView(CuadernoView.PASO_FECHA)
            else -> onBack()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BitacoraViewModel.BitacoraEvent.ShowBanner -> {
                    BannerManager.show(event.message, event.type)
                }
                BitacoraViewModel.BitacoraEvent.Success -> {
                    viewModel.setView(CuadernoView.LISTADO)
                }
            }
        }
    }

    Scaffold(
        containerColor = SincBackground,
        topBar = {
            MinimalHeader(
                title = "Registros",
                onBackPress = {
                    when (state.currentView) {
                        CuadernoView.LISTADO -> onBack()
                        CuadernoView.CATEGORIAS -> viewModel.setView(CuadernoView.LISTADO)
                        CuadernoView.PASO_FECHA -> viewModel.setView(CuadernoView.CATEGORIAS)
                        CuadernoView.PASO_DESCRIPCION -> viewModel.setView(CuadernoView.PASO_FECHA)
                    }
                },
                modifier = Modifier
                    .background(SincGrayBackground)
                    .statusBarsPadding()
            )
        },
        floatingActionButton = {
            if (state.currentView == CuadernoView.LISTADO) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setView(CuadernoView.CATEGORIAS) },
                    modifier = Modifier.padding(bottom = if (mainScaffoldBottomPadding > 0.dp) mainScaffoldBottomPadding - 12.dp else 16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Nuevo Registro", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Barra de progreso (Solo en el stepper - 2 Pasos)
                if (state.currentView == CuadernoView.PASO_FECHA || state.currentView == CuadernoView.PASO_DESCRIPCION) {
                    val step = if (state.currentView == CuadernoView.PASO_FECHA) 1 else 2
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        RegistroProgressBar(currentStep = step)
                    }
                }

                AnimatedContent(
                    targetState = state.currentView,
                    label = "registros_flow_animation",
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        val enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn()
                        val exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut()
                        val popEnter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn()
                        val popExit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut()

                        // Lógica de dirección
                        if (targetState.ordinal > initialState.ordinal) {
                            enter togetherWith exit
                        } else {
                            popEnter togetherWith popExit
                        }
                    }
                ) { view ->
                    when (view) {
                        CuadernoView.LISTADO -> ListadoContent(
                            state = state,
                            onItemClick = { selectedBitacora = it },
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() }
                        )
                        CuadernoView.CATEGORIAS -> CategorySelectionContent(
                            onCategorySelected = { viewModel.setView(CuadernoView.PASO_FECHA) }
                        )
                        CuadernoView.PASO_FECHA -> DateSelectionStep(
                            selectedDate = state.tempDate,
                            onDateSelected = { viewModel.onDateSelected(it) }
                        )
                        CuadernoView.PASO_DESCRIPCION -> DescriptionEntryStep(
                            date = state.tempDate,
                            content = state.tempContent,
                            onContentChanged = { viewModel.onContentChanged(it) },
                            onSave = { 
                                viewModel.saveBitacora(state.tempDate, state.tempContent)
                            },
                            isSaving = state.isSaving
                        )
                    }
                }

                if (state.isLoading && !state.isInitialLoad) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Pantalla de Guardado Minimalista con Texto Alternado
            if (state.isSaving) {
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
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            },
                            label = "loading_text_animation"
                        ) { isFirst ->
                            Text(
                                text = if (isFirst) "Espere..." else "Guardando registro...",
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

    // Modal de Detalle
    if (selectedBitacora != null) {
        BitacoraDetailSheet(
            bitacora = selectedBitacora!!,
            onEdit = { 
                selectedBitacora = null 
            },
            onDelete = {
                showDeleteDialog = selectedBitacora!!.id
                selectedBitacora = null
            },
            onDismiss = { selectedBitacora = null }
        )
    }

    // Confirmación para Borrar
    if (showDeleteDialog != null) {
        ConfirmationDialog(
            showDialog = true,
            title = "Eliminar Registro",
            message = "¿Estás seguro de que deseas borrar esta anotación?",
            onConfirm = {
                viewModel.deleteBitacora(showDeleteDialog!!)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun ListadoContent(
    state: BitacoraState,
    onItemClick: (Bitacora) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    if (state.isInitialLoad) {
        FullscreenLoader(message = "Cargando registros...")
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            CuadernoMonthSelector(
                selectedDate = state.selectedDate,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 16.dp)
            )

            if (state.bitacoras.isEmpty() || state.filteredBitacoras.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyState(
                            title = "Sin anotaciones",
                            message = "No hay registros para este mes.",
                            icon = Icons.Default.Assignment
                        )
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(state.filteredBitacoras, key = { it.id }) { bitacora ->
                        RegistroListItem(
                            bitacora = bitacora,
                            onClick = { onItemClick(bitacora) }
                        )
                    }
                }
            }
        }
    }
}
