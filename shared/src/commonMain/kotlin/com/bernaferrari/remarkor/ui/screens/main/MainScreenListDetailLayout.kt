package com.bernaferrari.remarkor.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.ui.components.BackHandler
import com.bernaferrari.remarkor.ui.components.CreateFileDialog
import com.bernaferrari.remarkor.ui.components.EmptyState
import com.bernaferrari.remarkor.ui.components.rememberAdaptiveLayoutInfo
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.screens.SettingsScreen
import com.bernaferrari.remarkor.ui.screens.filebrowser.FileBrowserContent
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import com.bernaferrari.remarkor.ui.viewmodel.MainViewModel
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.all
import markor.shared.generated.resources.archive
import markor.shared.generated.resources.back
import markor.shared.generated.resources.choose_file_to_view_edit
import markor.shared.generated.resources.clear_query
import markor.shared.generated.resources.clear_selection
import markor.shared.generated.resources.create_note_to_start_searching
import markor.shared.generated.resources.delete
import markor.shared.generated.resources.favorites
import markor.shared.generated.resources.name
import markor.shared.generated.resources.no_matches
import markor.shared.generated.resources.no_notes_yet
import markor.shared.generated.resources.note_color
import markor.shared.generated.resources.notebook
import markor.shared.generated.resources.oldest_first
import markor.shared.generated.resources.recent_first
import markor.shared.generated.resources.search_notes
import markor.shared.generated.resources.search_notes_label
import markor.shared.generated.resources.select_all
import markor.shared.generated.resources.select_file
import markor.shared.generated.resources.selected
import markor.shared.generated.resources.settings
import markor.shared.generated.resources.sort
import markor.shared.generated.resources.switch_to_grid_view
import markor.shared.generated.resources.switch_to_list_view
import markor.shared.generated.resources.trash
import markor.shared.generated.resources.try_different_search_term
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MainScreenListDetailLayout(
    adaptiveInfo: com.bernaferrari.remarkor.ui.components.AdaptiveLayoutInfo,
    isSelectionMode: Boolean,
    selectedFiles: Set<Path>,
    fileBrowserViewModel: FileBrowserViewModel,
    notebookDirectory: String,
    isGridView: Boolean,
    leftPanelContent: LeftPanelContent,
    currentFilterMode: FileFilterMode,
    onFileSelected: (String) -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onSetColorForSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleGridView: () -> Unit,
    onFilterModeChange: (FileFilterMode) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFileBrowser: () -> Unit,
    onNavigateToEditor: (String, Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane - File List or Settings
        Column(
            modifier = Modifier
                .weight(adaptiveInfo.listPaneWeight)
                .fillMaxHeight()
        ) {
            AnimatedContent(
                targetState = leftPanelContent,
                modifier = Modifier.fillMaxSize(),
                label = "leftPanelContentTransition",
                transitionSpec = {
                    if (targetState == LeftPanelContent.SETTINGS) {
                        (slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = LinearOutSlowInEasing
                            ),
                            initialOffsetX = { width -> width }
                        ) + fadeIn(animationSpec = tween(durationMillis = 180))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 180,
                                        easing = FastOutLinearInEasing
                                    ),
                                    targetOffsetX = { width -> -width / 6 }
                                ) + fadeOut(animationSpec = tween(durationMillis = 140)))
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = LinearOutSlowInEasing
                            ),
                            initialOffsetX = { width -> -width / 6 }
                        ) + fadeIn(animationSpec = tween(durationMillis = 180))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 180,
                                        easing = FastOutLinearInEasing
                                    ),
                                    targetOffsetX = { width -> width }
                                ) + fadeOut(animationSpec = tween(durationMillis = 140)))
                    }
                }
            ) { panelContent ->
                // Scaffold owns top bar + standard window insets (status bars). AnimatedContent
                // uses a Box by default — never put TopAppBar and content as siblings without Scaffold.
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.statusBars,
                    topBar = {
                        when (panelContent) {
                            LeftPanelContent.FILE_BROWSER -> {
                                if (isSelectionMode) {
                                    SelectionTopBar(
                                        selectedCount = selectedFiles.size,
                                        currentFilterMode = currentFilterMode,
                                        onFilterModeChange = onFilterModeChange,
                                        onClearSelection = onClearSelection,
                                        onSelectAll = onSelectAll,
                                        onSetColor = onSetColorForSelected,
                                        onDelete = onDeleteSelected,
                                        showFilterChips = true
                                    )
                                } else {
                                    TopAppBar(
                                        title = {
                                            Text(
                                                stringResource(Res.string.notebook),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        },
                                        actions = {
                                            IconButton(onClick = onToggleGridView) {
                                                Icon(
                                                    if (isGridView) {
                                                        Icons.AutoMirrored.Filled.List
                                                    } else {
                                                        Icons.Default.GridView
                                                    },
                                                    contentDescription = null
                                                )
                                            }
                                            IconButton(onClick = onNavigateToSettings) {
                                                Icon(
                                                    Icons.Default.Settings,
                                                    contentDescription = stringResource(Res.string.settings)
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    )
                                }
                            }

                            LeftPanelContent.SETTINGS -> {
                                SettingsTopBar(
                                    onNavigateBack = onNavigateToFileBrowser
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    when (panelContent) {
                        LeftPanelContent.FILE_BROWSER -> {
                            FileBrowserContent(
                                initialPath = notebookDirectory.ifEmpty { null },
                                onNavigateToEditor = onNavigateToEditor,
                                onNavigateBack = { },
                                viewModel = fileBrowserViewModel,
                                isGridView = isGridView,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            )
                        }

                        LeftPanelContent.SETTINGS -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                SettingsScreen(
                                    onNavigateBack = onNavigateToFileBrowser,
                                    showTopBar = false,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Divider
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Right pane - empty state (notes open as Keep-style dialogs on large screens)
        Box(
            modifier = Modifier
                .weight(adaptiveInfo.detailPaneWeight)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(Res.string.select_file),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(Res.string.choose_file_to_view_edit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
