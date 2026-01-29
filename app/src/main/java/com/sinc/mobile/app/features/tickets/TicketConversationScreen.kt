package com.sinc.mobile.app.features.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.tickets.components.MessageItem
import com.sinc.mobile.app.ui.components.MinimalHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TicketConversationScreen(
    viewModel: TicketConversationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isRefreshing, onRefresh = viewModel::refreshConversation)

    LaunchedEffect(uiState.messages) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            MinimalHeader(
                title = uiState.ticket?.asunto ?: "Cargando...",
                onBackPress = onNavigateBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            MessageInput(
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                isSending = uiState.isSendingMessage
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .pullRefresh(pullRefreshState)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) { // Show loading only if no messages are loaded yet
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.messages.isEmpty() && !uiState.isLoading) {
                Text(
                    text = "No hay mensajes en esta conversación.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageItem(message = message)
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    isSending: Boolean
) {
    var text by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    onSendMessage(text)
                    text = ""
                },
                enabled = text.isNotBlank() && !isSending,
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}
