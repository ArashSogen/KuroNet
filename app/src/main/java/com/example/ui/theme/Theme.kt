package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KuroDarkColorScheme = darkColorScheme(
    primary = ToriiRed,
    onPrimary = PureWhite,
    primaryContainer = ToriiRedDark,
    onPrimaryContainer = PureWhite,
    secondary = SakuraPink,
    onSecondary = SumiBlack,
    secondaryContainer = SumiCardElevated,
    onSecondaryContainer = ZenWhite,
    tertiary = BambooGreen,
    onTertiary = SumiBlack,
    tertiaryContainer = LanternAmber,
    onTertiaryContainer = SumiBlack,
    background = SumiBlack,
    onBackground = ZenWhite,
    surface = SumiSurface,
    onSurface = ZenWhite,
    surfaceVariant = SumiCard,
    onSurfaceVariant = ZenGray,
    outline = SumiBorder,
    outlineVariant = SumiBorderSubtle,
    inverseSurface = ZenWhite,
    inverseOnSurface = SumiBlack,
    error = ToriiRed,
    onError = PureWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KuroDarkColorScheme,
        typography = Typography,
        content = content
    )
}
