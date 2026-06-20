package com.bernaferrari.remarkor.ui.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
actual fun rememberScreenDimensions(): ScreenDimensions =
    ScreenDimensions(
        widthDp = window.innerWidth.coerceAtLeast(320),
        heightDp = window.innerHeight.coerceAtLeast(480),
    )