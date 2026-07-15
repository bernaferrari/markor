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

        // Keep-style: one full-width notebook grid at every size. Notes open in a dialog
        // on large screens — no list-detail split / empty "select a file" pane.
        AdaptiveLayoutInfo(
            isLargeScreen = screenWidthDp >= 600,
            isExpandedScreen = screenWidthDp >= 840,
            isLandscape = isLandscape,
            showNavigationRail = screenWidthDp >= 600,
            showDualPane = false,
            listPaneWeight = 1f,
            detailPaneWeight = 0f,
        )
    }
}

data class AdaptiveLayoutInfo(
    val isLargeScreen: Boolean,
    val isExpandedScreen: Boolean,
    val isLandscape: Boolean,
    val showNavigationRail: Boolean,
    /** Deprecated: always false — Keep dialog replaces list-detail. */
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
            detailPaneWeight = 0f,
        )

        val TABLET_LANDSCAPE = AdaptiveLayoutInfo(
            isLargeScreen = true,
            isExpandedScreen = true,
            isLandscape = true,
            showNavigationRail = true,
            showDualPane = false,
            listPaneWeight = 1f,
            detailPaneWeight = 0f,
        )
    }
}
