package com.sinc.mobile.app.features.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.notifications.components.NotificationItem
import com.sinc.mobile.app.features.notifications.components.SelectionAppBar
import com.sinc.mobile.app.ui.components.EmptyState
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincBackground
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel(),
    onBackPress: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            navController.navigate(route)
        }
    }

    Scaffold(
        containerColor = SincBackground,
        topBar = {
            val isSelectionMode = uiState.isSelectionMode
            // Animate between the two app bars
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                SelectionAppBar(
                    selectedCount = uiState.selectedIds.size,
                    onClose = { viewModel.onEvent(NotificationsEvent.OnExitSelectionMode) },
                    onDelete = { viewModel.onEvent(NotificationsEvent.OnDeleteSelected) }
                )
            }
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                MinimalHeader(
                    title = "Notificaciones",
                    onBackPress = onBackPress
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.notifications.isEmpty()) {
                EmptyState(
                    title = "Sin notificaciones",
                    message = "Aún no has recibido ninguna notificación.",
                    icon = Icons.Outlined.Notifications
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = uiState.notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            isSelected = uiState.selectedIds.contains(notification.id),
                            isSelectionMode = uiState.isSelectionMode,
                            onClick = { viewModel.onEvent(NotificationsEvent.OnItemClick(notification)) },
                            onLongClick = { viewModel.onEvent(NotificationsEvent.OnItemLongClick(notification.id)) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
