package com.bernaferrari.remarkor.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // JVM/Desktop doesn't have a standard back button
    // Users navigate back via UI controls
}
