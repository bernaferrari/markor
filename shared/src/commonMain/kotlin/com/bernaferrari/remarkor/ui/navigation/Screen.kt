package com.bernaferrari.remarkor.ui.navigation

import com.bernaferrari.remarkor.ui.icons.MaterialSymbols

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.notebook
import markor.shared.generated.resources.quicknote
import markor.shared.generated.resources.settings
import markor.shared.generated.resources.todo
import org.jetbrains.compose.resources.StringResource

@Serializable
sealed class Screen : NavKey {
    @Serializable
    data object Notebook : Screen()

    @Serializable
    data object Todo : Screen()

    @Serializable
    data object QuickNote : Screen()

    @Serializable
    data object More : Screen()

    @Serializable
    data class Editor(val filePath: String, val autoOpenKeyboard: Boolean = false) : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data class FileBrowser(val path: String? = null) : Screen()

    @Serializable
    data class Search(val path: String? = null, val query: String? = null) : Screen()
}

data class BottomNavItem(
    val screen: Screen,
    val title: StringResource,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Notebook,
        title = Res.string.notebook,
        selectedIcon = MaterialSymbols.Filled.Description,
        unselectedIcon = MaterialSymbols.Outlined.Description
    ),
    BottomNavItem(
        screen = Screen.Todo,
        title = Res.string.todo,
        selectedIcon = MaterialSymbols.Filled.CheckCircle,
        unselectedIcon = MaterialSymbols.Outlined.CheckCircle
    ),
    BottomNavItem(
        screen = Screen.QuickNote,
        title = Res.string.quicknote,
        selectedIcon = MaterialSymbols.Filled.Add,
        unselectedIcon = MaterialSymbols.Outlined.Add
    ),
    BottomNavItem(
        screen = Screen.Settings,
        title = Res.string.settings,
        selectedIcon = MaterialSymbols.Filled.Settings,
        unselectedIcon = MaterialSymbols.Outlined.Settings
    )
)
