package net.gsantner.markor.ui.screens.editor

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // JVM/Desktop doesn't have a standard back button
    // Users navigate back via UI controls
}
