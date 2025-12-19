package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.ui.theme.AccentYellow
import com.sinc.mobile.ui.theme.Gray200
import com.sinc.mobile.ui.theme.InactiveGray

@Composable
fun MovimientoBottomBar(
    onSave: () -> Unit,
    isSaving: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 0.dp, // Removed shadow
        color = Color.Transparent // Inherit background color
    ) {
        Button(
            onClick = onSave,
            enabled = enabled, // Button is truly enabled/disabled now
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(48.dp),
            shape = ButtonDefaults.shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentYellow, // Original container color
                disabledContainerColor = Gray200, // Explicitly set for disabled state
                disabledContentColor = InactiveGray // Explicitly set for disabled state
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black
                )
            } else {
                Text(
                    text = "Guardar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black // Original text color
                )
            }
        }
    }
}
