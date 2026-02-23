package net.gsantner.markor.ui.screens.editor

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // iOS doesn't have a hardware back button, so we don't need to handle it
    // Users navigate back via the on-screen back button
}
