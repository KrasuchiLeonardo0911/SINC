package com.sinc.mobile.app.features.home.mainscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.shimmer

@Composable
fun MainContentSkeleton(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val shimmerHighlight = PlaceholderHighlight.shimmer(
        highlightColor = Color.White.copy(alpha = 0.5f)
    )

    Column(
        modifier = modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Box for Header and WeekdaySelector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header placeholder
                Spacer(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(0.6f)
                        .placeholder(visible = true, highlight = shimmerHighlight)
                )
            }
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                // WeekdaySelector placeholder
                Spacer(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .placeholder(visible = true, highlight = shimmerHighlight)
                )
            }
        }
        // Box for MyJournalSection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // MyJournalCard placeholder
            Spacer(
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
                    .placeholder(visible = true, highlight = shimmerHighlight)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Action buttons placeholder
            Spacer(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
                    .placeholder(visible = true, highlight = shimmerHighlight)
            )
        }
        // Box for QuickJournalSection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .placeholder(visible = true, highlight = shimmerHighlight)
            )
        }
    }
}
