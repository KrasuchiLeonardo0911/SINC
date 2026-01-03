package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormFieldWrapper(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable (Modifier) -> Unit
) {
    // La tarjeta es el wrapper. El "margin" se controla con el Arrangement.spacedBy en el Column del formulario.
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Bordes suaves
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Con sombra
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Fondo blanco
    ) {
        // El Column interno reemplaza al diseño anterior de "label arriba".
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp) // "bastante padding"
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, // Tamaño de subtítulo que te gustó
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // El contenido (dropdown/textfield) se coloca debajo del label.
            content(Modifier.fillMaxWidth())
        }
    }
}