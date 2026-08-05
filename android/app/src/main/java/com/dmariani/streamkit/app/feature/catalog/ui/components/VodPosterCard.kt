package com.dmariani.streamkit.app.feature.catalog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.dmariani.streamkit.core.domain.model.Video
import com.dmariani.streamkit.core.ui.theme.Radius
import com.dmariani.streamkit.core.ui.theme.Spacing
import com.dmariani.streamkit.core.ui.theme.StreamKitTypography
import com.dmariani.streamkit.core.ui.theme.SurfaceAlt
import com.dmariani.streamkit.core.ui.theme.TextPrimary

/**
 * 2:3 poster card for a VOD grid item — thumbnail image with a title
 * label below.
 */
@Composable
fun VodPosterCard(video: Video, onTap: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap(video.id) },
    ) {
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = null,
            placeholder = ColorPainter(SurfaceAlt),
            error = ColorPainter(SurfaceAlt),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(Radius.lg)),
        )
        Text(
            text = video.title,
            style = StreamKitTypography.BodySmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}
