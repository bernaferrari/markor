package com.bernaferrari.remarkor.platform

import platform.UIKit.UIViewController

/**
 * Holds the root view controller for native iOS UI (share sheet, image picker).
 * Configured once when [MainViewController] is created.
 */
object IosPlatformHolder {
    var rootViewController: UIViewController? = null
        private set

    fun configure(viewController: UIViewController) {
        rootViewController = viewController
    }
}