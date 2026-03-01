package com.bernaferrari.remarkor.ui.screens

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

enum class LeftPanelContent {
    FILE_BROWSER,
    SETTINGS
}

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

    // For dual pane: track selected file for detail view
    var selectedFileForDetail by remember { mutableStateOf<String?>(null) }

    // Left panel content for tablets
    var leftPanelContent by remember { mutableStateOf(LeftPanelContent.FILE_BROWSER) }

    val fileByPath = remember(files, trashFiles) {
        (files + trashFiles).associateBy { it.path }
    }
    val nonDirectorySelectedColors = remember(selectedFiles, fileByPath, noteMetadataByPath) {
        buildList<Int?> {
            selectedFiles.forEach { path ->
                val fileInfo = fileByPath[path]
                if (fileInfo?.isDirectory != true) {
                    // Keep nullable colors so "all default color" can be detected as a shared state.
                    add(noteMetadataByPath[path.toString()]?.note?.color)
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

    if (adaptiveInfo.showDualPane) {
        // Tablet landscape: List-Detail layout with persistent left panel
        val filterMode by fileBrowserViewModel.filterMode.collectAsState()
        ListDetailLayout(
            adaptiveInfo = adaptiveInfo,
            isSelectionMode = isSelectionMode,
            selectedFiles = selectedFiles,
            fileBrowserViewModel = fileBrowserViewModel,
            notebookDirectory = notebookDirectory,
            isGridView = isGridView,
            selectedFileForDetail = selectedFileForDetail,
            leftPanelContent = leftPanelContent,
            currentFilterMode = filterMode,
            onFileSelected = { path ->
                selectedFileForDetail = path
                onNavigateToEditor(path, false)
            },
            onClearSelection = { fileBrowserViewModel.clearSelection() },
            onSelectAll = { fileBrowserViewModel.toggleSelectAll() },
            onSetColorForSelected = { showSelectionColorSheet = true },
            onDeleteSelected = { showDeleteDialog = true },
            onToggleGridView = { isGridView = !isGridView },
            onFilterModeChange = { fileBrowserViewModel.setFilterMode(it) },
            onNavigateToSettings = {
                leftPanelContent = LeftPanelContent.SETTINGS
            },
            onNavigateToFileBrowser = {
                leftPanelContent = LeftPanelContent.FILE_BROWSER
            },
            onNavigateToEditor = onNavigateToEditor
        )
    } else {
        // Phone: Standard Scaffold (no drawer)
        val filterMode by fileBrowserViewModel.filterMode.collectAsState()
        val sortOrder by fileBrowserViewModel.sortOrder.collectAsState()

        PhoneLayout(
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
            onNavigateToEditor = onNavigateToEditor
        )
    }
}

@Composable
private fun PhoneLayout(
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

@Composable
private fun ListDetailLayout(
    adaptiveInfo: com.bernaferrari.remarkor.ui.components.AdaptiveLayoutInfo,
    isSelectionMode: Boolean,
    selectedFiles: Set<Path>,
    fileBrowserViewModel: FileBrowserViewModel,
    notebookDirectory: String,
    isGridView: Boolean,
    selectedFileForDetail: String?,
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
    onNavigateToEditor: (String, Boolean) -> Unit
) {
    // Track detail content path
    var detailPath by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane - File List or Settings
        Column(
            modifier = Modifier
                .weight(adaptiveInfo.listPaneWeight)
                .fillMaxHeight()
        ) {
            AnimatedContent(
                targetState = leftPanelContent,
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
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                actions = {
                                    IconButton(onClick = onToggleGridView) {
                                        Icon(
                                            if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
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

                        FileBrowserContent(
                            initialPath = notebookDirectory.ifEmpty { null },
                            onNavigateToEditor = { path, autoOpenKeyboard ->
                                detailPath = path
                                onNavigateToEditor(path, autoOpenKeyboard)
                            },
                            onNavigateBack = { },
                            viewModel = fileBrowserViewModel,
                            isGridView = isGridView,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LeftPanelContent.SETTINGS -> {
                        SettingsTopBar(
                            onNavigateBack = onNavigateToFileBrowser
                        )
                        SettingsScreen(
                            onNavigateBack = onNavigateToFileBrowser,
                            showTopBar = false
                        )
                    }
                }
            }
        }

        // Divider
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Right pane - Detail/Editor
        Column(
            modifier = Modifier
                .weight(adaptiveInfo.detailPaneWeight)
                .fillMaxHeight()
        ) {
            if (detailPath != null) {
                EditorScreen(
                    filePath = detailPath!!,
                    onNavigateBack = { detailPath = null }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
    }
}

@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                stringResource(Res.string.settings),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    currentFilterMode: FileFilterMode = FileFilterMode.ALL,
    onFilterModeChange: (FileFilterMode) -> Unit = {},
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onSetColor: () -> Unit,
    onDelete: () -> Unit,
    showFilterChips: Boolean = false
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = "$selectedCount ${stringResource(Res.string.selected)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.clear_selection)
                    )
                }
            },
            actions = {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        Icons.Default.SelectAll,
                        contentDescription = stringResource(Res.string.select_all)
                    )
                }
                if (currentFilterMode != FileFilterMode.TRASH) {
                    IconButton(onClick = onSetColor) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = stringResource(Res.string.note_color)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        if (showFilterChips) {
            FilterTabRow(
                currentFilterMode = currentFilterMode,
                onFilterModeChange = onFilterModeChange
            )
        }
    }
}

@Composable
private fun FilterTabRow(
    currentFilterMode: FileFilterMode,
    onFilterModeChange: (FileFilterMode) -> Unit
) {
    val hapticHelper = rememberHapticHelper()

    val filters = listOf(
        Triple(
            FileFilterMode.ALL,
            Res.string.all,
            Icons.AutoMirrored.Outlined.List to Icons.AutoMirrored.Filled.List
        ),
        Triple(
            FileFilterMode.FAVORITES,
            Res.string.favorites,
            Icons.Outlined.StarOutline to Icons.Default.Star
        ),
        Triple(
            FileFilterMode.ARCHIVE,
            Res.string.archive,
            Icons.Outlined.Archive to Icons.Default.Archive
        ),
        Triple(
            FileFilterMode.TRASH,
            Res.string.trash,
            Icons.Outlined.Delete to Icons.Default.Delete
        )
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filters.forEachIndexed { index, (mode, labelRes, icons) ->
                val isSelected = mode == currentFilterMode
                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        if (checked && mode != currentFilterMode) {
                            hapticHelper.performLightClick()
                            onFilterModeChange(mode)
                        }
                    },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = if (isSelected) icons.second else icons.first,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardTopBar(
    isGridView: Boolean,
    currentFilterMode: FileFilterMode,
    currentSortOrder: String,
    onFilterModeChange: (FileFilterMode) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onToggleGridView: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenSearch),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.search_notes_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onToggleGridView) {
                    Icon(
                        if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                        contentDescription = if (isGridView) stringResource(Res.string.switch_to_list_view) else stringResource(
                            Res.string.switch_to_grid_view
                        )
                    )
                }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(Res.string.sort)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.recent_first)) },
                            leadingIcon = {
                                if (currentSortOrder == "date") Icon(
                                    Icons.Default.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onSortOrderChange("date")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.oldest_first)) },
                            leadingIcon = {
                                if (currentSortOrder == "oldest") Icon(
                                    Icons.Default.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onSortOrderChange("oldest")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.name)) },
                            leadingIcon = {
                                if (currentSortOrder == "name") Icon(
                                    Icons.Default.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onSortOrderChange("name")
                                showSortMenu = false
                            }
                        )
                    }
                }
                IconButton(onClick = onOpenSettings) {
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

        FilterTabRow(
            currentFilterMode = currentFilterMode,
            onFilterModeChange = onFilterModeChange
        )
    }
}

@Composable
private fun SearchScreen(
    query: String,
    results: List<com.bernaferrari.remarkor.domain.repository.FileInfo>,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onOpenNote: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .padding(vertical = 4.dp),
                        placeholder = { Text(stringResource(Res.string.search_notes)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                },
                actions = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(Res.string.clear_query)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = if (query.isBlank()) stringResource(Res.string.no_notes_yet) else stringResource(
                        Res.string.no_matches
                    ),
                    subtitle = if (query.isBlank()) stringResource(Res.string.create_note_to_start_searching) else stringResource(
                        Res.string.try_different_search_term
                    ),
                    icon = Icons.Default.SearchOff
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = results,
                    key = { it.path.toString() }
                ) { file ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = file.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            val preview = file.preview?.trim().orEmpty()
                            if (preview.isNotEmpty()) {
                                Text(
                                    text = preview,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNote(file.path.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(title: String, subtitle: String) {
    EmptyState(
        title = title,
        subtitle = subtitle,
        icon = Icons.Default.Info
    )
}
