package com.bernaferrari.remarkor.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.bernaferrari.remarkor.AppContent
import com.bernaferrari.remarkor.di.initKoin
import com.bernaferrari.remarkor.platform.IosPlatformHolder
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    val controller = ComposeUIViewController {
        val documentDirectory = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true,
        ).firstOrNull() as? String ?: ""

        AppContent(
            systemInternalFilesDir = documentDirectory,
            onExit = { /* iOS apps don't exit from the UI */ },
        )
    }
    IosPlatformHolder.configure(controller)
    return controller
}