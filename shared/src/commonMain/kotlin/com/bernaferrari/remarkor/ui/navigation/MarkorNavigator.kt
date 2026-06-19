package com.bernaferrari.remarkor.ui.navigation

class MarkorNavigator(
    private val backstack: MutableList<Screen>,
    private val onExitWhenEmpty: () -> Unit,
) {
    fun navigate(screen: Screen) {
        backstack.add(screen)
    }

    fun pop() {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.size - 1)
        } else {
            onExitWhenEmpty()
        }
    }

    fun popToRoot() {
        while (backstack.size > 1) {
            backstack.removeAt(backstack.size - 1)
        }
    }
}