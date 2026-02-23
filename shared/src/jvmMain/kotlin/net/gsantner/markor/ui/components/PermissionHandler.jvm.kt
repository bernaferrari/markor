package net.gsantner.markor.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun HandleStoragePermissions(
    onRequest: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    // JVM/Desktop doesn't need storage permissions
    // Just call onGranted immediately
    if (onRequest) {
        onGranted()
    }
}
