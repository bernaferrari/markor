package com.bernaferrari.remarkor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Settings
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
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    ),
    BottomNavItem(
        screen = Screen.Todo,
        title = Res.string.todo,
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    ),
    BottomNavItem(
        screen = Screen.QuickNote,
        title = Res.string.quicknote,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    ),
    BottomNavItem(
        screen = Screen.Settings,
        title = Res.string.settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
