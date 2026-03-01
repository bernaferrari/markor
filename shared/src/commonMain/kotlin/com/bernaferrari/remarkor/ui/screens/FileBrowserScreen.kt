package com.bernaferrari.remarkor.ui.screens

import com.bernaferrari.remarkor.ui.components.RenameDialog
import com.bernaferrari.remarkor.ui.components.CreateFolderDialog
import com.bernaferrari.remarkor.ui.components.DeleteDialog
import com.bernaferrari.remarkor.ui.components.AssetManagerSheet
import com.bernaferrari.remarkor.ui.components.ShareDialog
import com.bernaferrari.remarkor.domain.service.ImageAssetManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.ui.components.*
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import com.bernaferrari.remarkor.ui.theme.MarkorTheme
import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import okio.FileSystem
import okio.SYSTEM
import okio.Path.Companion.toPath
import androidx.compose.ui.input.key.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

private const val BACK_ROW_RESHOW_DELAY_MS = 180L

@Composable
fun FileBrowserScreen(
    initialPath: String?,
    onNavigateToEditor: (String, Boolean) -> Unit,
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
    onNavigateToEditor: (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FileBrowserViewModel = koinViewModel(),
    isGridView: Boolean,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<String?>(null) }
    var rootPath by remember { mutableStateOf<String?>(null) }
    fun normalizePath(path: String?): String? = path?.trimEnd('/')
    val files by viewModel.files.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val noteMetadataByPath by viewModel.noteMetadataByPath.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var isNavigatingUp by remember { mutableStateOf(false) }
    val haptic = rememberHapticHelper()

    // Dialog & Sheet States
    var selectedFileForAction by remember { mutableStateOf<FileInfo?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showLabelsDialog by remember { mutableStateOf(false) }
    var labelsInitial by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAssetManager by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    // Asset manager - loaded async to avoid blocking
    val assetManager: ImageAssetManager = koinInject()
    var hasAssets by remember { mutableStateOf(false) }
    var fileContent by remember { mutableStateOf("") }
    
    // Load file content and check for assets async
    LaunchedEffect(selectedFileForAction) {
        if (selectedFileForAction != null && !selectedFileForAction!!.isDirectory) {
            hasAssets = assetManager.hasAssetsFolder(selectedFileForAction!!.path)
            fileContent = try {
                FileSystem.Companion.SYSTEM.read(selectedFileForAction!!.path) { readUtf8() }
            } catch (e: Exception) {
                ""
            }
        } else {
            hasAssets = false
            fileContent = ""
        }
    }

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
            hasAssets = hasAssets,
            onDismiss = { selectedFileForAction = null },
            onRename = { showRenameDialog = true },
            onDelete = { showDeleteDialog = true },
            onShare = { showShareDialog = true },
            onInfo = { /* TODO */ },
            onTogglePin = { viewModel.togglePin(selectedFileForAction!!.path) },
            onEditLabels = {
                labelsInitial = noteMetadataByPath[selectedFileForAction!!.path.toString()]?.labels?.map { it.name } ?: emptyList()
                showLabelsDialog = true
            },
            onManageAssets = { showAssetManager = true }
        )
    }
    
    // Share Dialog
    if (showShareDialog && selectedFileForAction != null) {
        ShareDialog(
            filePath = selectedFileForAction!!.path,
            hasAssets = hasAssets,
            assetManager = assetManager,
            onDismiss = { showShareDialog = false }
        )
    }
    
    // Asset Manager Sheet - uses pre-loaded fileContent
    if (showAssetManager && selectedFileForAction != null) {
        AssetManagerSheet(
            filePath = selectedFileForAction!!.path,
            content = fileContent,
            assetManager = assetManager,
            onDismiss = { showAssetManager = false },
            onAssetsDeleted = {
                // Could refresh UI if needed
            }
        )
    }

    LaunchedEffect(currentPath) {
        isLoading = true
        val loadedPath = viewModel.loadFiles(currentPath)
        val normalizedCurrent = normalizePath(currentPath)
        val normalizedLoaded = normalizePath(loadedPath)

        // Keep local path state in sync with repository-resolved path
        // (e.g. migration from stale "/Notebook" subpaths).
        if (normalizedCurrent != normalizedLoaded) {
            if (normalizePath(rootPath) == normalizedCurrent || rootPath == null) {
                rootPath = loadedPath
            }
            currentPath = loadedPath
            isLoading = false
            return@LaunchedEffect
        }
        if (rootPath == null) {
            rootPath = loadedPath
        }
        isLoading = false
        if (isNavigatingUp) {
            delay(BACK_ROW_RESHOW_DELAY_MS)
            isNavigatingUp = false
        }
    }

    LaunchedEffect(initialPath) {
        val normalizedInitial = normalizePath(initialPath) ?: return@LaunchedEffect
        val normalizedRoot = normalizePath(rootPath)

        // Handle external root changes (e.g. user switched storage mode in Settings).
        if (normalizedRoot != null && normalizedInitial != normalizedRoot) {
            currentPath = normalizedInitial
            rootPath = normalizedInitial
        }
    }

    // View Mode State passed from parent
    val displayedFiles = viewModel.getFilteredFiles()
    var cachedDisplayedFiles by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
    LaunchedEffect(displayedFiles) {
        if (displayedFiles.isNotEmpty()) {
            cachedDisplayedFiles = displayedFiles
        }
    }
    val visibleFiles = if (displayedFiles.isEmpty() && isLoading && cachedDisplayedFiles.isNotEmpty()) {
        cachedDisplayedFiles
    } else {
        displayedFiles
    }
    
    // Keyboard navigation state
    var keyboardSelectedIndex by remember { mutableStateOf(-1) }

    val canNavigateToParent = remember(currentPath, rootPath) {
        val current = normalizePath(currentPath)
        val root = normalizePath(rootPath)
        current != null && root != null && current != root
    }

    val parentDirectoryLabel = remember(currentPath) {
        runCatching {
            normalizePath(currentPath)
                ?.toPath()
                ?.parent
                ?.name
                ?.ifBlank { "Notebook" }
                ?: "Notebook"
        }.getOrDefault("Notebook")
    }
    var displayedParentDirectoryLabel by remember { mutableStateOf(parentDirectoryLabel) }
    LaunchedEffect(parentDirectoryLabel, isNavigatingUp) {
        if (!isNavigatingUp) {
            displayedParentDirectoryLabel = parentDirectoryLabel
        }
    }

    fun navigateToParentDirectory() {
        val current = normalizePath(currentPath) ?: return
        isNavigatingUp = true
        val root = normalizePath(rootPath)
        val parent = runCatching {
            current.toPath().parent?.toString()?.trimEnd('/')
        }.getOrNull()

        when {
            parent.isNullOrEmpty() -> {
                if (root != null) currentPath = root else onNavigateBack()
            }
            root == null -> currentPath = parent
            parent.length < root.length || !parent.startsWith(root) -> currentPath = root
            else -> currentPath = parent
        }
    }

    BackHandler(enabled = canNavigateToParent) {
        navigateToParentDirectory()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                
                when {
                    // Arrow Down
                    event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown -> {
                            keyboardSelectedIndex = if (keyboardSelectedIndex < visibleFiles.size - 1) {
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
                        if (keyboardSelectedIndex in visibleFiles.indices) {
                            val file = visibleFiles[keyboardSelectedIndex]
                            if (file.isDirectory) {
                                currentPath = file.path.toString()
                            } else {
                                viewModel.recordFileAccess(file.path.toString())
                                onNavigateToEditor(file.path.toString(), false)
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

            AnimatedVisibility(
                visible = canNavigateToParent && filterMode != FileFilterMode.TRASH && !isNavigatingUp,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val backRowShape = MaterialTheme.shapes.large
                val backRowInteraction = remember { MutableInteractionSource() }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = backRowShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(backRowShape)
                            .clickable(
                                interactionSource = backRowInteraction,
                                indication = ripple()
                            ) {
                                haptic.performLightClick()
                                navigateToParentDirectory()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(Res.string.back_to_with_arg, displayedParentDirectoryLabel),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Filtered files based on current mode - use displayedFiles from parent scope
            val trashFiles by viewModel.trashFiles.collectAsState()
            
            if (isLoading && visibleFiles.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    // Content is empty during loading (indicator at top)
                }
            } else if (visibleFiles.isEmpty()) {
                val shouldOffsetForBottomActions = !isSelectionMode && filterMode != FileFilterMode.TRASH
                val emptyStateBottomOffset = if (shouldOffsetForBottomActions) 120.dp else 0.dp
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                        .padding(bottom = emptyStateBottomOffset)
                ) {
                    when {
                        filterMode == FileFilterMode.FAVORITES && files.isNotEmpty() -> {
                            EmptyState(
                                title = stringResource(Res.string.no_favorites_yet),
                                subtitle = stringResource(Res.string.no_favorites_yet_description),
                                icon = Icons.Outlined.StarOutline
                            )
                        }
                        filterMode == FileFilterMode.TRASH && trashFiles.isEmpty() -> {
                            EmptyState(
                                title = stringResource(Res.string.trash_is_empty),
                                subtitle = stringResource(Res.string.trash_is_empty_description),
                                icon = Icons.Default.Delete
                            )
                        }
                        else -> {
                            EmptyListState()
                        }
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 16.dp, 
                            end = 16.dp, 
                            bottom = 120.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(visibleFiles) { _, file ->
                            val isSelected = selectedFiles.contains(file.path)
                            val isFavorite = favorites.contains(file.path.toString())
                            Box {
                                val noteLabels = noteMetadataByPath[file.path.toString()]?.labels?.map { it.name } ?: emptyList()
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
                                                onNavigateToEditor(file.path.toString(), false)
                                            }
                                        }
                                    },
                                    isPinned = noteMetadataByPath[file.path.toString()]?.note?.pinned == true,
                                    color = noteMetadataByPath[file.path.toString()]?.note?.color,
                                    imagePreviewUrl = noteMetadataByPath[file.path.toString()]?.note?.imagePreviewUrl,
                                    labels = noteLabels,
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
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 120.dp, 
                            start = 16.dp, 
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(visibleFiles) { index, file ->
                            val isSelected = selectedFiles.contains(file.path)
                            val isFavorite = favorites.contains(file.path.toString())
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
                                        noteColor = noteMetadataByPath[file.path.toString()]?.note?.color,
                                        preview = noteMetadataByPath[file.path.toString()]?.note?.preview,
                                        onClick = {
                                            viewModel.recordFileAccess(file.path.toString())
                                            onNavigateToEditor(file.path.toString(), false)
                                        },
                                        onLongClick = {
                                            viewModel.enterSelectionMode(file.path)
                                        },
                                        onMoreClick = {
                                            selectedFileForAction = file
                                        },
                                        isKeyboardSelected = index == keyboardSelectedIndex
                                    )
                                }
                            } else {
                                val isTrashMode = filterMode == FileFilterMode.TRASH
                                
                                FileItem(
                                    file = file,
                                    isSelected = isSelected,
                                    selectionMode = isSelectionMode,
                                    isFavorite = false,
                                    noteColor = noteMetadataByPath[file.path.toString()]?.note?.color,
                                    preview = noteMetadataByPath[file.path.toString()]?.note?.preview,
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
                                                onNavigateToEditor(file.path.toString(), false)
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
                                    isKeyboardSelected = index == keyboardSelectedIndex
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // M3 Expressive: Bottom-center extended FAB
        val trashFiles by viewModel.trashFiles.collectAsState()
        val isTrashMode = filterMode == FileFilterMode.TRASH
        
        if (!isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                if (isTrashMode && trashFiles.isNotEmpty()) {
                    // Empty Trash Button
                    FilledTonalButton(
                        onClick = {
                            haptic.performHeavyClick()
                            viewModel.emptyTrash()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Empty Trash", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                } else if (!isTrashMode) {
                    // Split action: primary creates note, trailing opens extra actions.
                    var showAddMenu by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    val createNote = {
                        scope.launch {
                            haptic.performSuccess()
                            val effectivePath = currentPath
                                ?: initialPath
                                ?: viewModel.loadFiles(null).also { loadedPath ->
                                    currentPath = loadedPath
                                }
                            val noteName = "new_note_${kotlin.random.Random.nextInt(1000, 9999)}.md"
                            viewModel.createNewFile(
                                parent = effectivePath.toPath(),
                                name = noteName,
                                onCreated = { createdPath ->
                                    onNavigateToEditor(createdPath.toString(), true)
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHeavyClick()
                                createNote()
                            },
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(Res.string.create_new)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "New Note",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                haptic.performLightClick()
                                showAddMenu = !showAddMenu
                            },
                            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .width(56.dp)
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(Res.string.more_create_options)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false },
                        offset = DpOffset(0.dp, (-8).dp),
                        shape = MaterialTheme.shapes.large,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
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
}

@Composable
private fun EmptyListState() {
    EmptyState(
        title = stringResource(Res.string.notebook_is_empty),
        subtitle = stringResource(Res.string.notebook_is_empty_description),
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
    noteColor: Int? = null,
    preview: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    haptic: HapticHelper = rememberHapticHelper(),
    isTrashMode: Boolean = false,
    onRestore: (() -> Unit)? = null,
    onDeletePermanently: (() -> Unit)? = null,
    isKeyboardSelected: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = when {
        isSelected -> colorScheme.primaryContainer
        isKeyboardSelected -> colorScheme.secondaryContainer
        isTrashMode -> colorScheme.errorContainer.copy(alpha = 0.3f)
        file.isDirectory -> colorScheme.surface
        noteColor != null -> resolveNoteSurfaceColor(noteColor, colorScheme, fallback = colorScheme.surfaceContainerLow)
        else -> colorScheme.surfaceContainerLow
    }
    val effectiveBackground = containerColor.compositeOver(colorScheme.surface)
    val accentOverride = noteColor?.let(::Color)
    val previewPalette = remember(effectiveBackground, colorScheme, accentOverride) {
        resolveMarkdownColorPalette(
            colorScheme = colorScheme,
            backgroundColor = effectiveBackground,
            accentColorOverride = accentOverride
        )
    }
    val iconContainerColor = remember(file.isDirectory, colorScheme, accentOverride) {
        when {
            file.isDirectory -> colorScheme.primaryContainer
            accentOverride != null -> {
                val alpha = if (colorScheme.surface.luminance() < 0.5f) 0.36f else 0.22f
                accentOverride.copy(alpha = alpha).compositeOver(colorScheme.surfaceContainerHigh)
            }
            else -> colorScheme.secondaryContainer
        }
    }
    val iconTintColor = remember(file.isDirectory, colorScheme, iconContainerColor, accentOverride) {
        when {
            file.isDirectory -> colorScheme.primary
            accentOverride != null -> {
                resolveMarkdownColorPalette(
                    colorScheme = colorScheme,
                    backgroundColor = iconContainerColor,
                    accentColorOverride = accentOverride
                ).accent
            }
            else -> colorScheme.secondary
        }
    }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    val card = @Composable {
        val cardShape = MaterialTheme.shapes.medium
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
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
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = when {
                isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                isKeyboardSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                else -> null
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                    // Icon Container
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(iconContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                            contentDescription = null,
                            tint = iconTintColor,
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
                                text = stringResource(Res.string.folder),
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

                            val effectivePreview = preview?.takeIf { it.isNotBlank() }
                                ?: file.preview?.takeIf { it.isNotBlank() }
                            if (!effectivePreview.isNullOrEmpty()) {
                                val previewText = remember(effectivePreview, colorScheme, effectiveBackground, accentOverride) {
                                    renderGridMarkdown(
                                        effectivePreview,
                                        colorScheme,
                                        backgroundColor = effectiveBackground,
                                        accentColorOverride = accentOverride
                                    )
                                }
                                Text(
                                    text = previewText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = previewPalette.body,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (selectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                haptic.performLightClick()
                                onClick()
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
                                    contentDescription = stringResource(Res.string.restore),
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
                                    contentDescription = stringResource(Res.string.delete_permanently),
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
                                    contentDescription = stringResource(Res.string.favorite),
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
                                    contentDescription = stringResource(Res.string.more),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
        }
    }
    
    if (!file.isDirectory) {
        SharedElementContainer(
            key = SharedTransitionKeys.fileCard(file.path.toString()),
            isSource = true,
            useSharedBounds = true
        ) {
            card()
        }
    } else {
        card()
    }
}
