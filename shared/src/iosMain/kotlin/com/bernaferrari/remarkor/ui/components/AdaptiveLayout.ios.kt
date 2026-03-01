package com.bernaferrari.remarkor.ui.components

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberScreenDimensions(): ScreenDimensions {
    val screen = UIScreen.mainScreen
    val bounds = screen.bounds

    // Use CoreGraphics functions to get width and height
    val widthDp = CGRectGetWidth(bounds).toInt().coerceIn(320, 2048)
    val heightDp = CGRectGetHeight(bounds).toInt().coerceIn(480, 2732)

    return ScreenDimensions(
        widthDp = widthDp,
        heightDp = heightDp
    )
}
