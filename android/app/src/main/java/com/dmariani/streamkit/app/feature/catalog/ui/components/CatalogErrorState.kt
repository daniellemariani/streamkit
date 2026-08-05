package com.dmariani.streamkit.app.feature.catalog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dmariani.streamkit.app.R
import com.dmariani.streamkit.core.ui.theme.AccentOn
import com.dmariani.streamkit.core.ui.theme.AccentPrimary
import com.dmariani.streamkit.core.ui.theme.SemanticError
import com.dmariani.streamkit.core.ui.theme.Spacing
import com.dmariani.streamkit.core.ui.theme.StreamKitTypography
import com.dmariani.streamkit.core.ui.theme.TextPrimary

/**
 * Shown when the VOD catalog fetch fails and no prior cache exists —
 * full-section error state with a retry affordance.
 */
@Composable
fun CatalogErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = SemanticError,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(R.string.catalog_vod_error_message),
            style = StreamKitTypography.Body,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
        ) {
            Text(
                text = stringResource(R.string.catalog_vod_error_retry),
                color = AccentOn,
            )
        }
    }
}
