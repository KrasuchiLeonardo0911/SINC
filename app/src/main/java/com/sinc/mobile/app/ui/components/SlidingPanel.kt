package com.sinc.mobile.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val ANIMATION_DURATION = 300

@Composable
fun SlidingPanel(
    showPanel: Boolean,
    onDismiss: () -> Unit,
    onFullyOpen: () -> Unit,
    panelWidth: Dp = 400.dp,
    handleWidth: Dp = 60.dp,
    handleContent: @Composable BoxScope.() -> Unit,
    panelContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val screenWidthPx = with(LocalDensity.current) { panelWidth.toPx() }
    val handleWidthPx = with(LocalDensity.current) { handleWidth.toPx() }

    val offsetX = remember { Animatable(screenWidthPx) }

    LaunchedEffect(showPanel) {
        if (showPanel) {
            offsetX.animateTo(
                targetValue = screenWidthPx - handleWidthPx,
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            )
        } else {
            offsetX.animateTo(
                targetValue = screenWidthPx,
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            )
        }
    }

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            offsetX.snapTo((offsetX.value + delta).coerceAtMost(screenWidthPx))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        mainContent()

        // Scrim to dismiss the panel on outside click
        if (showPanel && offsetX.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // No ripple effect
                        onClick = onDismiss
                    )
            )
        }

        // Draggable container for the sliding panel and handle
        Box(
            Modifier
                .fillMaxHeight()
                .width(panelWidth)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = draggableState,
                    enabled = offsetX.value > 0f, // Only allow dragging if not fully open
                    onDragStopped = {
                        coroutineScope.launch {
                            // If dragged more than 40% of the handle's width
                            if (offsetX.value < screenWidthPx - (handleWidthPx * 1.4f)) {
                                // Animate fully into view
                                offsetX.animateTo(0f, animationSpec = tween(250)) {
                                    // When animation finishes, invoke the callback
                                    if (value == 0f) onFullyOpen()
                                }
                            } else {
                                // Animate back to peeking position
                                offsetX.animateTo(
                                    screenWidthPx - handleWidthPx,
                                    animationSpec = tween(ANIMATION_DURATION)
                                )
                            }
                        }
                    }
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                panelContent()
            }

            // Only show the handle if the panel is not fully open
            if (offsetX.value > 0f && showPanel) {
                Box(
                    modifier = Modifier.offset { IntOffset(-handleWidthPx.roundToInt(), 0) }
                ) {
                    handleContent()
                }
            }
        }
    }
}
