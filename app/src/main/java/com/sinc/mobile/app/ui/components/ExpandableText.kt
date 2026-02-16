package com.sinc.mobile.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ExpandableText(
    text: String,
    prefix: String = "",
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .animateContentSize()
            .clickable(enabled = isOverflowing || isExpanded) { isExpanded = !isExpanded }
    ) {
        val fullText = if (prefix.isNotEmpty()) "$prefix $text" else text
        
        Text(
            text = fullText,
            color = color,
            fontWeight = fontWeight,
            style = style,
            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded) {
                    isOverflowing = textLayoutResult.hasVisualOverflow
                }
            }
        )
    }
}
