package com.bernaferrari.remarkor.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.pow

fun resolveNoteSurfaceColor(
    noteColor: Int?,
    colorScheme: ColorScheme,
    fallback: Color = colorScheme.surfaceContainerLow
): Color {
    val isLegacyDefaultBlack = noteColor == 0xFF000000.toInt()
    if (noteColor == null || isLegacyDefaultBlack) {
        return fallback
    }

    val base = Color(noteColor)
    val isDarkTheme = relativeLuminance(colorScheme.background) < 0.5f
    return if (isDarkTheme) {
        // Keep note surfaces dark in dark mode so foreground text remains high-contrast.
        lerp(base, Color.Black, 0.58f)
    } else {
        // Keep color identity while softening slightly in light mode.
        lerp(base, Color.White, 0.12f)
    }
}

private fun relativeLuminance(color: Color): Float {
    fun channel(c: Float): Float {
        return if (c <= 0.03928f) c / 12.92f else (((c + 0.055f) / 1.055f).toDouble()
            .pow(2.4)).toFloat()
    }

    val r = channel(color.red)
    val g = channel(color.green)
    val b = channel(color.blue)
    return (0.2126f * r) + (0.7152f * g) + (0.0722f * b)
}
