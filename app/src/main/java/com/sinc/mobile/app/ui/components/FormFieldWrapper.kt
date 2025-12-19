package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.ui.theme.CozyTextMain
import com.sinc.mobile.ui.theme.CozyWhite

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
        colors = CardDefaults.cardColors(containerColor = CozyWhite) // Fondo blanco
    ) {
        // El Column interno reemplaza al diseño anterior de "label arriba".
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp) // "bastante padding"
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, // Tamaño de subtítulo que te gustó
                color = CozyTextMain,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // El contenido (dropdown/textfield) se coloca debajo del label.
            content(Modifier.fillMaxWidth())
        }
    }
}
