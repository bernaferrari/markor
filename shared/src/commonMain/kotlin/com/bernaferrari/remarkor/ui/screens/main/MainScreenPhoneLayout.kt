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
internal fun MainScreenPhoneLayout(
    isSelectionMode: Boolean,
    selectedFiles: Set<Path>,
    fileBrowserViewModel: FileBrowserViewModel,
    notebookDirectory: String,
    isGridView: Boolean,
    currentFilterMode: FileFilterMode,
    currentSortOrder: String,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onSetColorForSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleGridView: () -> Unit,
    onOpenSettings: () -> Unit,
    onFilterModeChange: (FileFilterMode) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onNavigateToEditor: (String, Boolean) -> Unit
) {
    val files by fileBrowserViewModel.files.collectAsState()
    val trashFiles by fileBrowserViewModel.trashFiles.collectAsState()
    var isSearchScreenOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val searchableFiles = remember(files, trashFiles) {
        (files + trashFiles).filter { !it.isDirectory }
    }
    val searchResults = remember(searchableFiles, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) {
            searchableFiles
                .sortedByDescending { it.lastModified }
                .take(40)
        } else {
            searchableFiles
                .mapNotNull { file ->
                    val name = file.name.lowercase()
                    val preview = file.preview.orEmpty().lowercase()
                    val score = when {
                        name.startsWith(query) -> 0
                        name.contains(query) -> 1
                        preview.contains(query) -> 2
                        else -> return@mapNotNull null
                    }
                    score to file
                }
                .sortedWith(compareBy<Pair<Int, com.bernaferrari.remarkor.domain.repository.FileInfo>> { it.first }
                    .thenByDescending { it.second.lastModified })
                .map { it.second }
                .take(80)
        }
    }

    BackHandler(enabled = isSearchScreenOpen) {
        searchQuery = ""
        isSearchScreenOpen = false
    }

    if (isSearchScreenOpen && !isSelectionMode) {
        SearchScreen(
            query = searchQuery,
            results = searchResults,
            onQueryChange = { searchQuery = it },
            onClose = {
                searchQuery = ""
                isSearchScreenOpen = false
            },
            onOpenNote = { path ->
                searchQuery = ""
                isSearchScreenOpen = false
                onNavigateToEditor(path, false)
            }
        )
        return
    }

    Scaffold(
        // Draw content behind the bottom system bar for a true edge-to-edge main surface.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
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
                StandardTopBar(
                    isGridView = isGridView,
                    currentFilterMode = currentFilterMode,
                    currentSortOrder = currentSortOrder,
                    onFilterModeChange = onFilterModeChange,
                    onSortOrderChange = onSortOrderChange,
                    onToggleGridView = onToggleGridView,
                    onOpenSettings = onOpenSettings,
                    onOpenSearch = { isSearchScreenOpen = true }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            FileBrowserContent(
                initialPath = notebookDirectory.ifEmpty { null },
                onNavigateToEditor = onNavigateToEditor,
                onNavigateBack = { },
                viewModel = fileBrowserViewModel,
                isGridView = isGridView
            )
        }
    }
}
