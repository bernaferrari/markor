package net.gsantner.markor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Helper class for A+ micro-interaction haptics.
 * Centralizes logic to allow enabling/disabling or tuning in future.
 */
class HapticHelper(private val hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    
    fun performLightClick() {
        // TextHandleMove is often a light tick on Android
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun performHeavyClick() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun performSuccess() {
        // ToggleOn is distinct
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) // Fallback if no specific success
    }
}

@Composable
fun rememberHapticHelper(): HapticHelper {
    val haptic = LocalHapticFeedback.current
    return androidx.compose.runtime.remember(haptic) {
        HapticHelper(haptic)
    }
}
