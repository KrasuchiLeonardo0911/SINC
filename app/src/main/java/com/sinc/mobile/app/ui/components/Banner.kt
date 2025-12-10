package com.sinc.mobile.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sinc.mobile.ui.theme.colorAlta // Importar el nuevo color
import kotlinx.coroutines.delay
import java.util.UUID

enum class BannerType(val backgroundColor: Color, val textColor: Color) {
    SUCCESS(colorAlta, Color.White), // Usar colorAlta
    ERROR(Color(0xFFBA1A1A), Color.White)
}

data class BannerData(
    val message: String,
    val type: BannerType,
    val id: UUID = UUID.randomUUID()
)

// Global state holder for the banner.
// This makes it easy to show a banner from anywhere in the app.
object BannerManager {
    var bannerData by mutableStateOf<BannerData?>(null)
        private set

    fun show(message: String, type: BannerType) {
        bannerData = BannerData(message, type)
    }

    fun dismiss() {
        bannerData = null
    }
}

@Composable
fun GlobalBanner(
    modifier: Modifier = Modifier,
    durationMillis: Long = 5000
) {
    val managerBanner = BannerManager.bannerData
    var bannerToRender by remember { mutableStateOf(managerBanner) }

    // Update the banner to be rendered only when a new banner appears
    if (managerBanner != null) {
        bannerToRender = managerBanner
    }

    LaunchedEffect(managerBanner) {
        if (managerBanner != null) {
            delay(durationMillis)
            // Only dismiss if the banner hasn't been replaced by a new one
            if (BannerManager.bannerData?.id == managerBanner.id) {
                BannerManager.dismiss()
            }
        }
    }

    // Calculate the top padding dynamically to appear below the status bar and a typical top app bar
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 64.dp // Approximate height of the TopAppBar
    val totalPadding = statusBarHeight + topBarHeight + 8.dp // Add 8.dp margin

    AnimatedVisibility(
        visible = managerBanner != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
        // Use a longer fade out to make it feel smoother
        exit = fadeOut(animationSpec = tween(durationMillis = 1000)),
        modifier = modifier
    ) {
        // Use bannerToRender, which holds the data during the exit animation
        bannerToRender?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = totalPadding, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = it.type.backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = it.message,
                        color = it.type.textColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
