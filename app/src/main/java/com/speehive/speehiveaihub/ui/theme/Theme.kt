package com.speehive.speehiveaihub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable


private val PulseAIColorScheme = darkColorScheme(
    primary = PulseGreen,
    secondary = PulseAmber,
    tertiary = PulseBlue,
    background = PureBlack,
    surface = CardSurface,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
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
