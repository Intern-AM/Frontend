package com.speehive.speehiveaihub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val PulseAIColorScheme = lightColorScheme(
    primary = PulseGreen,
    secondary = PulseAmber,
    tertiary = PulseBlue,
    background = AppBackground,
    surface = CardSurface,
    onPrimary = AppBackground,
    onSecondary = AppBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = PulseRed
)


@Composable
fun SpeehiveAIHubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PulseAIColorScheme,
        typography = PulseTypography,
        content = content
    )
}
