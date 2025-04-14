package org.example.openaitts.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColorPalette(
    val profitGreen: Color = Color.Unspecified,
    val lossRed: Color = Color.Unspecified,
)

val lightProfitGreenColor = Color(color = 0xFF32de84)
val lightLossRedColor = Color(color = 0xFFD2122E)

val darkProfitGreenColor = Color(color = 0xFF32de84)
val darkLossRedColor = Color(color = 0xFFD2122E)

val lightAppColorPalette = AppColorPalette(
    profitGreen = lightProfitGreenColor,
    lossRed = lightLossRedColor,
)

val darkAppColorPalette = AppColorPalette(
    profitGreen = darkProfitGreenColor,
    lossRed = darkLossRedColor,
)

val localAppColorPalette = compositionLocalOf { AppColorPalette() }