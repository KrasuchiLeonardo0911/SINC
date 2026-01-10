package com.sinc.mobile.app.features.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.ConfirmationDialog
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is SettingsViewModel.NavigationEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            showDialog = true,
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
            },
            title = "Cerrar Sesión",
            message = "¿Estás seguro de que quieres cerrar la sesión?"
        )
    }

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Configuración (en construcción)",
                onBackPress = onNavigateBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = CozyLightGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Card
            ProfileCard(
                name = "Productor", // Hardcoded for now
                onEditClick = { /* TODO: Navigate to profile edit screen */ }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Settings Items
            var notificationsEnabled by remember { mutableStateOf(true) }

            SettingsSection {
                SettingsItem(
                    title = "Cuenta",
                    icon = Icons.Outlined.Person,
                    iconBackgroundColor = CozyLavender,
                    onClick = onNavigateToChangePassword // Reuse change password screen for "Account"
                )
                SettingsItem(
                    title = "Notificaciones",
                    icon = Icons.Outlined.Notifications,
                    iconBackgroundColor = CozyMint
                ) {
                    CozySwitch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection {
                SettingsItem(
                    title = "Ayuda",
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    iconBackgroundColor = CozyPink,
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    title = "Cerrar Sesión",
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    iconBackgroundColor = CozyPink.copy(alpha = 0.5f),
                    isLogout = true,
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }
}

@Composable
fun ProfileCard(name: String, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CozyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEditClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CozyLightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "User Avatar",
                    tint = CozyTextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CozyTextMain)
                )
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CozyTextSecondary)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Editar perfil",
                tint = CozyIconGray
            )
        }
    }
}

@Composable
fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CozyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    isLogout: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val contentColor = if (isLogout) md_theme_light_error else CozyTextMain

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = contentColor),
            modifier = Modifier.weight(1f)
        )
        if (trailingContent != null) {
            trailingContent()
        } else if (onClick != null && !isLogout) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = CozyIconGray
            )
        }
    }
}

@Composable
fun CozySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 52.dp,
    height: Dp = 32.dp,
    thumbPadding: Dp = 4.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val horizontalBias by animateFloatAsState(targetValue = if (checked) 1f else -1f)

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(CircleShape)
            .background(if (checked) CozyYellow else CozyDivider)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(thumbPadding)
                .fillMaxSize()
                .align(BiasAlignment(horizontalBias = horizontalBias, verticalBias = 0f))
        ) {
            Box(
                modifier = Modifier
                    .size(height - (thumbPadding * 2))
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}