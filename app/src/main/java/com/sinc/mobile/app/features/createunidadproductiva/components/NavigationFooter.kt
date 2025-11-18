package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavigationFooter(
    currentStep: Int,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancelClick) {
                Text("Cancelar")
            }

            Row {
                if (currentStep > 1) {
                    TextButton(onClick = onPreviousClick) {
                        Text("Anterior")
                    }
                }
                Button(
                    onClick = onNextClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(if (currentStep == 3) "Finalizar" else "Siguiente")
                }
            }
        }
    }
}
