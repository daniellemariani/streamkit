package com.dmariani.streamkit.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dmariani.streamkit.core.R
import com.dmariani.streamkit.core.ui.theme.Radius
import com.dmariani.streamkit.core.ui.theme.SemanticError
import com.dmariani.streamkit.core.ui.theme.Spacing
import com.dmariani.streamkit.core.ui.theme.StreamKitTypography
import com.dmariani.streamkit.core.ui.theme.TextPrimary

/**
 * Solid-red "Live" pill marking a Live carousel item as currently
 * broadcasting. Display-only — not interactive. Lives in `core` so it can
 * be reused by the Live Player overlay without `core` depending on `app`.
 */
@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.live_badge_label),
        style = StreamKitTypography.Label,
        color = TextPrimary,
        modifier = modifier
            .background(color = SemanticError, shape = RoundedCornerShape(Radius.sm))
            .padding(vertical = Spacing.xs, horizontal = Spacing.sm),
    )
}
