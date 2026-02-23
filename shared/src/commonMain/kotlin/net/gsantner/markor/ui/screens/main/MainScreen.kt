package net.gsantner.markor.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import net.gsantner.markor.shared.generated.resources.*
import androidx.compose.ui.unit.dp
import net.gsantner.markor.ui.screens.editor.EditorScreen
import net.gsantner.markor.ui.screens.filelist.FileBrowserContent
import net.gsantner.markor.ui.screens.settings.SettingsScreen
import net.gsantner.markor.ui.viewmodel.FileBrowserViewModel
import net.gsantner.markor.ui.viewmodel.MainViewModel
import net.gsantner.markor.ui.components.EmptyState
import net.gsantner.markor.ui.components.rememberAdaptiveLayoutInfo
import net.gsantner.markor.ui.components.BackHandler
import org.koin.compose.viewmodel.koinViewModel
import net.gsantner.markor.ui.components.CreateFileDialog
import okio.Path
import okio.Path.Companion.toPath
import net.gsantner.markor.ui.viewmodel.FileFilterMode

enum class LeftPanelContent {
    FILE_BROWSER,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
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
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    
    // Adaptive layout info
    val adaptiveInfo = rememberAdaptiveLayoutInfo()
    
    // For dual pane: track selected file for detail view
    var selectedFileForDetail by remember { mutableStateOf<String?>(null) }
    
    // Left panel content for tablets
    var leftPanelContent by remember { mutableStateOf(LeftPanelContent.FILE_BROWSER) }

    BackHandler(enabled = isSelectionMode) {
        fileBrowserViewModel.clearSelection()
    }

    if (showDeleteDialog) {
        net.gsantner.markor.ui.components.DeleteDialog(
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

    if (adaptiveInfo.showDualPane) {
        // Tablet landscape: List-Detail layout with persistent left panel
        ListDetailLayout(
            adaptiveInfo = adaptiveInfo,
            isSelectionMode = isSelectionMode,
            selectedFiles = selectedFiles,
            fileBrowserViewModel = fileBrowserViewModel,
            notebookDirectory = notebookDirectory,
            isGridView = isGridView,
            selectedFileForDetail = selectedFileForDetail,
            leftPanelContent = leftPanelContent,
            onFileSelected = { path -> 
                selectedFileForDetail = path 
                onNavigateToEditor(path, false)
            },
            onClearSelection = { fileBrowserViewModel.clearSelection() },
            onSelectAll = { fileBrowserViewModel.selectAll() },
            onDeleteSelected = { showDeleteDialog = true },
            onToggleGridView = { isGridView = !isGridView },
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
            onSelectAll = { fileBrowserViewModel.selectAll() },
            onDeleteSelected = { showDeleteDialog = true },
            onToggleGridView = { isGridView = !isGridView },
            onOpenSettings = onNavigateToSettings,
            onFilterModeChange = { fileBrowserViewModel.setFilterMode(it) },
            onSortOrderChange = { fileBrowserViewModel.setSortOrder(it) },
            onNavigateToEditor = onNavigateToEditor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                .sortedWith(compareBy<Pair<Int, net.gsantner.markor.domain.repository.FileInfo>> { it.first }
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
                    onClearSelection = onClearSelection,
                    onSelectAll = onSelectAll,
                    onDelete = onDeleteSelected
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListDetailLayout(
    adaptiveInfo: net.gsantner.markor.ui.components.AdaptiveLayoutInfo,
    isSelectionMode: Boolean,
    selectedFiles: Set<Path>,
    fileBrowserViewModel: FileBrowserViewModel,
    notebookDirectory: String,
    isGridView: Boolean,
    selectedFileForDetail: String?,
    leftPanelContent: LeftPanelContent,
    onFileSelected: (String) -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleGridView: () -> Unit,
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
                            animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
                            initialOffsetX = { width -> width }
                        ) + fadeIn(animationSpec = tween(durationMillis = 180))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing),
                                targetOffsetX = { width -> -width / 6 }
                            ) + fadeOut(animationSpec = tween(durationMillis = 140)))
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
                            initialOffsetX = { width -> -width / 6 }
                        ) + fadeIn(animationSpec = tween(durationMillis = 180))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing),
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
                                onClearSelection = onClearSelection,
                                onSelectAll = onSelectAll,
                                onDelete = onDeleteSelected
                            )
                        } else {
                            TopAppBar(
                                title = { Text("Notebook", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                                actions = {
                                    IconButton(onClick = onToggleGridView) {
                                        Icon(
                                            if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                            "Toggle View"
                                        )
                                    }
                                    IconButton(onClick = onNavigateToSettings) {
                                        Icon(Icons.Default.Settings, "Settings")
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
                            "Select a file",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Choose a file from the list to view or edit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { Text("Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "$selectedCount Selected",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
                            text = "Search notes",
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
                        contentDescription = if (isGridView) "Switch to list view" else "Switch to grid view"
                    )
                }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.recent_first)) },
                            leadingIcon = {
                                if (currentSortOrder == "date") Icon(Icons.Default.Check, contentDescription = null)
                            },
                            onClick = {
                                onSortOrderChange("date")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.oldest_first)) },
                            leadingIcon = {
                                if (currentSortOrder == "oldest") Icon(Icons.Default.Check, contentDescription = null)
                            },
                            onClick = {
                                onSortOrderChange("oldest")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.name)) },
                            leadingIcon = {
                                if (currentSortOrder == "name") Icon(Icons.Default.Check, contentDescription = null)
                            },
                            onClick = {
                                onSortOrderChange("name")
                                showSortMenu = false
                            }
                        )
                    }
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.settings))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentFilterMode == FileFilterMode.ALL,
                    onClick = { onFilterModeChange(FileFilterMode.ALL) },
                    label = { Text(stringResource(Res.string.all)) }
                )
                FilterChip(
                    selected = currentFilterMode == FileFilterMode.FAVORITES,
                    onClick = { onFilterModeChange(FileFilterMode.FAVORITES) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (currentFilterMode == FileFilterMode.FAVORITES) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarOutline
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(stringResource(Res.string.favorites)) }
                )
                FilterChip(
                    selected = currentFilterMode == FileFilterMode.ARCHIVE,
                    onClick = { onFilterModeChange(FileFilterMode.ARCHIVE) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (currentFilterMode == FileFilterMode.ARCHIVE) {
                                Icons.Filled.Archive
                            } else {
                                Icons.Outlined.Archive
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(stringResource(Res.string.archive)) }
                )
                FilterChip(
                    selected = currentFilterMode == FileFilterMode.TRASH,
                    onClick = { onFilterModeChange(FileFilterMode.TRASH) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (currentFilterMode == FileFilterMode.TRASH) {
                                Icons.Filled.Delete
                            } else {
                                Icons.Outlined.Delete
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(stringResource(Res.string.trash)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    query: String,
    results: List<net.gsantner.markor.domain.repository.FileInfo>,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
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
                            Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.clear_query))
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
                    title = if (query.isBlank()) stringResource(Res.string.no_notes_yet) else stringResource(Res.string.no_matches),
                    subtitle = if (query.isBlank()) stringResource(Res.string.create_note_to_start_searching) else stringResource(Res.string.try_different_search_term),
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
