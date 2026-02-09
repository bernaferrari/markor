package net.gsantner.markor.ui.components
 
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Check if we're on a large screen (tablet) without needing WindowSizeClass.
 * Uses Configuration directly which is simpler and works everywhere.
 */
@Composable
fun isLargeScreen(): Boolean {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        val screenWidthDp = configuration.screenWidthDp
        val screenHeightDp = configuration.screenHeightDp
        
        // Consider large screen if width >= 600dp (standard tablet threshold)
        screenWidthDp >= 600 && screenHeightDp >= 400
    }
}

/**
 * Simple adaptive layout helper without WindowSizeClass dependency.
 */
@Composable
fun rememberAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        val screenWidthDp = configuration.screenWidthDp
        val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
        
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
