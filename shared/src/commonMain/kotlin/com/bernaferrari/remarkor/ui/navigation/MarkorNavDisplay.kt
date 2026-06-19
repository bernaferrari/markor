package com.bernaferrari.remarkor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.bernaferrari.remarkor.ui.components.LocalAnimatedVisibilityScope
import com.bernaferrari.remarkor.ui.components.LocalSharedTransitionScope
import com.bernaferrari.remarkor.ui.screens.EditorScreen
import com.bernaferrari.remarkor.ui.screens.FileBrowserScreen
import com.bernaferrari.remarkor.ui.screens.MainScreen
import com.bernaferrari.remarkor.ui.screens.SettingsScreen

@Composable
fun MarkorNavDisplay(
    backstack: List<Screen>,
    onNavigate: (Screen) -> Unit,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current

    NavDisplay(
        backStack = backstack,
        onBack = onPopBackStack,
        sharedTransitionScope = sharedTransitionScope,
        transitionSpec = { markorTransitionSpec(initialState.key, targetState.key) },
        popTransitionSpec = { markorPopTransitionSpec(initialState.key, targetState.key) },
        predictivePopTransitionSpec = { markorPopTransitionSpec(initialState.key, targetState.key) },
        entryProvider = { screen ->
            NavEntry(key = screen, contentKey = screen.toString()) {
                CompositionLocalProvider(
                    LocalAnimatedVisibilityScope provides LocalNavAnimatedContentScope.current,
                ) {
                    when (screen) {
                        is Screen.Notebook,
                        is Screen.Todo,
                        is Screen.QuickNote,
                        is Screen.More -> MainScreen(
                            currentTab = screen.tabIndex(),
                            onNavigateToEditor = { path, autoOpenKeyboard ->
                                onNavigate(Screen.Editor(path, autoOpenKeyboard))
                            },
                            onNavigateToSettings = { onNavigate(Screen.Settings) },
                        )

                        is Screen.Editor -> EditorScreen(
                            filePath = screen.filePath,
                            openKeyboardOnStart = screen.autoOpenKeyboard,
                            onNavigateBack = onPopBackStack,
                        )

                        is Screen.Settings -> SettingsScreen(onNavigateBack = onPopBackStack)

                        is Screen.FileBrowser -> FileBrowserScreen(
                            initialPath = screen.path,
                            onNavigateToEditor = { path, autoOpenKeyboard ->
                                onNavigate(Screen.Editor(path, autoOpenKeyboard))
                            },
                            onNavigateBack = onPopBackStack,
                        )

                        is Screen.Search -> Unit
                    }
                }
            }
        },
        modifier = modifier,
    )
}

private fun Screen.tabIndex(): Int = when (this) {
    Screen.Notebook -> 0
    Screen.Todo -> 1
    Screen.QuickNote -> 2
    Screen.More -> 3
    else -> 0
}