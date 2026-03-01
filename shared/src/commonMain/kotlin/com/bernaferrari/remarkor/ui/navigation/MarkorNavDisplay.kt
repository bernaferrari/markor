package com.bernaferrari.remarkor.ui.navigation

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.LocalNavAnimatedContentScope
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
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    fun isSettings(key: Any): Boolean = key.toString() == Screen.Settings.toString()

    NavDisplay(
        backStack = backstack,
        onBack = onPopBackStack,
        sharedTransitionScope = sharedTransitionScope,
        transitionSpec = {
            if (isSettings(targetState.key) && !isSettings(initialState.key)) {
                (slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 240,
                        easing = LinearOutSlowInEasing
                    ),
                    initialOffsetX = { width -> width }
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = LinearOutSlowInEasing
                    )
                )) togetherWith (
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 120,
                            easing = FastOutLinearInEasing
                        )
                    ) + ExitTransition.KeepUntilTransitionsFinished
                )
            } else {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 160,
                        easing = LinearOutSlowInEasing
                    )
                ) togetherWith (
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 120,
                            easing = FastOutLinearInEasing
                        )
                    ) + ExitTransition.KeepUntilTransitionsFinished
                )
            }
        },
        popTransitionSpec = {
            if (isSettings(initialState.key) && !isSettings(targetState.key)) {
                (slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = LinearOutSlowInEasing
                    ),
                    initialOffsetX = { width -> -width / 10 }
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 160,
                        easing = LinearOutSlowInEasing
                    )
                )) togetherWith (
                    slideOutHorizontally(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing
                        ),
                        targetOffsetX = { width -> width }
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 140,
                            easing = FastOutLinearInEasing
                        )
                    ) + ExitTransition.KeepUntilTransitionsFinished
                )
            } else {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 160,
                        easing = LinearOutSlowInEasing
                    )
                ) togetherWith (
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 120,
                            easing = FastOutLinearInEasing
                        )
                    ) + ExitTransition.KeepUntilTransitionsFinished
                )
            }
        },
        predictivePopTransitionSpec = { _ ->
            if (isSettings(initialState.key) && !isSettings(targetState.key)) {
                (slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = LinearOutSlowInEasing
                    ),
                    initialOffsetX = { width -> -width / 10 }
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 160,
                        easing = LinearOutSlowInEasing
                    )
                )) togetherWith (
                    slideOutHorizontally(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing
                        ),
                        targetOffsetX = { width -> width }
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 140,
                            easing = FastOutLinearInEasing
                        )
                    ) + ExitTransition.KeepUntilTransitionsFinished
                )
            } else {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 160,
                        easing = LinearOutSlowInEasing
                    )
                ) togetherWith (
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 120,
                            easing = FastOutLinearInEasing
                        )
                    ) + ExitTransition.KeepUntilTransitionsFinished
                )
            }
        },
        entryProvider = { screen ->
            NavEntry(
                key = screen,
                contentKey = screen.toString()
            ) {
                CompositionLocalProvider(
                    LocalAnimatedVisibilityScope provides LocalNavAnimatedContentScope.current
                ) {
                    when (screen) {
                        is Screen.Notebook -> MainScreen(
                            currentTab = 0,
                            onNavigateToEditor = { path, autoOpenKeyboard -> onNavigate(Screen.Editor(path, autoOpenKeyboard)) },
                            onNavigateToSettings = { onNavigate(Screen.Settings) }
                        )
                        is Screen.Todo -> MainScreen(
                            currentTab = 1,
                            onNavigateToEditor = { path, autoOpenKeyboard -> onNavigate(Screen.Editor(path, autoOpenKeyboard)) },
                            onNavigateToSettings = { onNavigate(Screen.Settings) }
                        )
                        is Screen.QuickNote -> MainScreen(
                            currentTab = 2,
                            onNavigateToEditor = { path, autoOpenKeyboard -> onNavigate(Screen.Editor(path, autoOpenKeyboard)) },
                            onNavigateToSettings = { onNavigate(Screen.Settings) }
                        )
                        is Screen.More -> MainScreen(
                            currentTab = 3,
                            onNavigateToEditor = { path, autoOpenKeyboard -> onNavigate(Screen.Editor(path, autoOpenKeyboard)) },
                            onNavigateToSettings = { onNavigate(Screen.Settings) }
                        )
                        is Screen.Editor -> EditorScreen(
                            filePath = screen.filePath,
                            openKeyboardOnStart = screen.autoOpenKeyboard,
                            onNavigateBack = onPopBackStack
                        )
                        is Screen.Settings -> SettingsScreen(
                            onNavigateBack = onPopBackStack
                        )
                        is Screen.FileBrowser -> FileBrowserScreen(
                            initialPath = screen.path,
                            onNavigateToEditor = { path, autoOpenKeyboard -> onNavigate(Screen.Editor(path, autoOpenKeyboard)) },
                            onNavigateBack = onPopBackStack
                        )
                        is Screen.Search -> {
                            // Placeholder for SearchScreen
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}
