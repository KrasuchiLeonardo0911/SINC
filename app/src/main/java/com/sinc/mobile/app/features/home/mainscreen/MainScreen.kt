package com.sinc.mobile.app.features.home.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.campos.CamposScreen
import com.sinc.mobile.app.features.historial_movimientos.HistorialMovimientosScreen
import com.sinc.mobile.app.features.home.MainViewModel
import com.sinc.mobile.app.features.home.mainscreen.components.Header
import com.sinc.mobile.app.features.home.mainscreen.components.MyJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.QuickJournalSection
import com.sinc.mobile.app.features.home.mainscreen.components.WeekdaySelector
import com.sinc.mobile.app.features.logistics.LogisticsScreen
import com.sinc.mobile.app.features.logistics.components.LogisticsDraggableHandle
import com.sinc.mobile.app.features.stock.StockScreen
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes
import com.sinc.mobile.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.delay


@Composable
fun MainScreen(
    navController: NavHostController,
    startRoute: String = CozyBottomNavRoutes.HOME,
    viewModel: MainViewModel = hiltViewModel() // Inject MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState() // Observe uiState

    var currentRoute by rememberSaveable { mutableStateOf(startRoute) }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = CozyMediumGray,
        bottomBar = {
            CozyBottomNavBar(
                selectedRoute = currentRoute,
                onItemSelected = { newRoute ->
                    if (newRoute != CozyBottomNavRoutes.ADD) {
                        currentRoute = newRoute
                    } else {
                        navController.navigate(com.sinc.mobile.app.navigation.Routes.MOVIMIENTO)
                    }
                }
            )
        }
    ) { paddingValues ->
        Crossfade(targetState = currentRoute, label = "main_screen_crossfade") { route ->
            when (route) {
                CozyBottomNavRoutes.HOME -> MainContent(
                    paddingValues = paddingValues,
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
                CozyBottomNavRoutes.STOCK -> StockScreen(
                    mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                    onBack = { currentRoute = CozyBottomNavRoutes.HOME } // Pass onBack lambda
                )
                CozyBottomNavRoutes.HISTORIAL -> HistorialMovimientosScreen(
                    mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                    onBack = { currentRoute = CozyBottomNavRoutes.HOME }
                )
                CozyBottomNavRoutes.CAMPOS -> CamposScreen(
                    mainScaffoldBottomPadding = paddingValues.calculateBottomPadding(),
                    onNavigateToCreateUnidadProductiva = {
                        navController.navigate(Routes.CREATE_UNIDAD_PRODUCTIVA)
                    },
                    onBack = { currentRoute = CozyBottomNavRoutes.HOME }
                )
                // Add placeholders for other routes
                else -> {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        Text(text = "Screen for $route")
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(
    paddingValues: PaddingValues,
    onSettingsClick: () -> Unit
) {
    var showLogisticsHandle by rememberSaveable { mutableStateOf(false) }
    var isLoadingLogistics by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val screenWidthPx = with(LocalDensity.current) { 400.dp.toPx() } // This should ideally come from BoxWithConstraints
    val handleWidthPx = with(LocalDensity.current) { 60.dp.toPx() }

    val offsetX = remember { Animatable(screenWidthPx) } // Start off-screen

    LaunchedEffect(showLogisticsHandle) {
        if (showLogisticsHandle) {
            isLoadingLogistics = false // Reset loading state
            offsetX.animateTo(
                targetValue = screenWidthPx - handleWidthPx,
                animationSpec = tween(durationMillis = 300)
            )
        } else {
            // Animate to fully off-screen
            offsetX.animateTo(
                targetValue = screenWidthPx,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            offsetX.snapTo((offsetX.value + delta).coerceAtMost(screenWidthPx))
        }
    }

    fun hideLogisticsPanel() {
        coroutineScope.launch {
            offsetX.animateTo(screenWidthPx, animationSpec = tween(300))
            showLogisticsHandle = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Header(onSettingsClick = onSettingsClick)
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.LightGray)
                Column(modifier = Modifier.padding(16.dp)) {
                    WeekdaySelector(
                        onDateClick = {
                            if (!showLogisticsHandle) { // Only open if not already open
                                coroutineScope.launch {
                                    offsetX.snapTo(screenWidthPx) // Ensure it starts off-screen
                                }
                                showLogisticsHandle = true
                            }
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) { MyJournalSection() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) { QuickJournalSection() }
        }

        // Scrim to dismiss the panel on outside click
        if (showLogisticsHandle && offsetX.value > 0f) { // Only show when peeking (not fully open)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)) // Semi-transparent overlay
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // No ripple effect
                        onClick = { hideLogisticsPanel() }
                    )
            )
        }


        // Draggable container for the sliding panel and handle
        Box(
            Modifier
                .fillMaxHeight()
                .width(screenWidthPx.dp) // The width of the sliding panel container
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = draggableState,
                    onDragStopped = {
                        coroutineScope.launch {
                            // If dragged more than 40% of the handle's width
                            if (offsetX.value < screenWidthPx - (handleWidthPx * 1.4f)) {
                                // Animate fully into view
                                offsetX.animateTo(0f, animationSpec = tween(250))
                                // Once panel is fully open, trigger loading
                                isLoadingLogistics = true
                                // Here you would launch a data fetch
                                // For now, just a delay
                                delay(1500)
                                isLoadingLogistics = false
                            } else {
                                // Animate back to peeking position
                                offsetX.animateTo(
                                    screenWidthPx - handleWidthPx,
                                    animationSpec = tween(300)
                                )
                            }
                        }
                    }
                )
        ) {
            // The SlidingScreen itself acts as the main content of the draggable panel
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                if (isLoadingLogistics) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Embed the LogisticsScreen content directly
                    LogisticsScreen(onBackPress = ::hideLogisticsPanel)
                }
            }

            // Only show the handle if the panel is not fully open
            if (offsetX.value > 0f && showLogisticsHandle) { // Also check showLogisticsHandle
                LogisticsDraggableHandle(
                    modifier = Modifier
                        .align(Alignment.TopStart) // Align to the start of this Box, which is the right edge of the visible screen
                        .offset { IntOffset(-handleWidthPx.roundToInt(), 0) } // Position handle at the very edge of the panel
                        .padding(top = 80.dp) // Adjusted to align with WeekdaySelector
                )
            }
        }
    }
}