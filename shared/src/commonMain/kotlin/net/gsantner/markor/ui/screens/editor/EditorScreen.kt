package net.gsantner.markor.ui.screens.editor

import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.gsantner.markor.domain.model.Document
import net.gsantner.markor.ui.components.EditorAction
import net.gsantner.markor.ui.components.FormatToolbar
import net.gsantner.markor.ui.components.MarkdownVisualTransformation
import net.gsantner.markor.ui.components.resolveNoteSurfaceColor
import net.gsantner.markor.ui.components.resolveMarkdownColorPalette
import net.gsantner.markor.ui.viewmodel.EditorViewModel
import net.gsantner.markor.ui.components.SharedElementContainer
import net.gsantner.markor.ui.components.SharedTransitionKeys
import net.gsantner.markor.util.resolveImageUrl
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import net.gsantner.markor.ui.theme.MarkorTheme
import net.gsantner.markor.domain.service.ImageAssetManager
import net.gsantner.markor.domain.service.PickedImage
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import okio.Path
import okio.Path.Companion.toPath

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    filePath: String,
    onNavigateBack: () -> Unit,
    openKeyboardOnStart: Boolean = false,
    viewModel: EditorViewModel = koinViewModel(),
    shareService: net.gsantner.markor.domain.service.ShareService = org.koin.compose.koinInject()
) {
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
        titleInput = loadedDocument?.title?.ifBlank { loadedDocument.name.substringBeforeLast(".") } ?: ""
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
    
    val outlineItems = remember(content.text) {
        net.gsantner.markor.ui.components.parseOutline(content.text)
    }
    
    // NEW: Color Selection State
    var showColorSheet by remember { mutableStateOf(false) }
    val noteColor by viewModel.noteColor.collectAsState()
    
    // Image picker and asset manager
    val assetManager: ImageAssetManager = org.koin.compose.koinInject()
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
        net.gsantner.markor.ui.components.getCurrentParagraphIndex(content.text, content.selection.start)
    }
    
    // NEW: Slash Command State
    val slashCommandState = remember(content.text, content.selection) {
        net.gsantner.markor.ui.components.detectSlashCommand(content)
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
        net.gsantner.markor.ui.components.AdvancedSearchReplaceDialog(
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
                    content = content.copy(selection = androidx.compose.ui.text.TextRange(index, index + query.length))
                }
            },
            onReplace = { query, replacement ->
                val text = content.text
                val selection = content.selection
                // Check if current selection matches query (simple verify before replace)
                val selectedText = if (selection.min != selection.max) text.substring(selection.min, selection.max) else ""
                
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
                         content = content.copy(selection = androidx.compose.ui.text.TextRange(index, index + query.length))
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
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
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
                        showSearchDialog -> { showSearchDialog = false; true }
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
                }
                else false
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
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.more_options))
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
                        EditorAction.HORIZONTAL_RULE -> content = insertAtCursor(content, "\n---\n", "")
                        EditorAction.UNDO -> { /* Handled in top bar now */ }
                        EditorAction.REDO -> { /* Handled in top bar now */ }
                        EditorAction.SEARCH -> { /* Handled in top bar now */ }
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
                       surfaceColor = editorContentSurfaceColor,
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
                           
                           val isSignificantChange = kotlin.math.abs(newContent.text.length - content.text.length) > 10
                           
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
        net.gsantner.markor.ui.components.SlashCommandMenu(
            visible = showSlashMenu && !isPreviewMode,
            query = slashQuery,
            onSelect = { command ->
                // Apply the slash command to the content
                val newContent = net.gsantner.markor.ui.components.applySlashCommand(
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
        net.gsantner.markor.ui.components.ExportDialog(
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
        net.gsantner.markor.ui.components.OutlinePanel(
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

    if (showColorSheet) {
        net.gsantner.markor.ui.components.ColorSelectionSheet(
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

private data class TitleRenameResult(
    val document: Document?,
    val updatedTitleInput: String,
    val updatedPath: Path? = null
)

private suspend fun commitTitleRenameIfNeeded(
    titleInput: String,
    document: Document?,
    renameDocument: suspend (Document, String) -> Path?
): TitleRenameResult {
    if (document == null) {
        return TitleRenameResult(document = null, updatedTitleInput = titleInput)
    }

    val trimmedTitle = titleInput.trim()
    if (trimmedTitle.isBlank()) {
        return TitleRenameResult(
            document = document.copy(title = ""),
            updatedTitleInput = ""
        )
    }

    val targetName = buildTargetName(trimmedTitle, document.name)
    if (targetName == document.name) {
        return TitleRenameResult(document = document.copy(title = trimmedTitle), updatedTitleInput = trimmedTitle)
    }

    val renamedPath = renameDocument(document, targetName)
    if (renamedPath == null) {
        return TitleRenameResult(document = document, updatedTitleInput = trimmedTitle)
    }
    val updatedTitle = renamedPath.name.substringBeforeLast(".")

    return TitleRenameResult(
        document = document.copy(path = renamedPath, title = updatedTitle),
        updatedTitleInput = updatedTitle,
        updatedPath = renamedPath
    )
}

private fun buildTargetName(title: String, currentFileName: String): String {
    val sanitizedTitle = title
        .replace("/", " ")
        .replace("\\", " ")
        .trim()
        .ifBlank { currentFileName.substringBeforeLast(".") }

    val currentExt = currentFileName.substringAfterLast(".", "")
    if (currentExt.isBlank()) return sanitizedTitle
    if (sanitizedTitle.endsWith(".$currentExt", ignoreCase = true)) return sanitizedTitle
    return "$sanitizedTitle.$currentExt"
}

@Composable
private fun EditorTab(
    filePath: String,
    title: String,
    content: TextFieldValue,
    surfaceColor: Color,
    focusRequestNonce: Int = 0,
    autoFocusOnStart: Boolean = false,
    onAutoFocusConsumed: () -> Unit = {},
    onTitleChange: (String) -> Unit,
    onTitleCommit: () -> Unit,
    onContentChange: (TextFieldValue) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val markdownPalette = remember(colorScheme, surfaceColor) {
        resolveMarkdownColorPalette(colorScheme, surfaceColor)
    }
    val editorScrollState = rememberScrollState()
    val focusRequester = remember(filePath) { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var didAutoFocus by remember(filePath) { mutableStateOf(false) }

    LaunchedEffect(autoFocusOnStart, didAutoFocus) {
        if (autoFocusOnStart && !didAutoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
            didAutoFocus = true
            onAutoFocusConsumed()
        }
    }

    LaunchedEffect(focusRequestNonce) {
        if (focusRequestNonce > 0) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    SharedElementContainer(
        key = SharedTransitionKeys.fileCard(filePath),
        isSource = false,
        useSharedBounds = true
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MarkorTheme.spacing.large)
                .background(
                    surfaceColor,
                    MaterialTheme.shapes.large
                )
                .padding(horizontal = MarkorTheme.spacing.small)
        ) {
            val bodyMinHeight = (maxHeight - 88.dp).coerceAtLeast(220.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(editorScrollState)
            ) {
            SharedElementContainer(
                key = SharedTransitionKeys.fileTitle(filePath),
                isSource = false
            ) {
                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onTitleCommit()
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown &&
                                (event.key == Key.Enter || event.key == Key.NumPadEnter)
                            ) {
                                onTitleCommit()
                                focusRequester.requestFocus()
                                keyboardController?.show()
                                true
                            } else {
                                false
                            }
                        }
                )
            }
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 28.sp,
                    color = markdownPalette.body,
                    letterSpacing = 0.sp
                ),
                cursorBrush = SolidColor(markdownPalette.accent),
                visualTransformation = let {
                    remember(colorScheme, surfaceColor) {
                        MarkdownVisualTransformation(
                            colorScheme = colorScheme,
                            backgroundColor = surfaceColor
                        )
                    }
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .heightIn(min = bodyMinHeight)
                    .padding(MarkorTheme.spacing.large)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                            if (handleSmartEnter(content, onContentChange)) {
                                return@onPreviewKeyEvent true
                            }
                        }
                        false
                    }
            )
            }
        }
    }
}

private fun handleSmartEnter(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
): Boolean {
    val text = value.text
    val selection = value.selection
    if (!selection.collapsed) return false

    // Find the bounds of the current line
    var lineStart = selection.start
    while (lineStart > 0 && text[lineStart - 1] != '\n') {
        lineStart--
    }
    val currentLine = text.substring(lineStart, selection.start)

    // Regex patterns for lists
    val taskRegex = """^(\s*)([-*+])\s*\[([ xX]?)\]\s*(.*)$""".toRegex()
    val bulletRegex = """^(\s*)([-*+])\s+(.*)$""".toRegex()
    val numberedRegex = """^(\s*)(\d+)\.\s+(.*)$""".toRegex()

    val taskMatch = taskRegex.find(currentLine)
    val bulletMatch = bulletRegex.find(currentLine)
    val numberedMatch = numberedRegex.find(currentLine)

    return when {
        taskMatch != null -> {
            val whitespace = taskMatch.groupValues[1]
            val marker = taskMatch.groupValues[2]
            val content = taskMatch.groupValues[4]
            if (content.isEmpty()) {
                // Exit list: Clear the marker and insert newline
                val newText = text.removeRange(lineStart, selection.start) + "\n"
                onValueChange(value.copy(text = newText, selection = TextRange(lineStart + 1)))
            } else {
                // Continue list
                val insert = "\n$whitespace$marker [ ] "
                val newText = text.replaceRange(selection.start, selection.start, insert)
                onValueChange(value.copy(text = newText, selection = TextRange(selection.start + insert.length)))
            }
            true
        }
        bulletMatch != null -> {
            val whitespace = bulletMatch.groupValues[1]
            val marker = bulletMatch.groupValues[2]
            val content = bulletMatch.groupValues[3]
            if (content.isEmpty()) {
                val newText = text.removeRange(lineStart, selection.start) + "\n"
                onValueChange(value.copy(text = newText, selection = TextRange(lineStart + 1)))
            } else {
                val insert = "\n$whitespace$marker "
                val newText = text.replaceRange(selection.start, selection.start, insert)
                onValueChange(value.copy(text = newText, selection = TextRange(selection.start + insert.length)))
            }
            true
        }
        numberedMatch != null -> {
            val whitespace = numberedMatch.groupValues[1]
            val number = numberedMatch.groupValues[2].toInt()
            val content = numberedMatch.groupValues[3]
            if (content.isEmpty()) {
                val newText = text.removeRange(lineStart, selection.start) + "\n"
                onValueChange(value.copy(text = newText, selection = TextRange(lineStart + 1)))
            } else {
                val insert = "\n$whitespace${number + 1}. "
                val newText = text.replaceRange(selection.start, selection.start, insert)
                onValueChange(value.copy(text = newText, selection = TextRange(selection.start + insert.length)))
            }
            true
        }
        else -> false
    }
}

@Composable
private fun PreviewTab(
    filePath: String,
    title: String,
    content: String,
    backgroundColor: Color,
    onTapToEdit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val markdownPalette = remember(colorScheme, backgroundColor) {
        resolveMarkdownColorPalette(colorScheme, backgroundColor)
    }
    val previewBlocks = remember(content, filePath) {
        buildPreviewBlocks(content, filePath)
    }
    val emptyMessage = stringResource(Res.string.nothing_to_preview)
    val styledText = remember<AnnotatedString>(content, colorScheme, backgroundColor) {
        net.gsantner.markor.ui.components.renderCleanMarkdown(
            if (content.isEmpty()) emptyMessage else content,
            colorScheme,
            backgroundColor
        )
    }

    SharedElementContainer(
        key = SharedTransitionKeys.fileCard(filePath),
        isSource = false,
        useSharedBounds = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MarkorTheme.spacing.medium)
                .background(backgroundColor, MaterialTheme.shapes.extraLarge)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onTapToEdit() })
                }
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (title.isNotBlank()) {
                SharedElementContainer(
                    key = SharedTransitionKeys.fileTitle(filePath),
                    isSource = false
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = markdownPalette.body
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (previewBlocks.none { it is PreviewBlock.Image }) {
                Text(
                    text = styledText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 32.sp,
                        color = if (content.isEmpty()) markdownPalette.subtle else markdownPalette.body
                    )
                )
            } else {
                val context = LocalPlatformContext.current
                previewBlocks.forEach { block ->
                    when (block) {
                        is PreviewBlock.Text -> {
                            if (block.content.isBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                val lineText = remember(block.content, colorScheme, backgroundColor) {
                                    net.gsantner.markor.ui.components.renderCleanMarkdown(
                                        block.content,
                                        colorScheme,
                                        backgroundColor
                                    )
                                }
                                Text(
                                    text = lineText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 30.sp,
                                        color = markdownPalette.body
                                    )
                                )
                            }
                        }
                        is PreviewBlock.Image -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(block.source)
                                    .build(),
                                contentDescription = block.altText,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 320.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

private sealed interface PreviewBlock {
    data class Text(val content: String) : PreviewBlock
    data class Image(val source: String, val altText: String?) : PreviewBlock
}

private val markdownImageRegex = Regex("!\\[([^\\]]*)\\]\\(([^)]+)\\)")

private fun buildPreviewBlocks(content: String, filePath: String): List<PreviewBlock> {
    if (content.isEmpty()) return emptyList()

    val blocks = mutableListOf<PreviewBlock>()
    content.lineSequence().forEach { line ->
        val matches = markdownImageRegex.findAll(line).toList()
        if (matches.isEmpty()) {
            blocks.add(PreviewBlock.Text(line))
            return@forEach
        }

        var cursor = 0
        matches.forEach { match ->
            val before = line.substring(cursor, match.range.first)
            if (before.isNotEmpty()) {
                blocks.add(PreviewBlock.Text(before))
            }

            val altText = match.groupValues[1].ifBlank { null }
            val rawPath = match.groupValues[2].trim()
            val resolvedPath = resolvePreviewImageSource(rawPath, filePath)
            if (!resolvedPath.isNullOrEmpty()) {
                blocks.add(PreviewBlock.Image(source = resolvedPath, altText = altText))
            }
            cursor = match.range.last + 1
        }

        val after = line.substring(cursor)
        if (after.isNotEmpty()) {
            blocks.add(PreviewBlock.Text(after))
        }
    }
    return blocks
}

private fun resolvePreviewImageSource(imagePath: String, filePath: String): String? {
    val source = imagePath.trim().removeSurrounding("<", ">").takeIf { it.isNotEmpty() } ?: return null
    if (
        source.startsWith("http://") ||
        source.startsWith("https://") ||
        source.startsWith("file://") ||
        source.startsWith("content://") ||
        source.startsWith("/")
    ) {
        return source
    }

    return runCatching {
        resolveImageUrl(source, filePath.toPath())
    }.getOrNull() ?: source
}

// Helper functions for text manipulation
private fun wrapSelection(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
    val before = value.text.substring(0, value.selection.start)
    val selected = value.text.substring(value.selection.start, value.selection.end)
    val after = value.text.substring(value.selection.end)
    
    val newText = before + prefix + selected + suffix + after
    val newCursor = value.selection.start + prefix.length + selected.length 
    
    return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newCursor))
}

private fun insertAtCursor(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
     val before = value.text.substring(0, value.selection.start)
     val after = value.text.substring(value.selection.end)
     val newText = before + prefix + suffix + after
     val newCursor = value.selection.start + prefix.length
     return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newCursor))
}

private fun insertAtStartOfLine(value: TextFieldValue, textToInsert: String): TextFieldValue {
    val text = value.text
    var lineStart = value.selection.start
    if (lineStart > text.length) lineStart = text.length
    while (lineStart > 0 && text[lineStart - 1] != '\n') {
        lineStart--
    }
    
    val before = text.substring(0, lineStart)
    val after = text.substring(lineStart)
    
    val newText = before + textToInsert + after
    val newCursor = value.selection.start + textToInsert.length
    return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newCursor))
}
