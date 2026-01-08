package com.sinc.mobile.app.features.home.mainscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.R

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun MyJournalSection(
    onStockClick: () -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onCamposClick: () -> Unit,
) {
    Column {
        // The illustration card placeholder, centered
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            item {
                MyJournalCard(onCamposClick = onCamposClick)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions row below the card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround // Space them out evenly
        ) {
            // Ver Stock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onStockClick)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Ver Stock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "Ver Stock", style = MaterialTheme.typography.bodySmall)
            }
            // Agregar Stock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAddClick)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Stock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "Agregar Stock", style = MaterialTheme.typography.bodySmall)
            }
            // Ver Historial
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onHistoryClick)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Ver Historial",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "Ver Historial", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun MyJournalCard(onCamposClick: () -> Unit) {
    // This card now only holds the placeholder and the button
    Card(
        modifier = Modifier
            .width(320.dp)
            .height(250.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // New Illustration
            Image(
                painter = painterResource(id = R.drawable.campo_acuarela),
                contentDescription = "Main Screen Illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Scrim for text readability over the image (if needed, otherwise remove)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                            endY = 400f
                        )
                    )
            )

            // Button on the bottom right
            Button(
                onClick = onCamposClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), // Reduce internal padding
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // White background
                    contentColor = MaterialTheme.colorScheme.primary // Primary color text
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Mis Campos")
            }
        }
    }
}