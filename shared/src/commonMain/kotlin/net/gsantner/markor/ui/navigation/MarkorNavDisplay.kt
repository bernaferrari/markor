package net.gsantner.markor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import net.gsantner.markor.ui.screens.editor.EditorScreen
import net.gsantner.markor.ui.screens.filelist.FileBrowserScreen
import net.gsantner.markor.ui.screens.main.MainScreen
import net.gsantner.markor.ui.screens.settings.SettingsScreen

@Composable
fun MarkorNavDisplay(
    backstack: List<Screen>,
    onNavigate: (Screen) -> Unit,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        entries = backstack.map { screen ->
            NavEntry(screen) {
                when (screen) {
                    is Screen.Notebook -> MainScreen(
                        currentTab = 0,
                        onNavigateToEditor = { onNavigate(Screen.Editor(it)) },
                        onNavigateToSettings = { onNavigate(Screen.Settings) }
                    )
                    is Screen.Todo -> MainScreen(
                        currentTab = 1,
                        onNavigateToEditor = { onNavigate(Screen.Editor(it)) },
                        onNavigateToSettings = { onNavigate(Screen.Settings) }
                    )
                    is Screen.QuickNote -> MainScreen(
                        currentTab = 2,
                        onNavigateToEditor = { onNavigate(Screen.Editor(it)) },
                        onNavigateToSettings = { onNavigate(Screen.Settings) }
                    )
                    is Screen.More -> MainScreen(
                        currentTab = 3,
                        onNavigateToEditor = { onNavigate(Screen.Editor(it)) },
                        onNavigateToSettings = { onNavigate(Screen.Settings) }
                    )
                    is Screen.Editor -> EditorScreen(
                        filePath = screen.filePath,
                        onNavigateBack = onPopBackStack
                    )
                    is Screen.Settings -> SettingsScreen(
                        onNavigateBack = onPopBackStack
                    )
                    is Screen.FileBrowser -> FileBrowserScreen(
                        initialPath = screen.path,
                        onNavigateToEditor = { onNavigate(Screen.Editor(it)) },
                        onNavigateBack = onPopBackStack
                    )
                    is Screen.Search -> {
                        // Placeholder for SearchScreen
                    }
                }
            }
        },
        onBack = onPopBackStack,
        modifier = modifier
    )
}
