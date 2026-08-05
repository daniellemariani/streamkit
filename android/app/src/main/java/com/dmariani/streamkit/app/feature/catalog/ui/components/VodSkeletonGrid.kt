package com.dmariani.streamkit.app.feature.catalog.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import com.dmariani.streamkit.core.ui.theme.Radius
import com.dmariani.streamkit.core.ui.theme.Spacing
import com.dmariani.streamkit.core.ui.theme.SurfaceAlt
import com.dmariani.streamkit.core.ui.theme.SurfaceCard

private const val SHIMMER_DURATION_MS = 1200
private const val SKELETON_ROWS = 3
private const val SKELETON_COLUMNS = 2

/**
 * Fixed 2-column, 3-row grid of shimmering placeholder cards shown while
 * the VOD grid is loading. Non-lazy — placed as a single full-span item
 * inside `CatalogScreen`'s root `LazyVerticalGrid`.
 */
@Composable
fun VodSkeletonGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        repeat(SKELETON_ROWS) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                repeat(SKELETON_COLUMNS) {
                    ShimmerCard(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ShimmerCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "vodSkeletonShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(Radius.lg))
            .drawWithContent {
                val bandWidth = size.width
                val xOffset = -bandWidth + progress * (size.width + bandWidth)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SurfaceAlt, SurfaceCard, SurfaceAlt),
                        startX = xOffset,
                        endX = xOffset + bandWidth,
                    ),
                )
            },
    )
}
