package net.gsantner.markor.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
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

enum class LeftPanelContent {
    FILE_BROWSER,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentTab: Int = 0,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = koinViewModel(),
    fileBrowserViewModel: FileBrowserViewModel = koinViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val quickNotePath by viewModel.quickNotePath.collectAsState()
    val todoFilePath by viewModel.todoFilePath.collectAsState()
    val notebookDirectory by viewModel.notebookDirectory.collectAsState()

    val isSelectionMode by fileBrowserViewModel.isSelectionMode.collectAsState()
    val selectedFiles by fileBrowserViewModel.selectedFiles.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
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
                onNavigateToEditor(path)
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
    } else if (showSettings) {
        SettingsScreen(onNavigateBack = { showSettings = false })
    } else {
        // Phone: Standard Scaffold with Drawer
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        
        // Observe filter mode
        val filterMode by fileBrowserViewModel.filterMode.collectAsState()
        val labels by fileBrowserViewModel.labels.collectAsState()
        val currentLabel by fileBrowserViewModel.currentLabel.collectAsState()

        net.gsantner.markor.ui.components.MainNavigationDrawer(
            drawerState = drawerState,
            currentFilterMode = filterMode,
            labels = labels,
            currentLabel = currentLabel,
            onSelectFilterMode = { mode -> 
                fileBrowserViewModel.setFilterMode(mode)
            },
            onSelectLabel = { label ->
                fileBrowserViewModel.setLabelFilter(label)
            },
            onNavigateToSettings = { showSettings = true }
        ) {
            PhoneLayout(
                scrollBehavior = scrollBehavior,
                isSelectionMode = isSelectionMode,
                selectedFiles = selectedFiles,
                fileBrowserViewModel = fileBrowserViewModel,
                notebookDirectory = notebookDirectory,
                isGridView = isGridView,
                quickNotePath = quickNotePath,
                todoFilePath = todoFilePath,
                currentFilterMode = filterMode,
                currentLabel = currentLabel,
                onClearSelection = { fileBrowserViewModel.clearSelection() },
                onSelectAll = { fileBrowserViewModel.selectAll() },
                onDeleteSelected = { showDeleteDialog = true },
                onToggleGridView = { isGridView = !isGridView },
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onShowCreateFileDialog = { showCreateFileDialog = true },
                onNavigateToEditor = onNavigateToEditor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhoneLayout(
    scrollBehavior: TopAppBarScrollBehavior,
    isSelectionMode: Boolean,
    selectedFiles: Set<Path>,
    fileBrowserViewModel: FileBrowserViewModel,
    notebookDirectory: String,
    isGridView: Boolean,
    quickNotePath: String,
    todoFilePath: String,
    currentFilterMode: net.gsantner.markor.ui.viewmodel.FileFilterMode,
    currentLabel: String? = null,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleGridView: () -> Unit,
    onOpenDrawer: () -> Unit,
    onShowCreateFileDialog: () -> Unit,
    onNavigateToEditor: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    scrollBehavior = scrollBehavior,
                    isGridView = isGridView,
                    title = when(currentFilterMode) {
                        net.gsantner.markor.ui.viewmodel.FileFilterMode.TRASH -> "Trash"
                        net.gsantner.markor.ui.viewmodel.FileFilterMode.ARCHIVE -> "Archive"
                        net.gsantner.markor.ui.viewmodel.FileFilterMode.FAVORITES -> "Favorites"
                        net.gsantner.markor.ui.viewmodel.FileFilterMode.LABEL -> currentLabel ?: "Label"
                        else -> "Markor"
                    },
                    onToggleGridView = onToggleGridView,
                    onOpenDrawer = onOpenDrawer
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode && quickNotePath.isNotEmpty() && todoFilePath.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onShowCreateFileDialog,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
    onNavigateToEditor: (String) -> Unit
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
            when (leftPanelContent) {
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
                            ),
                            modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                        )
                    }

                    FileBrowserContent(
                        initialPath = notebookDirectory.ifEmpty { null },
                        onNavigateToEditor = { path ->
                            detailPath = path
                            onNavigateToEditor(path)
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
                        onNavigateBack = onNavigateToFileBrowser
                    )
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
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
    scrollBehavior: TopAppBarScrollBehavior,
    isGridView: Boolean,
    title: String,
    onToggleGridView: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "Open Menu")
            }
        },
        actions = {
            IconButton(onClick = onToggleGridView) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                    contentDescription = if (isGridView) "Switch to List" else "Switch to Grid"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
    )
}

@Composable
private fun EmptyStateMessage(title: String, subtitle: String) {
    EmptyState(
        title = title,
        subtitle = subtitle,
        icon = Icons.Default.Info
    )
}
