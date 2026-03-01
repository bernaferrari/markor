package com.bernaferrari.remarkor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Screen dimensions for adaptive layout calculations.
 */
data class ScreenDimensions(
    val widthDp: Int,
    val heightDp: Int
)

/**
 * Expect function to get screen dimensions - platform-specific implementation.
 */
@Composable
expect fun rememberScreenDimensions(): ScreenDimensions

/**
 * Check if we're on a large screen (tablet) without needing WindowSizeClass.
 */
@Composable
fun isLargeScreen(): Boolean {
    val dimensions = rememberScreenDimensions()
    return remember(dimensions) {
        // Consider large screen if width >= 600dp (standard tablet threshold)
        dimensions.widthDp >= 600 && dimensions.heightDp >= 400
    }
}

/**
 * Simple adaptive layout helper without WindowSizeClass dependency.
 */
@Composable
fun rememberAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
    val dimensions = rememberScreenDimensions()
    return remember(dimensions) {
        val screenWidthDp = dimensions.widthDp
        val isLandscape = dimensions.widthDp > dimensions.heightDp

        AdaptiveLayoutInfo(
            isLargeScreen = screenWidthDp >= 600,
            isExpandedScreen = screenWidthDp >= 840,
            isLandscape = isLandscape,
            showNavigationRail = screenWidthDp >= 600,
            showDualPane = screenWidthDp >= 840 || (isLandscape && screenWidthDp >= 600),
            listPaneWeight = if (screenWidthDp >= 840) 0.35f else 0.4f,
            detailPaneWeight = if (screenWidthDp >= 840) 0.65f else 0.6f
        )
    }
}

data class AdaptiveLayoutInfo(
    val isLargeScreen: Boolean,
    val isExpandedScreen: Boolean,
    val isLandscape: Boolean,
    val showNavigationRail: Boolean,
    val showDualPane: Boolean,
    val listPaneWeight: Float,
    val detailPaneWeight: Float
) {
    companion object {
        val PHONE = AdaptiveLayoutInfo(
            isLargeScreen = false,
            isExpandedScreen = false,
            isLandscape = false,
            showNavigationRail = false,
            showDualPane = false,
            listPaneWeight = 1f,
            detailPaneWeight = 1f
        )

        val TABLET_LANDSCAPE = AdaptiveLayoutInfo(
            isLargeScreen = true,
            isExpandedScreen = true,
            isLandscape = true,
            showNavigationRail = true,
            showDualPane = true,
            listPaneWeight = 0.35f,
            detailPaneWeight = 0.65f
        )
    }
}
