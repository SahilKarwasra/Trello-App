package com.laarasoft.frontend.core.utils.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

fun Modifier.shimmer(
    baseColor: Color? = null,
    highlightColor: Color? = null,
    durationMillis: Int = 1200
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val actualBase = baseColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val actualHighlight = highlightColor ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)

    val brush = if (size.width > 0) {
        val width = size.width.toFloat()
        val offset = (translateAnim / 1000f) * (width * 2) - width
        Brush.linearGradient(
            colors = listOf(
                actualBase,
                actualHighlight,
                actualBase
            ),
            start = Offset(offset, 0f),
            end = Offset(offset + width, size.height.toFloat())
        )
    } else {
        Brush.linearGradient(
            colors = listOf(actualBase, actualBase)
        )
    }

    this
        .onGloballyPositioned { size = it.size }
        .background(brush)
}
