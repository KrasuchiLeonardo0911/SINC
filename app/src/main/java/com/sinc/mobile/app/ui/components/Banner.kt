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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.UUID

enum class BannerType {
    SUCCESS,
    ERROR,
    WARNING;

    val backgroundColor: Color
        @Composable
        get() = when (this) {
            SUCCESS -> Color(0xFF2E7D32) // Dark Green
            ERROR -> Color(0xFFD32F2F)   // Material Red
            WARNING -> Color(0xFFFFA000) // Amber
        }

    val textColor: Color
        get() = Color.White
}

data class BannerData(
    val message: String,
    val type: BannerType,
    val id: UUID = UUID.randomUUID()
)

// Global state holder for the banner.
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
    durationMillis: Long = 4000 // Increased duration for warnings
) {
    val managerBanner = BannerManager.bannerData
    var bannerToRender by remember { mutableStateOf(managerBanner) }

    if (managerBanner != null) {
        bannerToRender = managerBanner
    }

    LaunchedEffect(managerBanner) {
        if (managerBanner != null) {
            delay(durationMillis)
            if (BannerManager.bannerData?.id == managerBanner.id) {
                BannerManager.dismiss()
            }
        }
    }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 48.dp // Corrected to use the compact header height
    val totalPadding = statusBarHeight + topBarHeight + 8.dp

    AnimatedVisibility(
        visible = managerBanner != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000)),
        modifier = modifier
    ) {
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
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
