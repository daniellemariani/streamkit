package com.dmariani.streamkit.app.feature.catalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.dmariani.streamkit.core.domain.model.Video
import com.dmariani.streamkit.core.ui.components.LiveBadge
import com.dmariani.streamkit.core.ui.theme.Radius
import com.dmariani.streamkit.core.ui.theme.Spacing
import com.dmariani.streamkit.core.ui.theme.StreamKitTypography
import com.dmariani.streamkit.core.ui.theme.SurfaceAlt
import com.dmariani.streamkit.core.ui.theme.SurfaceCard
import com.dmariani.streamkit.core.ui.theme.TextPrimary

/**
 * 16:9 card for a Live carousel item — thumbnail with a bottom scrim
 * for label legibility, a Live badge in the top-left corner, and the
 * stream name in the bottom-left corner.
 */
@Composable
fun LiveCarouselCard(video: Video, onTap: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(Radius.lg))
            .background(SurfaceCard)
            .clickable { onTap(video.id) },
    ) {
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = null,
            placeholder = ColorPainter(SurfaceAlt),
            error = ColorPainter(SurfaceAlt),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.6f),
                        ),
                    ),
                ),
        )
        LiveBadge(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(Spacing.sm),
        )
        Text(
            text = video.title,
            style = StreamKitTypography.Label,
            color = TextPrimary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.sm),
        )
    }
}
