package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.sinc.mobile.ui.theme.SincMobileTheme

@Composable
fun MinimalHeader(
    title: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp) // Aumentamos ligeramente la altura para darle aire
            .padding(horizontal = 10.dp), // Padding lateral estricto
        horizontalArrangement = Arrangement.Start, // Alineación forzada a la izquierda
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de Retorno
        // Usamos 'Icon' directamente con un modifier de clickable sin ripple
        // o con ripple sutil para que no desplace el layout.
        Icon(
            // Usamos la versión ROUNDED para que las puntas del chevron sean suaves como en la imagen
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = "Atrás",
            tint = Color(0xFF111111), // Negro casi puro
            modifier = Modifier
                .size(36.dp) // Aumentamos el tamaño visual del icono
                // Ajuste negativo opcional: Los iconos suelen tener padding interno transparente.
                // A veces visualmente se ven desplazados.
                // Si sientes que está muy a la derecha, descomenta la siguiente línea:
                // .offset(x = (-8).dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Quitamos el efecto de ripple gris circular que ensucia el diseño minimalista
                    onClick = onBackPress
                )
        )

        Spacer(modifier = Modifier.width(8.dp)) // Redujimos el espacio porque el icono ahora es más grande

        // Título del Encabezado
        Text(
            text = title,
            color = Color(0xFF1F2937), // Negro suave
            fontSize = 18.sp,          // Tamaño ajustado al diseño original
            fontWeight = FontWeight.Medium, // CAMBIO CLAVE: De SemiBold a Medium
            letterSpacing = 0.sp       // Quitamos el tracking negativo
        )

        Spacer(modifier = Modifier.weight(1f))

        // Acciones del Header
        actions()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EE)
@Composable
fun MinimalHeaderPreview() {
    SincMobileTheme {
        MinimalHeader(
            title = "Todos los Registros",
            onBackPress = { }
        )
    }
}