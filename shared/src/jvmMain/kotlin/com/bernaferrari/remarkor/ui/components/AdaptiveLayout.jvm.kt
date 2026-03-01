package com.bernaferrari.remarkor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import java.awt.Toolkit

@Composable
actual fun rememberScreenDimensions(): ScreenDimensions {
    val toolkit = Toolkit.getDefaultToolkit()
    val screenSize = toolkit.screenSize
    return ScreenDimensions(
        widthDp = (screenSize.width / toolkit.screenResolution * 160).coerceAtLeast(320),
        heightDp = (screenSize.height / toolkit.screenResolution * 160).coerceAtLeast(480)
    )
}
