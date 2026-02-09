package net.gsantner.markor.ui.screens.filelist

import net.gsantner.markor.ui.components.RenameDialog
import net.gsantner.markor.ui.components.CreateFolderDialog
import net.gsantner.markor.ui.components.DeleteDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import net.gsantner.markor.domain.repository.FileInfo
import net.gsantner.markor.ui.components.*
import net.gsantner.markor.ui.viewmodel.FileBrowserViewModel
import net.gsantner.markor.ui.viewmodel.FileFilterMode
import net.gsantner.markor.ui.viewmodel.FileTypeFilter
import net.gsantner.markor.ui.theme.MarkorTheme
import okio.Path.Companion.toPath
import androidx.compose.ui.input.key.*
import org.koin.compose.viewmodel.koinViewModel

/**
 * Extracts the original filename from a trash filename.
 * Trash format: "yyyyMMdd_HHmmss_originalfilename.ext"
 */
private fun getOriginalPath(trashFileName: String): String {
    val underscoreIndex = trashFileName.indexOf("_")
    return if (underscoreIndex > 0) {
        trashFileName.substring(underscoreIndex + 1)
    } else {
        trashFileName
    }
}

@Composable
fun FileBrowserScreen(
    initialPath: String?,
    onNavigateToEditor: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FileBrowserViewModel = koinViewModel(),
    isGridView: Boolean = true
) {
    FileBrowserContent(
        initialPath = initialPath,
        onNavigateToEditor = onNavigateToEditor,
        onNavigateBack = onNavigateBack,
        viewModel = viewModel,
        isGridView = isGridView
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserContent(
    initialPath: String?,
    onNavigateToEditor: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FileBrowserViewModel = koinViewModel(),
    isGridView: Boolean,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf(initialPath) }
    val files by viewModel.files.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val noteMetadataByPath by viewModel.noteMetadataByPath.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    
    // Checkbox mode (Google Keep style) - always show checkboxes for easier selection
    var isCheckboxMode by remember { mutableStateOf(false) }
    
    // Track if we're at root for showing recent files
    val isAtRoot = currentPath == initialPath
    val haptic = rememberHapticHelper()

    // Dialog & Sheet States
    var selectedFileForAction by remember { mutableStateOf<FileInfo?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showLabelsDialog by remember { mutableStateOf(false) }
    var labelsInitial by remember { mutableStateOf<List<String>>(emptyList()) }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                val path = currentPath?.toPath()
                if (path != null) {
                    viewModel.createNewFolder(path, name)
                }
                showCreateFolderDialog = false
            }
        )
    }

    if (showRenameDialog && selectedFileForAction != null) {
        RenameDialog(
            currentName = selectedFileForAction!!.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName: String ->
                viewModel.renameFile(selectedFileForAction!!.path, newName)
                showRenameDialog = false
                selectedFileForAction = null
            }
        )
    }

    if (showDeleteDialog && selectedFileForAction != null) {
        DeleteDialog(
            count = 1,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteFile(selectedFileForAction!!.path)
                showDeleteDialog = false
                selectedFileForAction = null
            }
        )
    }

    if (showLabelsDialog && selectedFileForAction != null) {
        LabelsDialog(
            initialLabels = labelsInitial,
            onDismiss = { showLabelsDialog = false },
            onConfirm = { labels ->
                viewModel.setLabels(selectedFileForAction!!.path.toString(), labels)
                showLabelsDialog = false
            }
        )
    }

    if (selectedFileForAction != null && !showRenameDialog && !showDeleteDialog) {
        FileActionSheet(
            file = selectedFileForAction!!,
            isPinned = noteMetadataByPath[selectedFileForAction!!.path.toString()]?.note?.pinned == true,
            onDismiss = { selectedFileForAction = null },
            onRename = { showRenameDialog = true },
            onDelete = { showDeleteDialog = true },
            onShare = { /* TODO: Implement platform share */ },
            onInfo = { /* TODO */ },
            onTogglePin = { viewModel.togglePin(selectedFileForAction!!.path) },
            onEditLabels = {
                labelsInitial = noteMetadataByPath[selectedFileForAction!!.path.toString()]?.labels?.map { it.name } ?: emptyList()
                showLabelsDialog = true
            }
        )
    }

    LaunchedEffect(currentPath) {
        isLoading = true
        val path = viewModel.loadFiles(currentPath)
        if (currentPath == null) {
            currentPath = path
        }
        isLoading = false
    }

    // View Mode State passed from parent
    val displayedFiles = viewModel.getFilteredFiles()
    
    // Keyboard navigation state
    var keyboardSelectedIndex by remember { mutableStateOf(-1) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                
                when {
                    // Space bar toggles selection in checkbox mode
                    event.key == Key.Spacebar && event.type == KeyEventType.KeyDown && isCheckboxMode -> {
                        if (keyboardSelectedIndex in displayedFiles.indices) {
                            val file = displayedFiles[keyboardSelectedIndex]
                            viewModel.toggleSelection(file.path)
                            haptic.performLightClick()
                        }
                        true
                    }
                    // Arrow Down
                    event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown -> {
                        keyboardSelectedIndex = if (keyboardSelectedIndex < displayedFiles.size - 1) {
                            haptic.performLightClick()
                            keyboardSelectedIndex + 1
                        } else keyboardSelectedIndex
                        true
                    }
                    // Arrow Up
                    event.key == Key.DirectionUp && event.type == KeyEventType.KeyDown -> {
                        keyboardSelectedIndex = if (keyboardSelectedIndex > 0) {
                            haptic.performLightClick()
                            keyboardSelectedIndex - 1
                        } else keyboardSelectedIndex
                        true
                    }
                    // Enter to open file
                    event.key == Key.Enter && event.type == KeyEventType.KeyDown -> {
                        if (keyboardSelectedIndex in displayedFiles.indices) {
                            val file = displayedFiles[keyboardSelectedIndex]
                            if (file.isDirectory) {
                                currentPath = file.path.toString()
                            } else {
                                viewModel.recordFileAccess(file.path.toString())
                                onNavigateToEditor(file.path.toString())
                            }
                        }
                        true
                    }
                    // Escape to deselect
                    event.key == Key.Escape && event.type == KeyEventType.KeyDown -> {
                        keyboardSelectedIndex = -1
                        viewModel.clearSelection()
                        true
                    }
                    // Ctrl+A to select all
                    event.isCtrlPressed && event.key == Key.A && event.type == KeyEventType.KeyDown -> {
                        viewModel.selectAll()
                        haptic.performSuccess()
                        true
                    }
                    else -> false
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // M3 Standard: LinearProgressIndicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
            
            // Search bar
            SearchBar(
                query = viewModel.searchQuery.collectAsState().value,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onSearch = { },
                active = false,
                onActiveChange = { },
                placeholder = { Text("Search files...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (viewModel.searchQuery.collectAsState().value.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) { }
            
            // Search in content toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val searchInContent by viewModel.searchInContent.collectAsState()
                
                Text(
                    text = "Search in content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = searchInContent,
                    onClick = {
                        haptic.performLightClick()
                        viewModel.setSearchInContent(!searchInContent)
                    },
                    label = { Text("Content", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (searchInContent) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }
            
            // Recent Files Carousel (only at root)
            if (!isLoading && isAtRoot && recentFiles.isNotEmpty() && !isSelectionMode) {
                RecentFilesCarousel(
                    recentFiles = recentFiles,
                    onFileClick = { filePath ->
                        viewModel.recordFileAccess(filePath)
                        onNavigateToEditor(filePath)
                    }
                )
            }
            
            // Checkbox mode toggle and keyboard shortcut hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCheckboxMode) "Checkbox mode on - tap to select" else "Long-press or press Space to select",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                FilterChip(
                    selected = isCheckboxMode,
                    onClick = { 
                        haptic.performLightClick()
                        isCheckboxMode = !isCheckboxMode
                    },
                    label = { Text("Checkboxes", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            if (isCheckboxMode) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            null,
                            Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            // Filter chips (All | Favorites | Trash)
            if (!isLoading && !isSelectionMode && (files.isNotEmpty() || viewModel.trashFiles.collectAsState().value.isNotEmpty())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterMode == FileFilterMode.ALL,
                        onClick = { viewModel.setFilterMode(FileFilterMode.ALL) },
                        label = { Text("All") },
                        leadingIcon = if (filterMode == FileFilterMode.ALL) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = filterMode == FileFilterMode.FAVORITES,
                        onClick = { viewModel.setFilterMode(FileFilterMode.FAVORITES) },
                        label = { Text("Favorites") },
                        leadingIcon = {
                            Icon(
                                if (filterMode == FileFilterMode.FAVORITES) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                null,
                                Modifier.size(18.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = filterMode == FileFilterMode.TRASH,
                        onClick = { viewModel.setFilterMode(FileFilterMode.TRASH) },
                        label = { Text("Trash") },
                        leadingIcon = {
                            Icon(
                                if (filterMode == FileFilterMode.TRASH) Icons.Default.DeleteForever else Icons.Default.Delete,
                                null,
                                Modifier.size(18.dp)
                            )
                        }
                    )
                }
                
                // File type filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FileTypeFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = viewModel.fileTypeFilter.collectAsState().value == filter,
                            onClick = {
                                haptic.performLightClick()
                                viewModel.setFileTypeFilter(filter)
                            },
                            label = { Text(filter.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                selectedBorderColor = MaterialTheme.colorScheme.outline,
                                enabled = true,
                                selected = true
                            )
                        )
                    }
                }
            }
            
            // Filtered files based on current mode
            val displayedFiles = viewModel.getFilteredFiles()
            val trashFiles by viewModel.trashFiles.collectAsState()
            
            if (isLoading) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    // Content is empty during loading (indicator at top)
                }
            } else if (displayedFiles.isEmpty()) {
                when {
                    filterMode == FileFilterMode.FAVORITES && files.isNotEmpty() -> {
                        EmptyState(
                            title = "No favorites yet",
                            subtitle = "Swipe right on any file to add it to your favorites.",
                            icon = Icons.Outlined.StarOutline
                        )
                    }
                    filterMode == FileFilterMode.TRASH && trashFiles.isEmpty() -> {
                        EmptyState(
                            title = "Trash is empty",
                            subtitle = "Deleted files will appear here for 30 days.",
                            icon = Icons.Default.Delete
                        )
                    }
                    else -> {
                        EmptyListState()
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp, 
                            end = 16.dp, 
                            bottom = 120.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(displayedFiles) { index, file ->
                            val isSelected = selectedFiles.contains(file.path)
                            val isFavorite = favorites.contains(file.path.toString())
                            
                            // Staggered reveal animation
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 30L)
                                isVisible = true
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInVertically { it / 2 } + fadeIn(animationSpec = tween(300)),
                                label = "ItemStagger"
                            ) {
                                Box {
                                    FileGridItem(
                                        file = file,
                                        isSelected = isSelected,
                                        selectionMode = isSelectionMode,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleSelection(file.path)
                                            } else {
                                                if (file.isDirectory) {
                                                    currentPath = file.path.toString()
                                                } else {
                                                    viewModel.recordFileAccess(file.path.toString())
                                                    onNavigateToEditor(file.path.toString())
                                                }
                                            }
                                        },
                                        isPinned = noteMetadataByPath[file.path.toString()]?.note?.pinned == true,
                                        color = noteMetadataByPath[file.path.toString()]?.note?.color,
                                        imagePreviewUrl = noteMetadataByPath[file.path.toString()]?.note?.imagePreviewUrl,
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                viewModel.enterSelectionMode(file.path)
                                            }
                                        }
                                    )
                                    // Favorite indicator
                                    if (isFavorite && !isSelectionMode) {
                                        FavoriteIndicator(
                                            isFavorite = true,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            bottom = 120.dp, 
                            start = 16.dp, 
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(displayedFiles) { index, file ->
                            val isSelected = selectedFiles.contains(file.path)
                            val isFavorite = favorites.contains(file.path.toString())
                            
                            // Staggered reveal animation
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 30L)
                                isVisible = true
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInVertically { it / 2 } + fadeIn(animationSpec = tween(300)),
                                label = "ItemStagger"
                            ) {
                                // Wrap with swipe actions (not in selection mode)
                                if (!isSelectionMode && !file.isDirectory) {
                                    SwipeableFileCard(
                                        isFavorite = isFavorite,
                                        onToggleFavorite = {
                                            viewModel.toggleFavorite(file.path.toString())
                                        },
                                        onDelete = {
                                            selectedFileForAction = file
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        FileItem(
                                            file = file,
                                            isSelected = isSelected,
                                            selectionMode = isSelectionMode,
                                            isFavorite = isFavorite,
                                            onClick = {
                                                viewModel.recordFileAccess(file.path.toString())
                                                onNavigateToEditor(file.path.toString())
                                            },
                                            onLongClick = {
                                                viewModel.enterSelectionMode(file.path)
                                            },
                                            onMoreClick = {
                                                selectedFileForAction = file
                                            },
                                            isCheckboxMode = isCheckboxMode,
                                            isKeyboardSelected = index == keyboardSelectedIndex,
                                            onCheckboxClick = {
                                                viewModel.toggleSelection(file.path)
                                            }
                                        )
                                    }
                                } else {
                                    val isTrashMode = filterMode == FileFilterMode.TRASH
                                    
                                    FileItem(
                                        file = file,
                                        isSelected = isSelected,
                                        selectionMode = isSelectionMode,
                                        isFavorite = false,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleSelection(file.path)
                                            } else if (isTrashMode) {
                                                // Restore file from trash
                                                viewModel.restoreFile(file.path, getOriginalPath(file.name).toPath())
                                            } else {
                                                if (file.isDirectory) {
                                                    currentPath = file.path.toString()
                                                } else {
                                                    viewModel.recordFileAccess(file.path.toString())
                                                    onNavigateToEditor(file.path.toString())
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                viewModel.enterSelectionMode(file.path)
                                            }
                                        },
                                        onMoreClick = {
                                            selectedFileForAction = file
                                        },
                                        isTrashMode = isTrashMode,
                                        onRestore = {
                                            viewModel.restoreFile(file.path, getOriginalPath(file.name).toPath())
                                        },
                                        onDeletePermanently = {
                                            selectedFileForAction = file
                                            showDeleteDialog = true
                                        },
                                        isCheckboxMode = isCheckboxMode,
                                        isKeyboardSelected = index == keyboardSelectedIndex,
                                        onCheckboxClick = {
                                            viewModel.toggleSelection(file.path)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // M3 Expressive-Style: FAB with manual animation
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 24.dp)
        ) {
            val haptic = rememberHapticHelper()
            val trashFiles by viewModel.trashFiles.collectAsState()
            val isTrashMode = filterMode == FileFilterMode.TRASH
            
            if (isTrashMode && trashFiles.isNotEmpty()) {
                // Empty Trash FAB
                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHeavyClick()
                        viewModel.emptyTrash()
                    },
                    icon = { Icon(Icons.Default.DeleteForever, "Empty Trash") },
                    text = { Text("Empty Trash", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
                )
            } else if (!isTrashMode) {
                // Regular Add FAB
                var showAddMenu by remember { mutableStateOf(false) }
                
                val rotation by animateFloatAsState(
                    targetValue = if (showAddMenu) 45f else 0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                    label = "FAB Rotation"
                )

                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHeavyClick()
                        showAddMenu = !showAddMenu
                    },
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (showAddMenu) "Close" else "New",
                            modifier = Modifier.rotate(rotation)
                        )
                    },
                    text = { 
                        AnimatedVisibility(
                            visible = !showAddMenu,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Text("New", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    },
                    expanded = !showAddMenu,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
                )

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                    offset = DpOffset(0.dp, (-8).dp),
                    shape = MaterialTheme.shapes.large,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    DropdownMenuItem(
                        text = { Text("New Note", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        onClick = {
                            haptic.performSuccess()
                            val effectivePath = currentPath ?: initialPath
                            if (effectivePath != null) {
                               viewModel.createNewFile(effectivePath.toPath())
                            }
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New Folder", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.Folder, null) },
                        onClick = {
                            haptic.performSuccess()
                            showCreateFolderDialog = true
                            showAddMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyListState() {
    EmptyState(
        title = "Your notebook is empty",
        subtitle = "Tap the + button to create a new note or folder and start your journey.",
        icon = Icons.Default.FolderOpen
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileItem(
    file: FileInfo,
    isSelected: Boolean,
    selectionMode: Boolean,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    haptic: HapticHelper = rememberHapticHelper(),
    isTrashMode: Boolean = false,
    onRestore: (() -> Unit)? = null,
    onDeletePermanently: (() -> Unit)? = null,
    // Checkbox mode params
    isCheckboxMode: Boolean = false,
    isKeyboardSelected: Boolean = false,
    onCheckboxClick: (() -> Unit)? = null
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isKeyboardSelected -> MaterialTheme.colorScheme.secondaryContainer
        isTrashMode -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        file.isDirectory -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    // Expressive Motion: Scale on Click
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "ItemScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                onClick = {
                    haptic.performHeavyClick()
                    onClick()
                },
                onLongClick = {
                    haptic.performHeavyClick()
                    onLongClick()
                },
                interactionSource = interactionSource,
                indication = ripple()
            ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = when {
            isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isKeyboardSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            else -> null
        },
    ) {
      val content = @Composable {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        if (file.isDirectory) 
                            MaterialTheme.colorScheme.primaryContainer
                        else 
                            MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (file.isDirectory) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                // Show filename with extension in reduced opacity
                if (file.isDirectory) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Folder",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    val baseName = file.name.substringBeforeLast(".")
                    val ext = if (file.name.contains(".")) ".${file.extension}" else ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SharedElementContainer(
                            key = SharedTransitionKeys.fileTitle(file.path.toString()),
                            isSource = true
                        ) {
                            Text(
                                text = baseName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = ext,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    if (!file.preview.isNullOrEmpty()) {
                        val colorScheme = MaterialTheme.colorScheme
                        val previewText = remember(file.preview, colorScheme) {
                            renderGridMarkdown(file.preview, colorScheme)
                        }
                        Text(
                            text = previewText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            if (selectionMode || isCheckboxMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                        haptic.performLightClick()
                        onCheckboxClick?.invoke()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
            } else if (isTrashMode) {
                // Trash mode: show restore and delete permanently actions
                Row {
                    IconButton(
                        onClick = {
                            haptic.performSuccess()
                            onRestore?.invoke()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Restore",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            haptic.performHeavyClick()
                            onDeletePermanently?.invoke()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Delete Permanently",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Row {
                    // Favorite star indicator
                    if (isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    haptic.performHeavyClick()
                                    // Toggle favorite handled by parent
                                }
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = {
                            haptic.performHeavyClick()
                            onMoreClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
      }
      
      if (!file.isDirectory) {
          SharedElementContainer(
              key = SharedTransitionKeys.fileCard(file.path.toString()),
              isSource = true
          ) {
              content()
          }
      } else {
          content()
      }
    }
}
