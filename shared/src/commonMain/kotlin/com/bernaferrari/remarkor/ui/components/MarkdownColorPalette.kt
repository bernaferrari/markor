package com.bernaferrari.remarkor.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class MarkdownColorPalette(
    val body: Color,
    val accent: Color,
    val subtle: Color,
    val codeBackground: Color,
    val codeText: Color
)

fun resolveMarkdownColorPalette(
    colorScheme: ColorScheme,
    backgroundColor: Color,
    accentColorOverride: Color? = null
): MarkdownColorPalette {
    val bodyFallback = bestContrastColor(
        backgroundColor,
        listOf(colorScheme.onSurface, colorScheme.onBackground, Color.White, Color.Black)
    )
    val body = ensureMinContrast(
        preferred = colorScheme.onSurface,
        background = backgroundColor,
        fallback = bodyFallback,
        minRatio = 4.5f
    )

    val preferredAccent = accentColorOverride ?: colorScheme.primary
    val accentFallback = bestContrastColor(
        backgroundColor,
        listOf(
            preferredAccent,
            colorScheme.primary,
            colorScheme.tertiary,
            body,
            Color.White,
            Color.Black
        )
    )
    val accent = ensureMinContrast(
        preferred = preferredAccent,
        background = backgroundColor,
        fallback = accentFallback,
        minRatio = 3.0f
    )

    val subtleFallback = bestContrastColor(
        backgroundColor,
        listOf(colorScheme.onSurfaceVariant, body, Color.White, Color.Black)
    )
    val subtle = ensureMinContrast(
        preferred = colorScheme.onSurfaceVariant,
        background = backgroundColor,
        fallback = subtleFallback,
        minRatio = 3.0f
    )

    val codeBackground = if (relativeLuminance(backgroundColor) < 0.5f) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }
    val codeTextFallback = bestContrastColor(
        codeBackground,
        listOf(subtle, body, Color.White, Color.Black)
    )
    val codeText = ensureMinContrast(
        preferred = subtle,
        background = codeBackground,
        fallback = codeTextFallback,
        minRatio = 4.5f
    )

    return MarkdownColorPalette(
        body = body,
        accent = accent,
        subtle = subtle,
        codeBackground = codeBackground,
        codeText = codeText
    )
}

private fun bestContrastColor(background: Color, candidates: List<Color>): Color {
    return candidates.maxByOrNull { contrastRatio(it, background) } ?: Color.Unspecified
}

private fun ensureMinContrast(
    preferred: Color,
    background: Color,
    fallback: Color,
    minRatio: Float
): Color {
    val preferredRatio = contrastRatio(preferred, background)
    if (preferredRatio >= minRatio) return preferred

    val fallbackRatio = contrastRatio(fallback, background)
    return if (fallbackRatio > preferredRatio) fallback else preferred
}

private fun contrastRatio(foreground: Color, background: Color): Float {
    val foregroundLuminance = relativeLuminance(foreground.compositeOver(background)) + 0.05f
    val backgroundLuminance = relativeLuminance(background) + 0.05f
    return max(foregroundLuminance, backgroundLuminance) / min(
        foregroundLuminance,
        backgroundLuminance
    )
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
