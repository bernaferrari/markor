package com.bernaferrari.remarkor.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Platform-specific dynamic color scheme provider.
 * On Android 12+, this uses the system's dynamic colors.
 * On other platforms, returns null to fall back to static colors.
 */
@Composable
expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?
