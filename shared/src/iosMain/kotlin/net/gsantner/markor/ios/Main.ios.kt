package net.gsantner.markor.ios

import androidx.compose.ui.window.ComposeUIViewController
import net.gsantner.markor.AppContent
import net.gsantner.markor.di.initKoin
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Main entry point for iOS application.
 * Called from Swift AppDelegate to initialize and display the Compose UI.
 */
fun MainViewController() = ComposeUIViewController { 
    // Initialize Koin DI
    initKoin()
    
    // Get the iOS documents directory
    val documentDirectory = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() as? String ?: ""
    
    AppContent(
        systemInternalFilesDir = documentDirectory,
        onExit = { 
            // On iOS, we typically don't "exit" the app
        }
    )
}
