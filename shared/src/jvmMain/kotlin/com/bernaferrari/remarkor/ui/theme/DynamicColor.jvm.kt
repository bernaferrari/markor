package com.bernaferrari.remarkor.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * JVM/Desktop implementation of dynamic color scheme.
 * Returns null as dynamic colors are not supported on desktop.
 */
@Composable
actual fun dynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    return null
}
