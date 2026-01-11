package com.sinc.mobile.app.features.home.mainscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.sinc.mobile.R

@Composable
fun Header(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Text
        Text(
            text = "Hola, Productor", // Reverted text
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface // Keep text color consistent with theme
            )
        )

        // Right: Logo (replaces user icon)
        Image(
            painter = painterResource(id = R.drawable.logoovinos),
            contentDescription = "Logo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape) // Apply circular clip for ripple effect
                .clickable { onSettingsClick() } // Keep clickable functionality
        )
    }
}
