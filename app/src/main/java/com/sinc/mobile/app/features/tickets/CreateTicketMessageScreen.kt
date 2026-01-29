package com.sinc.mobile.app.features.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.MinimalHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketMessageScreen(
    viewModel: CreateTicketViewModel = hiltViewModel(),
    ticketType: String,
    onNavigateBack: () -> Unit,
    onTicketCreated: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CreateTicketEvent.TicketCreated -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Consulta enviada con éxito.")
                        onTicketCreated()
                    }
                }
                is CreateTicketEvent.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MinimalHeader(
                title = "Escribe tu Mensaje",
                onBackPress = onNavigateBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Button(
                onClick = { viewModel.createTicket(ticketType, message) },
                enabled = message.isNotBlank() && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar Consulta")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Describe tu consulta aquí...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 10
            )
        }
    }
}
