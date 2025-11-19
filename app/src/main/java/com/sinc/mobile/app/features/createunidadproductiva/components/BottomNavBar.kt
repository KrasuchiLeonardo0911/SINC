package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.ui.theme.colorBotonSiguiente

@Composable
fun BottomNavBar(
    currentStep: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 1) {
            TextButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Anterior"
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Anterior")
            }
        } else {
            Spacer(modifier = Modifier) // Ocupa el espacio para mantener el botón de siguiente a la derecha
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = colorBotonSiguiente) // Aplicar el nuevo color
        ) {
            Text(
                text = if (currentStep < 3) "Siguiente" else "Finalizar",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
