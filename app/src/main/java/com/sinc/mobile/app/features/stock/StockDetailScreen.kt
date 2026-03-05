package com.sinc.mobile.app.features.stock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincBackground

@Composable
fun StockDetailScreen(
    speciesName: String,
    grouping: String,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        containerColor = SincBackground,
        topBar = {
            MinimalHeader(
                title = "Stock de $speciesName",
                onBackPress = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Visualizando Stock de: $speciesName\nVista: $grouping",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
