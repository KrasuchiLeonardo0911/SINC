package com.sinc.mobile.app.features.logistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.sinc.mobile.app.ui.components.MinimalHeader

@Composable
fun LogisticsScreen(
    onBackPress: () -> Unit
) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Logística",
                onBackPress = onBackPress
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Pantalla de Logística", fontSize = 20.sp)
        }
    }
}
