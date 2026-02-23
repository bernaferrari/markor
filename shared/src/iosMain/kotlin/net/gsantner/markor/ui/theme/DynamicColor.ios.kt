package net.gsantner.markor.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * iOS implementation of dynamic color scheme.
 * iOS doesn't have an equivalent to Android 12+ Material You dynamic colors.
 * Returns null to fall back to static theme colors.
 * 
 * Note: iOS does have a system theme (light/dark), but color scheme
 * is handled by the MarkorTheme based on the system appearance.
 */
@Composable
actual fun dynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    // iOS doesn't support Material You dynamic colors
    // The dark/light theme switching is handled separately
    return null
}
