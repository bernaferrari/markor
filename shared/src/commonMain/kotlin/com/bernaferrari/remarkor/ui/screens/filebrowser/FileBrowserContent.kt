package com.bernaferrari.remarkor.ui.screens.filebrowser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.domain.repository.IAssetRepository
import com.bernaferrari.remarkor.ui.components.AssetManagerSheet
import com.bernaferrari.remarkor.ui.components.BackHandler
import com.bernaferrari.remarkor.ui.components.CreateFolderDialog
import com.bernaferrari.remarkor.ui.components.DeleteDialog
import com.bernaferrari.remarkor.ui.components.EmptyState
import com.bernaferrari.remarkor.ui.components.FileActionSheet
import com.bernaferrari.remarkor.ui.components.FileGridItem
import com.bernaferrari.remarkor.ui.components.HapticHelper
import com.bernaferrari.remarkor.ui.components.LabelsDialog
import com.bernaferrari.remarkor.ui.components.RenameDialog
import com.bernaferrari.remarkor.ui.components.ShareDialog
import com.bernaferrari.remarkor.ui.components.SharedElementContainer
import com.bernaferrari.remarkor.ui.components.SharedTransitionKeys
import com.bernaferrari.remarkor.ui.components.SwipeableFileCard
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.components.renderGridMarkdown
import com.bernaferrari.remarkor.ui.components.resolveMarkdownColorPalette
import com.bernaferrari.remarkor.ui.components.resolveNoteSurfaceColor
import com.bernaferrari.remarkor.ui.screens.filebrowser.BACK_ROW_RESHOW_DELAY_MS
import com.bernaferrari.remarkor.ui.screens.filebrowser.EmptyListState
import com.bernaferrari.remarkor.ui.screens.filebrowser.FileItem
import com.bernaferrari.remarkor.ui.screens.filebrowser.getOriginalPath
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.back_to_with_arg
import markor.shared.generated.resources.archive_is_empty
import markor.shared.generated.resources.archive_is_empty_description
import markor.shared.generated.resources.create_new
import markor.shared.generated.resources.delete_permanently
import markor.shared.generated.resources.favorite
import markor.shared.generated.resources.folder
import markor.shared.generated.resources.more
import markor.shared.generated.resources.more_create_options
import markor.shared.generated.resources.no_favorites_yet
import markor.shared.generated.resources.no_favorites_yet_description
import markor.shared.generated.resources.notebook_is_empty
import markor.shared.generated.resources.notebook_is_empty_description
import markor.shared.generated.resources.restore
import markor.shared.generated.resources.trash_is_empty
import markor.shared.generated.resources.trash_is_empty_description
import okio.FileSystem
import okio.Path.Companion.toPath
import com.bernaferrari.remarkor.util.platformFileSystem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FileBrowserContent(
    initialPath: String?,
    onNavigateToEditor: (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FileBrowserViewModel = koinViewModel(),
    isGridView: Boolean,
    /** Adaptive staggered grid min width (dp). Wider on large screens. */
    gridMinCellWidthDp: Int = 150,
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
    var showFileActionSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showLabelsDialog by remember { mutableStateOf(false) }
    var labelsInitial by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAssetManager by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Asset manager - loaded async to avoid blocking
    val assetManager: IAssetRepository = koinInject()
    var hasAssets by remember { mutableStateOf(false) }
    var fileContent by remember { mutableStateOf("") }

    // Load file content and check for assets async
    LaunchedEffect(selectedFileForAction) {
        if (selectedFileForAction != null && !selectedFileForAction!!.isDirectory) {
            hasAssets = assetManager.hasAssetsFolder(selectedFileForAction!!.path)
            fileContent = try {
                platformFileSystem.read(selectedFileForAction!!.path) { readUtf8() }
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
            onDismiss = {
                showRenameDialog = false
                selectedFileForAction = null
            },
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
            onDismiss = {
                showDeleteDialog = false
                selectedFileForAction = null
            },
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
            onDismiss = {
                showLabelsDialog = false
                selectedFileForAction = null
            },
            onConfirm = { labels ->
                viewModel.setLabels(selectedFileForAction!!.path.toString(), labels)
                showLabelsDialog = false
                selectedFileForAction = null
            }
        )
    }

    val actionFile = selectedFileForAction
    if (actionFile != null && showFileActionSheet) {
        FileActionSheet(
            file = actionFile,
            isPinned = noteMetadataByPath[actionFile.path.toString()]?.pinned == true,
            isFavorite = favorites.contains(actionFile.path.toString()),
            hasAssets = hasAssets,
            onDismiss = {
                showFileActionSheet = false
                selectedFileForAction = null
            },
            onRename = {
                showFileActionSheet = false
                showRenameDialog = true
            },
            onDelete = {
                showFileActionSheet = false
                showDeleteDialog = true
            },
            onShare = {
                showFileActionSheet = false
                showShareDialog = true
            },
            onInfo = {
                showFileActionSheet = false
                selectedFileForAction = null
            },
            onTogglePin = {
                viewModel.togglePin(actionFile.path)
                showFileActionSheet = false
                selectedFileForAction = null
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(actionFile.path.toString())
                showFileActionSheet = false
                selectedFileForAction = null
            },
            onEditLabels = {
                labelsInitial =
                    noteMetadataByPath[actionFile.path.toString()]?.labels?.map { it.name }
                        ?: emptyList()
                showFileActionSheet = false
                showLabelsDialog = true
            },
            onManageAssets = {
                showFileActionSheet = false
                showAssetManager = true
            }
        )
    }

    // Share Dialog
    if (showShareDialog && selectedFileForAction != null) {
        ShareDialog(
            filePath = selectedFileForAction!!.path,
            hasAssets = hasAssets,
            assetManager = assetManager,
            onDismiss = {
                showShareDialog = false
                selectedFileForAction = null
            }
        )
    }

    // Asset Manager Sheet - uses pre-loaded fileContent
    if (showAssetManager && selectedFileForAction != null) {
        AssetManagerSheet(
            filePath = selectedFileForAction!!.path,
            content = fileContent,
            assetManager = assetManager,
            onDismiss = {
                showAssetManager = false
                selectedFileForAction = null
            },
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
    val visibleFiles =
        if (displayedFiles.isEmpty() && isLoading && cachedDisplayedFiles.isNotEmpty()) {
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
                            text = stringResource(
                                Res.string.back_to_with_arg,
                                displayedParentDirectoryLabel
                            ),
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
                val shouldOffsetForBottomActions =
                    !isSelectionMode && filterMode != FileFilterMode.TRASH
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

                        filterMode == FileFilterMode.ARCHIVE -> {
                            EmptyState(
                                title = stringResource(Res.string.archive_is_empty),
                                subtitle = stringResource(Res.string.archive_is_empty_description),
                                icon = Icons.Outlined.Archive,
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
                        columns = StaggeredGridCells.Adaptive(gridMinCellWidthDp.dp),
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
                                val noteLabels =
                                    noteMetadataByPath[file.path.toString()]?.labels?.map { it.name }
                                        ?: emptyList()
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
                                    isPinned = noteMetadataByPath[file.path.toString()]?.pinned == true,
                                    isFavorite = isFavorite,
                                    color = noteMetadataByPath[file.path.toString()]?.color,
                                    imagePreviewUrl = noteMetadataByPath[file.path.toString()]?.imagePreviewUrl,
                                    labels = noteLabels,
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            viewModel.enterSelectionMode(file.path)
                                        }
                                    },
                                )
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
                                        noteColor = noteMetadataByPath[file.path.toString()]?.color,
                                        preview = noteMetadataByPath[file.path.toString()]?.preview,
                                        onClick = {
                                            viewModel.recordFileAccess(file.path.toString())
                                            onNavigateToEditor(file.path.toString(), false)
                                        },
                                        onLongClick = {
                                            viewModel.enterSelectionMode(file.path)
                                        },
                                        onMoreClick = {
                                            selectedFileForAction = file
                                            showFileActionSheet = true
                                        },
                                        onToggleFavorite = {
                                            viewModel.toggleFavorite(file.path.toString())
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
                                    noteColor = noteMetadataByPath[file.path.toString()]?.color,
                                    preview = noteMetadataByPath[file.path.toString()]?.preview,
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(file.path)
                                        } else if (isTrashMode) {
                                            // Restore file from trash
                                            viewModel.restoreFile(
                                                file.path,
                                                getOriginalPath(file.name).toPath()
                                            )
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
                                        showFileActionSheet = true
                                    },
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(file.path.toString())
                                    },
                                    isTrashMode = isTrashMode,
                                    onRestore = {
                                        viewModel.restoreFile(
                                            file.path,
                                            getOriginalPath(file.name).toPath()
                                        )
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
                        Text(
                            "Empty Trash",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else if (!isTrashMode) {
                    // Split action: primary creates note, trailing opens extra actions.
                    var showAddMenu by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    val createNote = {
                        scope.launch {
                            haptic.performSuccess()
                            viewModel.setFilterMode(FileFilterMode.ALL)
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
                            shape = RoundedCornerShape(
                                topStart = 28.dp,
                                bottomStart = 28.dp,
                                topEnd = 10.dp,
                                bottomEnd = 10.dp
                            )
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
                            shape = RoundedCornerShape(
                                topStart = 10.dp,
                                bottomStart = 10.dp,
                                topEnd = 28.dp,
                                bottomEnd = 28.dp
                            ),
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
                                viewModel.setFilterMode(FileFilterMode.ALL)
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
