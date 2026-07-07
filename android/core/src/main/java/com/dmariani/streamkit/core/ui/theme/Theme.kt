package com.dmariani.streamkit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StreamKitColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = AccentOn,
    background = Background,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    error = SemanticError,
    onError = AccentOn,
)

private val StreamKitMaterialTypography = Typography(
    titleLarge = StreamKitTypography.Heading1,
    titleMedium = StreamKitTypography.Heading2,
    bodyLarge = StreamKitTypography.Body,
    bodySmall = StreamKitTypography.BodySmall,
    labelSmall = StreamKitTypography.Label,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StreamKitColorScheme,
        typography = StreamKitMaterialTypography,
        content = content,
    )
}
