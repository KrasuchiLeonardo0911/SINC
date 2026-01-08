package com.sinc.mobile.app.features.logistics.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SlidingScreen(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(start = 60.dp), // Leave space for the handle
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        // The content of the sliding screen will go here.
        // For now, it's just a white surface.
    }
}
