package com.bernaferrari.remarkor.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import com.bernaferrari.remarkor.ui.components.NoteEditorDialog
import com.bernaferrari.remarkor.ui.components.rememberAdaptiveLayoutInfo
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.screens.main.MainScreenPhoneLayout
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
fun MainScreen(
    currentTab: Int = 0,
    onNavigateToEditor: (String, Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = koinViewModel(),
    fileBrowserViewModel: FileBrowserViewModel = koinViewModel()
) {
    val notebookDirectory by viewModel.notebookDirectory.collectAsState()

    val isSelectionMode by fileBrowserViewModel.isSelectionMode.collectAsState()
    val selectedFiles by fileBrowserViewModel.selectedFiles.collectAsState()
    val files by fileBrowserViewModel.files.collectAsState()
    val trashFiles by fileBrowserViewModel.trashFiles.collectAsState()
    val noteMetadataByPath by fileBrowserViewModel.noteMetadataByPath.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showSelectionColorSheet by remember { mutableStateOf(false) }

    // Adaptive layout info
    val adaptiveInfo = rememberAdaptiveLayoutInfo()

    // Keep-style note overlay on large screens — simple fade (sharedBounds was too expensive).
    var noteDialogPath by remember { mutableStateOf<String?>(null) }
    var noteDialogAutoKeyboard by remember { mutableStateOf(false) }
    var retainedNoteDialogPath by remember { mutableStateOf<String?>(null) }
    var retainedNoteDialogAutoKeyboard by remember { mutableStateOf(false) }
    if (noteDialogPath != null) {
        retainedNoteDialogPath = noteDialogPath
        retainedNoteDialogAutoKeyboard = noteDialogAutoKeyboard
    }
    val isNoteDialogVisible = noteDialogPath != null

    // Large screens: open notes in a Keep-style dialog over a full-width notebook grid.
    // Phone: push full-screen editor. No list-detail split — the empty "select a file" pane is gone.
    val openNote: (String, Boolean) -> Unit = { path, autoOpenKeyboard ->
        if (adaptiveInfo.isLargeScreen) {
            noteDialogPath = path
            noteDialogAutoKeyboard = autoOpenKeyboard
        } else {
            onNavigateToEditor(path, autoOpenKeyboard)
        }
    }

    BackHandler(enabled = isNoteDialogVisible) {
        noteDialogPath = null
        noteDialogAutoKeyboard = false
    }

    val fileByPath = remember(files, trashFiles) {
        (files + trashFiles).associateBy { it.path }
    }
    val nonDirectorySelectedColors = remember(selectedFiles, fileByPath, noteMetadataByPath) {
        buildList<Int?> {
            selectedFiles.forEach { path ->
                val fileInfo = fileByPath[path]
                if (fileInfo?.isDirectory != true) {
                    // Keep nullable colors so "all default color" can be detected as a shared state.
                    add(noteMetadataByPath[path.toString()]?.color)
                }
            }
        }
    }
    val (selectionSheetCurrentColor, selectionSheetShowCurrentSelection) =
        remember(nonDirectorySelectedColors) {
            if (nonDirectorySelectedColors.isEmpty()) {
                null to false
            } else {
                val firstColor = nonDirectorySelectedColors.first()
                val allSame = nonDirectorySelectedColors.all { it == firstColor }
                if (allSame) {
                    firstColor to true
                } else {
                    null to false
                }
            }
        }

    BackHandler(enabled = isSelectionMode) {
        fileBrowserViewModel.clearSelection()
    }

    if (showDeleteDialog) {
        com.bernaferrari.remarkor.ui.components.DeleteDialog(
            count = selectedFiles.size,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                fileBrowserViewModel.deleteSelectedFiles()
                showDeleteDialog = false
            }
        )
    }

    if (showCreateFileDialog) {
        CreateFileDialog(
            onDismiss = { showCreateFileDialog = false },
            onConfirm = { fileName ->
                val effectivePath = notebookDirectory.ifEmpty { null }
                if (effectivePath != null) {
                    fileBrowserViewModel.createNewFile(effectivePath.toPath(), fileName)
                }
                showCreateFileDialog = false
            },
            suggestedName = "new_note.md"
        )
    }

    if (showSelectionColorSheet) {
        com.bernaferrari.remarkor.ui.components.ColorSelectionSheet(
            currentColor = selectionSheetCurrentColor,
            showCurrentSelection = selectionSheetShowCurrentSelection,
            onColorSelected = { color ->
                fileBrowserViewModel.setColorForSelectedFiles(color)
                showSelectionColorSheet = false
            },
            onDismiss = { showSelectionColorSheet = false }
        )
    }

    // Full-width notebook on all sizes (Keep-style). Large screens open the editor as an overlay.
    Box(modifier = Modifier.fillMaxSize()) {
        val filterMode by fileBrowserViewModel.filterMode.collectAsState()
        val sortOrder by fileBrowserViewModel.sortOrder.collectAsState()

        MainScreenPhoneLayout(
            isSelectionMode = isSelectionMode,
            selectedFiles = selectedFiles,
            fileBrowserViewModel = fileBrowserViewModel,
            notebookDirectory = notebookDirectory,
            isGridView = isGridView,
            currentFilterMode = filterMode,
            currentSortOrder = sortOrder,
            onClearSelection = { fileBrowserViewModel.clearSelection() },
            onSelectAll = { fileBrowserViewModel.toggleSelectAll() },
            onSetColorForSelected = { showSelectionColorSheet = true },
            onDeleteSelected = { showDeleteDialog = true },
            onToggleGridView = { isGridView = !isGridView },
            onOpenSettings = onNavigateToSettings,
            onFilterModeChange = { fileBrowserViewModel.setFilterMode(it) },
            onSortOrderChange = { fileBrowserViewModel.setSortOrder(it) },
            onNavigateToEditor = openNote,
            gridMinCellWidthDp = when {
                adaptiveInfo.isExpandedScreen -> 220
                adaptiveInfo.isLargeScreen -> 180
                else -> 150
            },
            contentMaxWidthDp = if (adaptiveInfo.isExpandedScreen) 1200 else null,
        )

        AnimatedVisibility(
            visible = isNoteDialogVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 160, easing = LinearOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120, easing = FastOutLinearInEasing)),
            label = "keep_note_overlay",
        ) {
            retainedNoteDialogPath?.let { path ->
                NoteEditorDialog(
                    filePath = path,
                    openKeyboardOnStart = retainedNoteDialogAutoKeyboard,
                    onDismiss = {
                        noteDialogPath = null
                        noteDialogAutoKeyboard = false
                    },
                )
            }
        }
    }
}
