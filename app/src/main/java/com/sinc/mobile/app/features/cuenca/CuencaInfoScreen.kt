package com.sinc.mobile.app.features.cuenca

import androidx.compose.foundation.Image

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Email

import androidx.compose.material.icons.filled.Phone

import androidx.compose.material3.Button

import androidx.compose.material3.Card

import androidx.compose.material3.CardDefaults

import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalUriHandler

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.navigation.NavController

import com.sinc.mobile.R

import com.sinc.mobile.app.ui.components.MinimalHeader

@Composable
fun CuencaInfoScreen(navController: NavController) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Más Info",
                onBackPress = { navController.popBackStack() },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Soluciones para el Productor y la Cuenca",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Esta aplicación móvil es una herramienta de la Mesa de Gestión de la Cuenca Ovino-Caprina para atender los desafíos del sector en el sur de Misiones.\n\nA través de esta plataforma, los productores pueden realizar una actualización en tiempo real de sus datos productivos, como el stock ganadero. La app facilita el monitoreo integrado y la georreferenciación de los campos, permitiendo un acceso centralizado a la información para técnicos y la Mesa de Gestión, apoyando así la toma de decisiones estratégicas para el desarrollo sostenible de la región.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val uriHandler = LocalUriHandler.current
                        val url = "https://sicsurmisiones.online/cuenca-misiones"
                        Button(
                            onClick = { uriHandler.openUri(url) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver página completa")
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Impulsado por",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val logos = listOf(
                                com.sinc.mobile.R.drawable.logoovinos, // New logo, first position
                                com.sinc.mobile.R.drawable.inta1,     // Second position
                                com.sinc.mobile.R.drawable.camca,
                                com.sinc.mobile.R.drawable.guayra1,
                                com.sinc.mobile.R.drawable.normal10,
                                com.sinc.mobile.R.drawable.unam
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(logos) { logoResId ->
                                    Image(
                                        painter = painterResource(id = logoResId),
                                        contentDescription = null,
                                        modifier = Modifier.height(60.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Footer Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Desarrollado por",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Leonardo Krasuchi",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Inner Column to group and center the contact rows as a block
                        Column(horizontalAlignment = Alignment.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("krasuchileonardo@gmail.com", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("3755571080", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
