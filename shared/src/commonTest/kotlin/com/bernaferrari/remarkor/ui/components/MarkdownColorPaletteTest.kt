package com.bernaferrari.remarkor.ui.components

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

class MarkdownColorPaletteTest {
    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance() + 0.05f
        val lb = b.luminance() + 0.05f
        return max(la, lb) / min(la, lb)
    }

    @Test
    fun inlineCodeIsReadableOnLightAndDarkNotes() {
        val cases = listOf(
            lightColorScheme() to Color(0xFFFFF8B8), // yellow note
            lightColorScheme() to Color(0xFFD4E4ED), // blue note
            lightColorScheme() to Color(0xFFFFFBFE), // plain light surface
            darkColorScheme() to Color(0xFF3B3A2E), // dark tinted note
            darkColorScheme() to Color(0xFF1C1B1F), // plain dark surface
        )
        for ((scheme, background) in cases) {
            val palette = resolveMarkdownColorPalette(scheme, background)
            val chip = palette.codeBackground.compositeOver(background)
            val ratio = contrast(palette.codeText.compositeOver(chip), chip)
            assertTrue(ratio >= 4.5f, "Inline code contrast $ratio on $background")
        }
    }
}
