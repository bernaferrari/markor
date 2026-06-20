package com.bernaferrari.remarkor.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.bernaferrari.remarkor.AppContent
import com.bernaferrari.remarkor.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport {
        AppContent(
            systemInternalFilesDir = "/Notebook",
            onExit = { /* Web apps stay in the browser tab */ },
        )
    }
}