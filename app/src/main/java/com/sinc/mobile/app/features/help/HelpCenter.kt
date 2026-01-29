package com.sinc.mobile.app.features.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.MinimalHeader

data class FaqItem(
    val question: String,
    val answer: String
)

data class FaqCategory(
    val title: String,
    val items: List<FaqItem>
)

val faqData = listOf(
    FaqCategory(
        title = "General",
        items = listOf(
            FaqItem(
                question = "¿Cómo cambio mi contraseña?",
                answer = "Puedes cambiar tu contraseña desde la pantalla de 'Ajustes', seleccionando la opción 'Cambiar contraseña'."
            ),
            FaqItem(
                question = "¿Cómo puedo cerrar sesión?",
                answer = "La opción para cerrar sesión se encuentra en la pantalla de 'Ajustes'."
            )
        )
    ),
    FaqCategory(
        title = "Campos y Unidades Productivas",
        items = listOf(
            FaqItem(
                question = "¿Qué es una Unidad Productiva?",
                answer = "Una Unidad Productiva representa un campo o establecimiento donde se gestiona el ganado. Cada movimiento de stock está asociado a una Unidad Productiva."
            ),
            FaqItem(
                question = "¿Cómo creo una nueva Unidad Productiva?",
                answer = "En la sección 'Mis Campos', encontrarás un botón para añadir una nueva Unidad Productiva. Deberás completar un formulario con la información del campo."
            ),
            FaqItem(
                question = "¿Cómo edito la información de una Unidad Productiva?",
                answer = "Desde la lista de 'Mis Campos', puedes seleccionar una Unidad Productiva para ver sus detalles y editar la información."
            )
        )
    ),
    FaqCategory(
        title = "Stock y Movimientos",
        items = listOf(
            FaqItem(
                question = "¿Cómo registro un nuevo movimiento de stock (entrada/salida)?",
                answer = "Desde la pantalla principal, en la sección 'Mi Cuaderno', pulsa en 'Añadir' para iniciar el registro de un nuevo movimiento. Deberás seleccionar el campo, la especie, categoría, motivo y cantidad."
            ),
            FaqItem(
                question = "¿Qué son los 'movimientos pendientes'?",
                answer = "Los movimientos pendientes son aquellos que has registrado en tu dispositivo pero que aún no se han sincronizado con el sistema central. Puedes verlos en la tarjeta 'Movimientos Pendientes' del dashboard o en la pestaña 'Pendientes' de la pantalla de movimientos."
            ),
            FaqItem(
                question = "¿Cómo sincronizo mis movimientos pendientes con el sistema?",
                answer = "En la pantalla de 'Movimientos Pendientes', encontrarás un botón para 'Sincronizar y Guardar'. Al pulsarlo, todos los movimientos de la lista se enviarán al sistema."
            ),
            FaqItem(
                question = "¿Dónde puedo ver mi historial de movimientos?",
                answer = "En la pantalla principal, en la sección 'Mi Cuaderno', pulsa en 'Historial' para acceder al historial completo de movimientos de stock."
            )
        )
    ),
    FaqCategory(
        title = "Ventas",
        items = listOf(
            FaqItem(
                question = "¿Cómo declaro una nueva venta?",
                answer = "La declaración de ventas se realiza desde la sección 'Ventas'. Deberás completar un formulario con los detalles de la venta, como la especie, categoría, cantidad y peso aproximado."
            ),
            FaqItem(
                question = "¿Dónde veo el historial de mis ventas?",
                answer = "Dentro de la sección 'Ventas', puedes acceder al historial pulsando el icono en la esquina superior derecha."
            )
        )
    )
)

@Composable
fun HelpScreen(
    onBackPress: () -> Unit,
    onNavigateToTickets: () -> Unit
) {
    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            MinimalHeader(
                title = "Centro de Ayuda",
                onBackPress = onBackPress,
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Preguntas Frecuentes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(faqData) { category ->
                FaqCategoryItem(category = category)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToTickets,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Mis Consultas (Soporte)")
                }
            }
        }
    }
}

@Composable
fun FaqCategoryItem(category: FaqCategory) {
    Column {
        Text(
            text = category.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        category.items.forEach { faqItem ->
            FaqItemView(item = faqItem)
            Divider()
        }
    }
}

@Composable
fun FaqItemView(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Contraer" else "Expandir"
            )
        }

        AnimatedVisibility(visible = expanded) {
            Text(
                text = item.answer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
