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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.ui.theme.SincGrayBackground
import com.sinc.mobile.ui.theme.SincMobileTheme

@Composable
fun MinimalHeader(
    title: String? = null,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    titleFontSize: TextUnit = 18.sp,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = SincGrayBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding() // Padding interno para respetar la barra de estado
                .fillMaxWidth()
                .height(48.dp) // Altura compacta
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de Retorno
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "Atrás",
                tint = Color(0xFF111111),
                modifier = Modifier
                    .size(32.dp) // Icono más compacto
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackPress
                    )
            )

            if (!title.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))

                // Título del Encabezado
                Text(
                    text = title,
                    color = Color(0xFF1F2937),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Acciones del Header
            actions()
        }
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