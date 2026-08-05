package com.dmariani.streamkit.app.feature.catalog.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dmariani.streamkit.app.R
import com.dmariani.streamkit.core.ui.theme.Background
import com.dmariani.streamkit.core.ui.theme.StreamKitTypography
import com.dmariani.streamkit.core.ui.theme.TextPrimary
import com.dmariani.streamkit.core.ui.theme.TextSecondary

/**
 * Catalog screen's top app bar — app title and a Settings entry point.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopAppBar(onSettingsTapped: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.catalog_app_bar_title),
                style = StreamKitTypography.Heading1,
                color = TextPrimary,
            )
        },
        actions = {
            IconButton(onClick = onSettingsTapped) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.catalog_settings_icon_description),
                    modifier = Modifier.size(20.dp),
                    tint = TextSecondary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
    )
}
