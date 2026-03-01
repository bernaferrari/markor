package com.bernaferrari.remarkor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * iOS implementation of storage permissions handler.
 * iOS doesn't require explicit storage permissions for app sandbox access.
 * For accessing files outside the sandbox, iOS uses UIDocumentPickerViewController
 * which handles permissions automatically.
 */
@Composable
actual fun HandleStoragePermissions(
    onRequest: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    // On iOS, we don't need storage permissions for app sandbox
    // Files outside sandbox are accessed via document picker which handles permissions
    LaunchedEffect(onRequest) {
        if (onRequest) {
            onGranted()
        }
    }
}
