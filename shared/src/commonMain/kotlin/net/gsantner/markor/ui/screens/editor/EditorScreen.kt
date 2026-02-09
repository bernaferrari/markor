package net.gsantner.markor.ui.screens.editor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import net.gsantner.markor.ui.components.markdownToAnnotatedString
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.gsantner.markor.domain.model.Document
import net.gsantner.markor.ui.components.EditorAction
import net.gsantner.markor.ui.components.FormatToolbar
import net.gsantner.markor.ui.components.MarkdownVisualTransformation
import net.gsantner.markor.ui.viewmodel.EditorViewModel
import net.gsantner.markor.domain.model.BlockDocument
import net.gsantner.markor.domain.parser.BlockParser
import net.gsantner.markor.ui.components.BlockEditor
import net.gsantner.markor.ui.components.SharedElementContainer
import net.gsantner.markor.ui.components.SharedTransitionKeys
import org.koin.compose.viewmodel.koinViewModel

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import net.gsantner.markor.ui.components.RenameDialog
import net.gsantner.markor.ui.theme.MarkorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    filePath: String,
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = koinViewModel(),
    shareService: net.gsantner.markor.domain.service.ShareService = org.koin.compose.koinInject()
) {
    val scope = rememberCoroutineScope()
    var isPreviewMode by remember { mutableStateOf(false) }
    var document by remember { mutableStateOf<Document?>(null) }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(true) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    
    // Rename State
    var showRenameDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Handle System Back Press
    BackHandler(enabled = true) {
        if (hasUnsavedChanges && document != null) {
            scope.launch {
                viewModel.saveDocument(document!!, content.text)
                onNavigateBack()
            }
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(filePath) {
        isLoading = true
        document = viewModel.loadDocument(filePath)
        val text = document?.content ?: ""
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
    
    // NEW: Block Editor State
    var isBlockMode by remember { mutableStateOf(false) }
    var blockDocument by remember { mutableStateOf(BlockDocument()) }
    
    // Sync content when switching modes
    LaunchedEffect(isBlockMode) {
        if (isBlockMode) {
            // Text -> Block
            blockDocument = BlockParser.parse(content.text)
        } else if (blockDocument.blocks.isNotEmpty()) {
            // Block -> Text (only if we have blocks)
            // Ideally we'd do this on every change, but doing it on switch is cleaner for now
            // But we need to make sure we don't lose edits if we switch back
            val newText = BlockParser.toMarkdown(blockDocument)
            if (newText != content.text) {
                content = TextFieldValue(newText)
                hasUnsavedChanges = true
            }
        }
    }
    
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
    var currentColor by remember { mutableStateOf<Int?>(null) }
    
    // Load metadata (color/archive default) - this is tricky because we load doc from repo which wraps file.
    // Metadata needs to be loaded separately or we just set it blindly. 
    // Ideally ViewModel exposes "currentNoteMetadata" flow.
    // For now we just implement the SETTERS. Getting the current color requires viewing the metadata.
    // Let's assume we start with null and if the user sets it, it updates.
    // BETTER: Observe metadata in VM. But for this iteration, let's just make it work.
    
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
    
    // NEW: Floating Selection Toolbar State
    val hasSelection = content.selection.start != content.selection.end

    // Helper to push to undo stack
    fun pushToUndo(value: TextFieldValue) {
        if (undoStack.isEmpty() || undoStack.last().text != value.text) {
            undoStack.add(value)
            redoStack.clear()
            if (undoStack.size > 50) undoStack.removeAt(0) // Limit stack size
        }
    }
    
    if (showRenameDialog && document != null) {
        RenameDialog(
            currentName = document!!.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                scope.launch {
                    val success = viewModel.renameDocument(document!!, newName)
                    if (success) {
                       // Reload document or update local state
                       // Ideally navigation should act up or we reload.
                       // For now, let's just reload.
                       document = viewModel.loadDocument(document!!.path.parent!!.div(newName).toString())
                    }
                    showRenameDialog = false
                }
            }
        )
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

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .onPreviewKeyEvent { event ->
                // Escape to close dialogs
                if (event.key == Key.Escape && event.type == KeyEventType.KeyDown) {
                    when {
                        showSearchDialog -> { showSearchDialog = false; true }
                        showRenameDialog -> { showRenameDialog = false; true }
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    val titleText = document?.name ?: filePath.substringAfterLast("/")
                     // Title handling logic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showRenameDialog = true }
                            .padding(4.dp)
                    ) {
                        SharedElementContainer(
                            key = SharedTransitionKeys.fileTitle(filePath),
                            isSource = false
                        ) {
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        if (hasUnsavedChanges) {
                             Text(
                                text = "Modified (Auto-saving...)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            scope.launch {
                                viewModel.saveDocument(document!!, content.text)
                                onNavigateBack()
                            }
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // View Mode Toggle
                    IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                         val isBack = rotation.value > 90f
                         Icon(
                            imageVector = if (!isBack) Icons.Default.Visibility else Icons.Default.EditNote,
                            contentDescription = if (isBack) "Edit" else "Preview",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Palette Action
                    IconButton(onClick = { showColorSheet = true }) {
                        Icon(Icons.Default.Palette, "Color")
                    }
                    
                    // Overflow menu for undo/redo/search (only in edit mode)
                    if(!isPreviewMode) {
                        var showOverflowMenu by remember { mutableStateOf(false) }
                        
                        Box {
                            IconButton(onClick = { showOverflowMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
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
                                        viewModel.setArchived(filePath, true)
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
                                    text = { Text("Document Outline") },
                                    leadingIcon = { Icon(Icons.Filled.List, null) },
                                    onClick = {
                                        showOutlinePanel = true
                                        showOverflowMenu = false
                                    },
                                    enabled = outlineItems.isNotEmpty()
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Export") },
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
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(if (isBlockMode) "Switch to Classic" else "Switch to Blocks") },
                                    leadingIcon = { Icon(if (isBlockMode) Icons.Default.Edit else Icons.Default.ViewAgenda, null) },
                                    onClick = {
                                        if (isBlockMode) {
                                            // Convert back to text before switching
                                            val newText = BlockParser.toMarkdown(blockDocument)
                                            content = TextFieldValue(newText)
                                            hasUnsavedChanges = true
                                        } else {
                                            // Blocks will be parsed in LaunchedEffect
                                        }
                                        isBlockMode = !isBlockMode
                                        showOverflowMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
            )
        },
        bottomBar = {
           if (!isPreviewMode && !isBlockMode) {
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
                     
                   PreviewTab(content = content.text)
               } else {
                   // Editor Mode
                   if (isBlockMode) {
                       BlockEditor(
                           document = blockDocument,
                           onDocumentChange = { newDoc ->
                               blockDocument = newDoc
                               hasUnsavedChanges = true
                               // Optional: Sync back to text immediately for autosave?
                               // For performance, maybe just on save
                           },
                           isFocusMode = isFocusMode
                       )
                   } else {
                       // Classic Editor
                       EditorTab(
                           content = content,
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
        }
        
        // Slash Command Menu (appears above keyboard when typing /)
        if (!isBlockMode) {
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
        }
        
        // Floating Selection Toolbar (appears above text selection)
        if (!isBlockMode) {
            net.gsantner.markor.ui.components.FloatingSelectionToolbar(
                visible = hasSelection && !isPreviewMode && !showSlashMenu,
                onBold = {
                    pushToUndo(content)
                    content = wrapSelection(content, "**", "**")
                    hasUnsavedChanges = true
                },
                onItalic = {
                    pushToUndo(content)
                    content = wrapSelection(content, "_", "_")
                    hasUnsavedChanges = true
                },
                onCode = {
                    pushToUndo(content)
                    content = wrapSelection(content, "`", "`")
                    hasUnsavedChanges = true
                },
                onLink = {
                    pushToUndo(content)
                    content = insertAtCursor(content, "[", "](url)")
                    hasUnsavedChanges = true
                },
            onDismiss = { /* Handled by selection change */ }
        )
    }
    
    // Export Dialog
    if (showExportDialog && document != null) {
        val doc = document!!
        net.gsantner.markor.ui.components.ExportDialog(
            filePath = filePath,
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
                    content = content.text.toByteArray(),
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
            currentColor = currentColor,
            onColorSelected = { color ->
                currentColor = color
                viewModel.setColor(filePath, color)
                showColorSheet = false
            },
            onDismiss = { showColorSheet = false }
        )
    }
}
}

@Composable
private fun EditorTab(
    content: TextFieldValue,
    onContentChange: (TextFieldValue) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(MarkorTheme.spacing.medium)
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.extraLarge
            )
            .padding(MarkorTheme.spacing.small)
    ) {
        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = let {
                val colorScheme = MaterialTheme.colorScheme
                remember(colorScheme) {
                    MarkdownVisualTransformation(colorScheme)
                }
            },
            modifier = Modifier
                .fillMaxSize()
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
private fun PreviewTab(content: String) {
    val colorScheme = MaterialTheme.colorScheme
    val styledText = remember<AnnotatedString>(content, colorScheme) {
        net.gsantner.markor.ui.components.renderCleanMarkdown(if (content.isEmpty()) "Nothing to preview yet..." else content, colorScheme)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = styledText,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 32.sp,
                color = if (content.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        )
    }
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
