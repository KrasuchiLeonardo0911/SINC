package com.sinc.mobile.app.features.maquetas

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sinc.mobile.R // Importado para recursos

@Composable
fun JournalScreen() {
    Scaffold(
        containerColor = SoftGray,
        bottomBar = { FloatingBottomNavBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Header()
            Spacer(modifier = Modifier.height(24.dp))
            WeekdaySelector()
            Spacer(modifier = Modifier.height(24.dp))
            MyJournalSection()
            Spacer(modifier = Modifier.height(24.dp))
            QuickJournalSection()
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hi, Jose Maria",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
        // Reemplazo temporal si no tienes la imagen
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "User", tint = Color.White)
        }

        /* CÓDIGO ORIGINAL (Úsalo cuando tengas la imagen en res/drawable)
        Image(
            painter = painterResource(id = R.drawable.logoovinos),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        */
    }
}

@Composable
fun WeekdaySelector() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dates = listOf("7", "8", "9", "10", "11", "12", "13")
    val selectedDate = "10"

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { day ->
                Text(text = day, style = MaterialTheme.typography.bodySmall.copy(color = DarkerGray))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dates.forEach { date ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (date == selectedDate) AccentOrange else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (date == selectedDate) Color.White else Color.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MyJournalSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Journal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "See all",
                style = MaterialTheme.typography.bodyMedium.copy(color = AccentOrange)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MyJournalCard()
            }
            item {
                EveningCard()
            }
        }
    }
}

@Composable
fun MyJournalCard() {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(180.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.tarjeta_crear_campos),
                contentDescription = "Card Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Scrim for text readability at the top
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent), // Darker at top, transparent at bottom
                            endY = 300f // Controls how far down the gradient extends
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top // Align text to the top
            ) {
                Text(
                    text = "Crea y Geolocaliza tu Campo",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Registra tus unidades productivas en el mapa.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
        }
    }
}

@Composable
fun EveningCard() {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF0EAE2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Evening",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.rotate(-90f)
            )
        }
    }
}

@Composable
fun QuickJournalSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Journal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "See all",
                style = MaterialTheme.typography.bodyMedium.copy(color = AccentOrange)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                QuickJournalCard(
                    backgroundColor = PalePink,
                    icon = Icons.Default.Home,
                    title = "Pause & reflect",
                    question = "What are you grateful for today?",
                    tags = listOf("Today", "Personal")
                )
            }
            item {
                QuickJournalCard(
                    backgroundColor = SoftLilac,
                    icon = Icons.Default.Person,
                    title = "Set Intentions",
                    question = "How do you want to feel?",
                    tags = listOf("Today", "Family")
                )
            }
            item {
                QuickJournalCard(
                    backgroundColor = MintGreen,
                    icon = Icons.Default.Notifications,
                    title = "Embrace Chan...",
                    question = "Let go and ...",
                    tags = listOf("Today")
                )
            }
        }
    }
}

@Composable
fun QuickJournalCard(
    backgroundColor: Color,
    icon: ImageVector,
    title: String,
    question: String,
    tags: List<String>
) {
    Card(
        modifier = Modifier.size(160.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = question, style = MaterialTheme.typography.bodyLarge)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    Chip(text = tag)
                }
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun FloatingBottomNavBar() {
    var selectedItem by remember { mutableStateOf("Home") }
    val items = listOf("Home", "Explore", "", "Journey", "Profile")

    // Mapa corregido con los iconos de Material Icons
    val icons = mapOf(
        "Home" to Icons.Default.Home,
        "Explore" to Icons.Default.LocationOn,
        "Journey" to Icons.Default.Article,
        "Profile" to Icons.Default.Person
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .height(64.dp),
            containerColor = Color.White,
            tonalElevation = 8.dp
        ) {
            items.forEach { item ->
                val isSelected = selectedItem == item
                if (item.isEmpty()) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { },
                        enabled = false
                    )
                } else {
                    NavigationBarItem(
                        icon = {
                            // Uso seguro del mapa
                            icons[item]?.let { iconVector ->
                                Icon(
                                    iconVector,
                                    contentDescription = item,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = { Text(item, style = MaterialTheme.typography.labelSmall) },
                        selected = isSelected,
                        onClick = { selectedItem = item },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            unselectedIconColor = DarkerGray,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = DarkerGray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { /*TODO*/ },
            shape = CircleShape,
            containerColor = AccentOrange,
            modifier = Modifier.offset(y = (-8).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun JournalScreenPreview() {
    JournalScreen()
}