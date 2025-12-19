package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.ui.theme.SincMobileTheme

/**
 * Un componente de encabezado reutilizable para pantallas de navegación profunda (detalle).
 *
 * @param title El texto que se mostrará en el centro.
 * @param onBackPress La lambda que se ejecutará al presionar el botón de "Atrás".
 * @param modifier Un modificador opcional para la personalización externa.
 * @param rightActionContent Un Composable opcional para el contenido de acción derecho.
 *                         Si no se provee, se usará un icono de menú por defecto.
 */
@Composable
fun SoftTopBar(
    title: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    rightActionContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Elemento Izquierdo (Botón "Atrás")
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Color(0x000000).copy(alpha = 0.06f),
                    spotColor = Color(0x000000).copy(alpha = 0.06f)
                )
                .clip(CircleShape)
                .background(Color.White)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color(0xFF333333),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Elemento Central (Título)
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            color = Color(0xFF1F2937),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        // Elemento Derecho (Slot de Acción / Menú)
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rightActionContent != null) {
                rightActionContent()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F3F0)
@Composable
fun SoftTopBarPreview() {
    SincMobileTheme {
        SoftTopBar(
            title = "Detalles",
            onBackPress = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F3F0)
@Composable
fun SoftTopBarWithActionPreview() {
    SincMobileTheme {
        SoftTopBar(
            title = "Editar Perfil",
            onBackPress = {},
            rightActionContent = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú",
                        tint = Color(0xFF333333)
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F3F0)
@Composable
fun SoftTopBarLongTitlePreview() {
    SincMobileTheme {
        SoftTopBar(
            title = "Un nombre de pantalla realmente largo",
            onBackPress = {},
            rightActionContent = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú",
                        tint = Color(0xFF333333)
                    )
                }
            }
        )
    }
}
