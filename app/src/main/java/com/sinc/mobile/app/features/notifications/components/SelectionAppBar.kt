package com.sinc.mobile.app.features.notifications.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.layout.statusBarsPadding
import com.sinc.mobile.ui.theme.SincGrayBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionAppBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SincGrayBackground,
        modifier = modifier
    ) {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "$selectedCount",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Cerrar modo selección"
                    )
                }
            },
            actions = {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar seleccionados"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}
