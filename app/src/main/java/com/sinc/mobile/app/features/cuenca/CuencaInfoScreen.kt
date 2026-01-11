package com.sinc.mobile.app.features.cuenca

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.sinc.mobile.app.ui.components.MinimalHeader

@Composable
fun CuencaInfoScreen(navController: NavController) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Información de la Cuenca",
                onBackPress = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Página en construcción.")
        }
    }
}
