package com.bernaferrari.remarkor.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.bernaferrari.remarkor.domain.repository.IAssetRepository
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import com.bernaferrari.remarkor.domain.model.Document

import com.bernaferrari.remarkor.domain.service.PickedImage
import com.bernaferrari.remarkor.ui.components.EditorAction
import com.bernaferrari.remarkor.ui.components.FormatToolbar
import com.bernaferrari.remarkor.ui.components.MarkdownVisualTransformation
import com.bernaferrari.remarkor.ui.components.SharedElementContainer
import com.bernaferrari.remarkor.ui.components.SharedTransitionKeys
import com.bernaferrari.remarkor.ui.components.resolveMarkdownColorPalette
import com.bernaferrari.remarkor.ui.components.resolveNoteSurfaceColor
import com.bernaferrari.remarkor.ui.theme.MarkorTheme
import com.bernaferrari.remarkor.ui.viewmodel.EditorViewModel
import com.bernaferrari.remarkor.ui.screens.editor.EditorTab
import com.bernaferrari.remarkor.ui.screens.editor.PreviewTab
import com.bernaferrari.remarkor.ui.screens.editor.buildDocumentInfoStats
import com.bernaferrari.remarkor.ui.screens.editor.commitTitleRenameIfNeeded
import com.bernaferrari.remarkor.ui.screens.editor.formatIntStringResource
import com.bernaferrari.remarkor.ui.screens.editor.formatStorageSize
import com.bernaferrari.remarkor.ui.screens.editor.insertAtCursor
import com.bernaferrari.remarkor.ui.screens.editor.insertAtStartOfLine
import com.bernaferrari.remarkor.ui.screens.editor.wrapSelection
import com.bernaferrari.remarkor.util.resolveImageUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.characters_no_spaces_with_arg
import markor.shared.generated.resources.characters_with_arg
import markor.shared.generated.resources.close
import markor.shared.generated.resources.document_info
import markor.shared.generated.resources.document_outline
import markor.shared.generated.resources.export
import markor.shared.generated.resources.headings_with_arg
import markor.shared.generated.resources.lines_with_arg
import markor.shared.generated.resources.more_options
import markor.shared.generated.resources.nothing_to_preview
import markor.shared.generated.resources.size_utf8_with_arg
import markor.shared.generated.resources.words_with_arg
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * Platform-specific back handler.
 * On Android, uses BackHandler. On iOS, does nothing (no hardware back button).
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)

/**
 * Platform-specific image picker.
 * Returns null if cancelled or not supported.
 */
@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit
): () -> Unit

@Composable
fun EditorScreen(
    filePath: String,
    onNavigateBack: () -> Unit,
    openKeyboardOnStart: Boolean = false,
    viewModel: EditorViewModel = koinViewModel(),
    shareService: com.bernaferrari.remarkor.domain.service.ShareService = org.koin.compose.koinInject()
) {
    val settingsRepository: ISettingsRepository = org.koin.compose.koinInject()
    val showLineNumbers by settingsRepository.isShowLineNumbers.collectAsState(initial = false)
    val editorFontSize by settingsRepository.getEditorFontSize.collectAsState(initial = 16)
    val wordWrap by settingsRepository.isWordWrap.collectAsState(initial = true)

    val scope = rememberCoroutineScope()
    var isPreviewMode by remember { mutableStateOf(false) }
    var activeFilePath by remember(filePath) { mutableStateOf(filePath) }
    var document by remember { mutableStateOf<Document?>(null) }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var titleInput by remember { mutableStateOf("") }
    var initialAutoFocusConsumed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Handle System Back Press
    PlatformBackHandler(enabled = true) {
        scope.launch {
            val renameResult = commitTitleRenameIfNeeded(
                titleInput = titleInput,
                document = document,
                renameDocument = viewModel::renameDocument
            )
            titleInput = renameResult.updatedTitleInput
            document = renameResult.document
            renameResult.updatedPath?.let { activeFilePath = it.toString() }

            if (hasUnsavedChanges && document != null) {
                viewModel.saveDocument(document!!, content.text)
            }
            onNavigateBack()
        }
    }

    LaunchedEffect(activeFilePath) {
        isLoading = true
        document = viewModel.loadDocument(activeFilePath)
        val text = document?.content ?: ""
        val loadedDocument = document
        titleInput =
            loadedDocument?.title?.ifBlank { loadedDocument.name.substringBeforeLast(".") } ?: ""
        content = TextFieldValue(text)
        isLoading = false
    }

    // Auto-save logic
    LaunchedEffect(content) {
        if (hasUnsavedChanges && document != null) {
            delay(2000) // Debounce 2 seconds
            viewModel.saveDocument(document!!, content.text)
            hasUnsavedChanges = false
        }
    }

    // Undo/Redo State
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }
    var undoDebounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var editorFocusNonce by remember { mutableIntStateOf(0) }

    // Search State
    var showSearchDialog by remember { mutableStateOf(false) }

    // Export Dialog State
    var showExportDialog by remember { mutableStateOf(false) }

    // Outline State
    var showOutlinePanel by remember { mutableStateOf(false) }
    var showDocumentInfoDialog by remember { mutableStateOf(false) }

    val outlineItems = remember(content.text) {
        com.bernaferrari.remarkor.ui.components.parseOutline(content.text)
    }
    val documentInfoStats = remember(content.text, outlineItems.size) {
        buildDocumentInfoStats(content.text, outlineItems.size)
    }

    // NEW: Color Selection State
    var showColorSheet by remember { mutableStateOf(false) }
    val noteColor by viewModel.noteColor.collectAsState()

    // Image picker and asset manager
    val assetManager: IAssetRepository = org.koin.compose.koinInject()
    var pendingImageInsert by remember { mutableStateOf<PickedImage?>(null) }

    val launchImagePicker = rememberImagePickerLauncher { pickedImage ->
        pendingImageInsert = pickedImage
    }

    // Handle picked image
    LaunchedEffect(pendingImageInsert) {
        val image = pendingImageInsert ?: return@LaunchedEffect
        pendingImageInsert = null // Reset

        val currentPath = activeFilePath.toPath()
        val relativePath = assetManager.addImage(currentPath, image.data, image.fileName)

        if (relativePath != null) {
            // Insert markdown image syntax at cursor
            val imageMarkdown = "![]($relativePath)"
            content = insertAtCursor(content, imageMarkdown, "")
            hasUnsavedChanges = true
        }
    }

    // NEW: Focus Mode State
    var isFocusMode by remember { mutableStateOf(false) }
    val currentParagraphIndex = remember(content.selection.start, content.text) {
        com.bernaferrari.remarkor.ui.components.getCurrentParagraphIndex(
            content.text,
            content.selection.start
        )
    }

    // NEW: Slash Command State
    val slashCommandState = remember(content.text, content.selection) {
        com.bernaferrari.remarkor.ui.components.detectSlashCommand(content)
    }
    val showSlashMenu = slashCommandState != null
    val slashQuery = slashCommandState?.second ?: ""
    val slashStartIndex = slashCommandState?.first ?: -1


    // Helper to push to undo stack
    fun pushToUndo(value: TextFieldValue) {
        if (undoStack.isEmpty() || undoStack.last().text != value.text) {
            undoStack.add(value)
            redoStack.clear()
            if (undoStack.size > 50) undoStack.removeAt(0) // Limit stack size
        }
    }

    if (showSearchDialog) {
        com.bernaferrari.remarkor.ui.components.AdvancedSearchReplaceDialog(
            onDismiss = { showSearchDialog = false },
            onFindNext = { query ->
                val text = content.text
                val startIndex = content.selection.end
                var index = text.indexOf(query, startIndex, ignoreCase = true)
                if (index == -1) {
                    // Wrap around
                    index = text.indexOf(query, 0, ignoreCase = true)
                }
                if (index != -1) {
                    content = content.copy(
                        selection = androidx.compose.ui.text.TextRange(
                            index,
                            index + query.length
                        )
                    )
                }
            },
            onReplace = { query, replacement ->
                val text = content.text
                val selection = content.selection
                // Check if current selection matches query (simple verify before replace)
                val selectedText = if (selection.min != selection.max) text.substring(
                    selection.min,
                    selection.max
                ) else ""

                if (selectedText.equals(query, ignoreCase = true)) {
                    // Replace selection
                    val newText = text.replaceRange(selection.min, selection.max, replacement)
                    pushToUndo(content)
                    content = content.copy(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(selection.min + replacement.length)
                    )
                    hasUnsavedChanges = true
                } else {
                    // Try to find next and select
                    val startIndex = content.selection.end
                    var index = text.indexOf(query, startIndex, ignoreCase = true)
                    if (index == -1) index = text.indexOf(query, 0, ignoreCase = true)

                    if (index != -1) {
                        // Select it so user can click replace again
                        content = content.copy(
                            selection = androidx.compose.ui.text.TextRange(
                                index,
                                index + query.length
                            )
                        )
                        // Optionally auto-replace? Standard behavior is "Find, then Replace".
                        // Let's just find it first if not selected.
                    }
                }
            },
            onReplaceAll = { query, replacement ->
                val newText = content.text.replace(query, replacement, ignoreCase = true)
                if (newText != content.text) {
                    pushToUndo(content)
                    content = content.copy(text = newText)
                    hasUnsavedChanges = true
                }
            }
        )
    }

    // Flip Animation State
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPreviewMode) {
        rotation.animateTo(
            targetValue = if (isPreviewMode) 180f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val colorScheme = MaterialTheme.colorScheme
    val editorBackgroundColor = colorScheme.background
    val editorContentSurfaceColor = remember(noteColor, colorScheme) {
        resolveNoteSurfaceColor(noteColor, colorScheme, fallback = colorScheme.surfaceContainerLow)
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .onPreviewKeyEvent { event ->
                // Escape to close dialogs
                if (event.key == Key.Escape && event.type == KeyEventType.KeyDown) {
                    when {
                        showSearchDialog -> {
                            showSearchDialog = false; true
                        }

                        else -> false
                    }
                }
                // Ctrl/Cmd + shortcuts
                else if (event.type == KeyEventType.KeyDown && (event.isCtrlPressed || event.isMetaPressed)) {
                    when (event.key) {
                        Key.S -> {
                            document?.let { doc ->
                                scope.launch {
                                    viewModel.saveDocument(doc, content.text)
                                    hasUnsavedChanges = false
                                }
                            }
                            true
                        }

                        Key.Z -> {
                            if (event.isShiftPressed) {
                                // Redo
                                if (redoStack.isNotEmpty()) {
                                    undoStack.add(content)
                                    content = redoStack.removeLast()
                                    hasUnsavedChanges = true
                                }
                            } else {
                                // Undo
                                if (undoStack.isNotEmpty()) {
                                    redoStack.add(content)
                                    content = undoStack.removeLast()
                                    hasUnsavedChanges = true
                                }
                            }
                            true
                        }

                        Key.Y -> { // Redo alternative
                            if (redoStack.isNotEmpty()) {
                                undoStack.add(content)
                                content = redoStack.removeLast()
                                hasUnsavedChanges = true
                            }
                            true
                        }

                        Key.B -> {
                            pushToUndo(content)
                            content = wrapSelection(content, "**", "**")
                            hasUnsavedChanges = true
                            true
                        }

                        Key.I -> {
                            pushToUndo(content)
                            content = wrapSelection(content, "_", "_")
                            hasUnsavedChanges = true
                            true
                        }

                        Key.U -> {
                            pushToUndo(content)
                            content = wrapSelection(content, "`", "`")
                            hasUnsavedChanges = true
                            true
                        }

                        Key.K -> {
                            pushToUndo(content)
                            content = wrapSelection(content, "[", "](url)")
                            hasUnsavedChanges = true
                            true
                        }

                        Key.F -> {
                            showSearchDialog = true
                            true
                        }

                        Key.H -> {
                            // Find/Replace
                            showSearchDialog = true
                            true
                        }

                        Key.A -> {
                            // Select all
                            true // Let the text field handle it
                        }

                        Key.C -> {
                            // Copy - let system handle
                            false
                        }

                        Key.X -> {
                            // Cut - let system handle
                            false
                        }

                        Key.V -> {
                            // Paste - let system handle
                            false
                        }

                        Key.Enter -> {
                            // New line with potential smart behavior
                            false // Let default behavior handle
                        }

                        else -> false
                    }
                }
                // Tab key for indentation
                else if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                    // event.preventDefault() // Removed, using return true to consume
                    pushToUndo(content)
                    content = content.copy(text = content.text + "\t")
                    hasUnsavedChanges = true
                    true
                } else false
            },
        containerColor = editorBackgroundColor,
        topBar = {
            TopAppBar(
                title = { Spacer(modifier = Modifier) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val renameResult = commitTitleRenameIfNeeded(
                                    titleInput = titleInput,
                                    document = document,
                                    renameDocument = viewModel::renameDocument
                                )
                                titleInput = renameResult.updatedTitleInput
                                document = renameResult.document
                                renameResult.updatedPath?.let { activeFilePath = it.toString() }

                                if (hasUnsavedChanges && document != null) {
                                    viewModel.saveDocument(document!!, content.text)
                                }
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // View Mode Toggle
                    IconButton(
                        onClick = {
                            scope.launch {
                                val renameResult = commitTitleRenameIfNeeded(
                                    titleInput = titleInput,
                                    document = document,
                                    renameDocument = viewModel::renameDocument
                                )
                                titleInput = renameResult.updatedTitleInput
                                document = renameResult.document
                                renameResult.updatedPath?.let { activeFilePath = it.toString() }
                                isPreviewMode = !isPreviewMode
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        val isBack = rotation.value > 90f
                        Icon(
                            imageVector = if (!isBack) Icons.Default.Visibility else Icons.Default.EditNote,
                            contentDescription = if (isBack) "Edit" else "Preview"
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Palette Action
                    IconButton(
                        onClick = { showColorSheet = true },
                        shape = MaterialTheme.shapes.medium,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Color")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Overflow menu remains available in both edit and preview modes.
                    var showOverflowMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            shape = MaterialTheme.shapes.medium,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(Res.string.more_options)
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Archive") },
                                leadingIcon = { Icon(Icons.Default.Archive, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.setArchived(activeFilePath, true)
                                    onNavigateBack()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Undo") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Undo, null) },
                                onClick = {
                                    if (undoStack.isNotEmpty()) {
                                        redoStack.add(content)
                                        content = undoStack.removeAt(undoStack.lastIndex)
                                    }
                                    showOverflowMenu = false
                                },
                                enabled = undoStack.isNotEmpty()
                            )
                            DropdownMenuItem(
                                text = { Text("Redo") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Redo, null) },
                                onClick = {
                                    if (redoStack.isNotEmpty()) {
                                        undoStack.add(content)
                                        content = redoStack.removeAt(redoStack.lastIndex)
                                    }
                                    showOverflowMenu = false
                                },
                                enabled = redoStack.isNotEmpty()
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Find & Replace") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                onClick = {
                                    showSearchDialog = true
                                    showOverflowMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.document_outline)) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                                onClick = {
                                    showOutlinePanel = true
                                    showOverflowMenu = false
                                },
                                enabled = outlineItems.isNotEmpty()
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.document_info)) },
                                leadingIcon = { Icon(Icons.Default.Info, null) },
                                onClick = {
                                    showDocumentInfoDialog = true
                                    showOverflowMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.export)) },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                onClick = {
                                    showExportDialog = true
                                    showOverflowMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isFocusMode) "Exit Focus Mode" else "Focus Mode") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CenterFocusStrong,
                                        null
                                    )
                                },
                                onClick = {
                                    isFocusMode = !isFocusMode
                                    showOverflowMenu = false
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (!isPreviewMode) {
                FormatToolbar { action ->
                    // Capture state before change for Undo
                    if (action != EditorAction.UNDO && action != EditorAction.REDO && action != EditorAction.SEARCH) {
                        pushToUndo(content)
                    }

                    when (action) {
                        EditorAction.BOLD -> content = wrapSelection(content, "**", "**")
                        EditorAction.ITALIC -> content = wrapSelection(content, "_", "_")
                        EditorAction.STRIKETHROUGH -> content = wrapSelection(content, "~~", "~~")
                        EditorAction.CODE -> content = wrapSelection(content, "`", "`")
                        EditorAction.LINK -> content = insertAtCursor(content, "[", "](url)")
                        EditorAction.IMAGE -> launchImagePicker()
                        EditorAction.HEADER -> content = insertAtStartOfLine(content, "# ")
                        EditorAction.LIST_BULLET -> content = insertAtStartOfLine(content, "- ")
                        EditorAction.LIST_NUMBERED -> content = insertAtStartOfLine(content, "1. ")
                        EditorAction.LIST_TASK -> content = insertAtStartOfLine(content, "- [ ] ")
                        EditorAction.QUOTE -> content = insertAtStartOfLine(content, "> ")
                        EditorAction.HORIZONTAL_RULE -> content =
                            insertAtCursor(content, "\n---\n", "")

                        EditorAction.UNDO -> { /* Handled in top bar now */
                        }

                        EditorAction.REDO -> { /* Handled in top bar now */
                        }

                        EditorAction.SEARCH -> { /* Handled in top bar now */
                        }
                    }
                    if (action != EditorAction.SEARCH && action != EditorAction.UNDO && action != EditorAction.REDO) {
                        hasUnsavedChanges = true
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Flip Animation
            val isBack = rotation.value > 90f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation.value
                        cameraDistance = 12f * density
                        if (isBack) {
                            rotationY = rotation.value - 180f // Correct visual for back side
                        }
                    }
            ) {
                if (isBack) {
                    // Preview (Clean Markdown)
                    // Because we flipped the container 180, the content inside is reversed if we don't handle it?
                    // Wait, we subtracted 180 when > 90.
                    // 0->90: Front visible.
                    // 90->180: Back visible. Back is rotated 180. We render Back rotated -180 rel to parent?
                    // Logic:
                    // 0: RotY=0.
                    // 180: RotY=180.
                    // If we are at 180, the content is mirrored.
                    // So when > 90, we should render content FLIPPED relative to the container, OR flip the container logic properly.
                    // Common logic:
                    // Modifier.graphicsLayer { rotationY = degrees }
                    // Inside:
                    // if (degrees <= 90) Front() else { Modifier.graphicsLayer { rotationY = 180f } Back() }
                    // Let's rely on the conditional 'isBack' logic above:
                    // IF isBack: rotationY = rotation.value - 180f (e.g., 180 - 180 = 0).
                    // So at 180 (target), rotationY is 0. So Back is upright.
                    // At 91: 91 - 180 = -89.
                    // Front at 89 vs Back at -89. That connects seamlessly.

                    PreviewTab(
                        filePath = activeFilePath,
                        title = titleInput,
                        content = content.text,
                        backgroundColor = editorContentSurfaceColor,
                        noteAccentColor = noteColor?.let(::Color),
                        onTapToEdit = {
                            editorFocusNonce++
                            isPreviewMode = false
                        }
                    )
                } else {
                    // Classic Editor
                    EditorTab(
                        filePath = activeFilePath,
                        title = titleInput,
                        content = content,
                        showLineNumbers = showLineNumbers,
                        editorFontSize = editorFontSize,
                        wordWrap = wordWrap,
                        surfaceColor = editorContentSurfaceColor,
                        noteAccentColor = noteColor?.let(::Color),
                        focusRequestNonce = editorFocusNonce,
                        autoFocusOnStart = openKeyboardOnStart &&
                                !initialAutoFocusConsumed &&
                                !isLoading &&
                                (document?.content?.isBlank() == true),
                        onAutoFocusConsumed = { initialAutoFocusConsumed = true },
                        onTitleChange = { titleInput = it },
                        onTitleCommit = {
                            scope.launch {
                                val renameResult = commitTitleRenameIfNeeded(
                                    titleInput = titleInput,
                                    document = document,
                                    renameDocument = viewModel::renameDocument
                                )
                                titleInput = renameResult.updatedTitleInput
                                document = renameResult.document
                                renameResult.updatedPath?.let { activeFilePath = it.toString() }
                            }
                        },
                        onContentChange = { newContent ->
                            // Smart Undo: Debounce logic to avoid cluttering history with every character
                            // We push to undo stack only if:
                            // 1. User pauses for 2 seconds (typing session ended)
                            // 2. Significant change (Paste/Cut > 10 chars)
                            // 3. Document save triggers (handled in autosave, but we track edit time here)

                            val isSignificantChange =
                                kotlin.math.abs(newContent.text.length - content.text.length) > 10

                            if (isSignificantChange) {
                                pushToUndo(content) // Save previous state immediately
                            } else {
                                // For normal typing, we rely on a debounce job to save state AFTER user stops typing
                                // Cancel previous job -> user is still typing
                                undoDebounceJob?.cancel()
                                undoDebounceJob = scope.launch {
                                    delay(2000) // 2 second pause = "commit"
                                    pushToUndo(newContent)
                                }
                            }

                            // If this is the FIRST character after a save/undo, we might want to ensure we have a base state?
                            // pushToUndo checks for duplicates so it's safe.

                            content = newContent
                            hasUnsavedChanges = true
                        }
                    )
                }
            }
        }

        // Slash Command Menu (appears above keyboard when typing /)
        com.bernaferrari.remarkor.ui.components.SlashCommandMenu(
            visible = showSlashMenu && !isPreviewMode,
            query = slashQuery,
            onSelect = { command ->
                // Apply the slash command to the content
                val newContent = com.bernaferrari.remarkor.ui.components.applySlashCommand(
                    content,
                    command,
                    slashStartIndex
                )
                pushToUndo(content)
                content = newContent
                hasUnsavedChanges = true
            },
            onDismiss = { /* Menu dismisses when user types something else */ }
        )

        // Export Dialog
        if (showExportDialog && document != null) {
            val doc = document!!
            com.bernaferrari.remarkor.ui.components.ExportDialog(
                filePath = activeFilePath,
                fileName = doc.name,
                markdownContent = content.text,
                onDismiss = { showExportDialog = false },
                onShareHtml = { showExportDialog = false },
                onPrint = { showExportDialog = false },
                onShareMarkdown = {
                    showExportDialog = false
                    val doc = document!!
                    shareService.shareFile(
                        fileName = doc.name,
                        content = content.text.encodeToByteArray(),
                        title = "Share Markdown",
                        mimeType = "text/markdown"
                    )
                },
            )
        }

        // Outline Panel (modal bottom sheet)
        if (showOutlinePanel) {
            com.bernaferrari.remarkor.ui.components.OutlinePanel(
                items = outlineItems,
                currentCharOffset = content.selection.start,
                onItemClick = { item ->
                    // Navigate to the heading
                    content = content.copy(
                        selection = androidx.compose.ui.text.TextRange(item.charOffset)
                    )
                },
                onDismiss = { showOutlinePanel = false }
            )
        }

        if (showDocumentInfoDialog) {
            AlertDialog(
                onDismissRequest = { showDocumentInfoDialog = false },
                title = { Text(stringResource(Res.string.document_info)) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            formatIntStringResource(
                                stringResource(Res.string.headings_with_arg),
                                documentInfoStats.headings
                            )
                        )
                        Text(
                            formatIntStringResource(
                                stringResource(Res.string.lines_with_arg),
                                documentInfoStats.lines
                            )
                        )
                        Text(
                            formatIntStringResource(
                                stringResource(Res.string.words_with_arg),
                                documentInfoStats.words
                            )
                        )
                        Text(
                            formatIntStringResource(
                                stringResource(Res.string.characters_with_arg),
                                documentInfoStats.characters
                            )
                        )
                        Text(
                            formatIntStringResource(
                                stringResource(Res.string.characters_no_spaces_with_arg),
                                documentInfoStats.charactersNoSpaces
                            )
                        )
                        Text(
                            stringResource(
                                Res.string.size_utf8_with_arg,
                                formatStorageSize(documentInfoStats.bytes)
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDocumentInfoDialog = false }) {
                        Text(stringResource(Res.string.close))
                    }
                }
            )
        }

        if (showColorSheet) {
            com.bernaferrari.remarkor.ui.components.ColorSelectionSheet(
                currentColor = noteColor,
                onColorSelected = { color ->
                    viewModel.setColor(activeFilePath, color)
                    showColorSheet = false
                },
                onDismiss = { showColorSheet = false }
            )
        }
    }
}
