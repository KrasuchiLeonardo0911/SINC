package com.sinc.mobile.app.features.home.mainscreen.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import com.sinc.mobile.R

private data class DashboardPage(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val imageResId: Int,
    val imageAlignment: Alignment = Alignment.Center,
)

private val pages = listOf(
    DashboardPage(
        id = "pending",
        title = "Movimientos Pendientes",
        description = "Toca para ver los registros de stock que aún no se han guardado.",
        imageResId = R.drawable.ilustracion_movimientos,
        imageAlignment = Alignment.Center
    ),
    DashboardPage(
        id = "sales",
        title = "Historial de\nVentas",
        description = "Toca para consultar el resumen de ventas de este mes.",
        imageResId = R.drawable.ilustracion_ventas,
        imageAlignment = Alignment.BottomCenter
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SyncStatusDashboard(
    onPendingMovementsClick: () -> Unit,
    onSalesHistoryClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Dot Indicator at the top
        SyncPagerIndicator(
            pagerState = pagerState,
            pageCount = pages.size
        )

        // Horizontal Pager for Cards
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) { pageIndex ->
            val page = pages[pageIndex]
            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                PendingItemsCard(
                    title = page.title,
                    description = page.description,
                    imageResId = page.imageResId,
                    imageAlignment = page.imageAlignment,
                    imageModifier = Modifier
                        .size(170.dp)
                        .offset(
                            y = if (page.id == "sales") (7).dp else 0.dp //Altura de la ilustracion
                        )
                        .clip(MaterialTheme.shapes.medium),
                    onCardClick = {
                        if (page.id == "pending") {
                            onPendingMovementsClick()
                        } else {
                            onSalesHistoryClick()
                        }
                    }
                )
            }
        }
    }
}
 
@Composable
private fun PendingItemsCard(
    title: String,
    description: String,
    @DrawableRes imageResId: Int,
    imageAlignment: Alignment,
    imageModifier: Modifier = Modifier,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.large)
                .clickable { onCardClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: Text content
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(lineHeight = 20.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Column 2: Illustration
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = imageAlignment
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier
                )
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SyncPagerIndicator(pagerState: PagerState, pageCount: Int) {
    Row(
        Modifier
            .height(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) {
            iteration ->
            val color by animateColorAsState(
                targetValue = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray,
                animationSpec = tween(300), label = "indicator_color"
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(8.dp)
            )
        }
    }
}