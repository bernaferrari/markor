package com.bernaferrari.remarkor.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Scrim behind modal sheets. The Material default (32% black) turns pastel note colors muddy in
 * light themes, so light themes get a lighter veil that keeps the note's color; dark themes keep a
 * deeper one so the sheet still stands out.
 */
@Composable
fun sheetScrimColor(): Color {
    val scheme = MaterialTheme.colorScheme
    val isLight = scheme.background.luminance() > 0.5f
    return scheme.scrim.copy(alpha = if (isLight) 0.16f else 0.5f)
}
