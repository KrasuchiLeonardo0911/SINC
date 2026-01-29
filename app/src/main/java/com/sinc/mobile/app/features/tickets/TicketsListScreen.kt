package com.sinc.mobile.app.features.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sinc.mobile.app.features.tickets.components.TicketListItem
import com.sinc.mobile.app.ui.components.EmptyState
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.domain.model.ticket.Ticket
import kotlinx.coroutines.launch
// No need for accompanist SwipeRefresh, using material pullrefresh
// import com.google.accompanist.swiperefresh.SwipeRefresh
// import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TicketsListScreen(
    navController: androidx.navigation.NavController,
    onTicketClick: (Long) -> Unit,
    onNewTicketClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: TicketsListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = viewModel::syncTickets)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        val message = currentBackStackEntry?.savedStateHandle?.get<String>("snackbar_message")
        if (message != null) {
            viewModel.syncTickets()
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            currentBackStackEntry?.savedStateHandle?.remove<String>("snackbar_message")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            MinimalHeader(title = "Mis Consultas", onBackPress = onNavigateBack, modifier = Modifier.statusBarsPadding())
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewTicketClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Consulta", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Box(
            Modifier
                .pullRefresh(pullRefreshState)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.tickets.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    icon = Icons.Outlined.Info,
                    title = "No tienes consultas",
                    message = "Presiona el botón '+' para crear una nueva consulta.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.tickets) { ticket ->
                        TicketListItem(
                            ticket = ticket,
                            onClick = { onTicketClick(ticket.id) }
                        )
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
