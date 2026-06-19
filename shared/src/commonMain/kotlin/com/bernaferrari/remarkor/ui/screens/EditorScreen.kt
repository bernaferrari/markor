package com.bernaferrari.remarkor.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.TextFieldValue
import com.bernaferrari.remarkor.domain.model.Document
import com.bernaferrari.remarkor.domain.repository.IAssetRepository
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import com.bernaferrari.remarkor.domain.service.PickedImage
import com.bernaferrari.remarkor.domain.service.ShareService
import com.bernaferrari.remarkor.ui.components.EditorAction
import com.bernaferrari.remarkor.ui.components.FormatToolbar
import com.bernaferrari.remarkor.ui.components.detectSlashCommand
import com.bernaferrari.remarkor.ui.components.parseOutline
import com.bernaferrari.remarkor.ui.components.resolveNoteSurfaceColor
import com.bernaferrari.remarkor.ui.screens.editor.EditorFlipContent
import com.bernaferrari.remarkor.ui.screens.editor.EditorKeyboardShortcutHandlers
import com.bernaferrari.remarkor.ui.screens.editor.EditorOverlays
import com.bernaferrari.remarkor.ui.screens.editor.EditorTopBar
import com.bernaferrari.remarkor.ui.screens.editor.buildDocumentInfoStats
import com.bernaferrari.remarkor.ui.screens.editor.commitEditorSession
import com.bernaferrari.remarkor.ui.screens.editor.editorKeyboardShortcuts
import com.bernaferrari.remarkor.ui.screens.editor.insertAtCursor
import com.bernaferrari.remarkor.ui.screens.editor.insertAtStartOfLine
import com.bernaferrari.remarkor.ui.screens.editor.rememberEditorUndoRedo
import com.bernaferrari.remarkor.ui.screens.editor.wrapSelection
import com.bernaferrari.remarkor.ui.viewmodel.EditorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Platform-specific back handler.
 * On Android, uses BackHandler. On iOS, does nothing (no hardware back button).
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)

/**
 * Platform-specific image picker.
 * Returns null if cancelled or not supported.
 */
@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit,
): () -> Unit

@Composable
fun EditorScreen(
    filePath: String,
    onNavigateBack: () -> Unit,
    openKeyboardOnStart: Boolean = false,
    viewModel: EditorViewModel = koinViewModel(),
    shareService: ShareService = koinInject(),
) {
    val settingsRepository: ISettingsRepository = koinInject()
    val showLineNumbers by settingsRepository.isShowLineNumbers.collectAsState(initial = false)
    val editorFontSize by settingsRepository.getEditorFontSize.collectAsState(initial = 16)
    val wordWrap by settingsRepository.isWordWrap.collectAsState(initial = true)

    val scope = rememberCoroutineScope()
    val undoRedo = rememberEditorUndoRedo()

    var isPreviewMode by remember { mutableStateOf(false) }
    var activeFilePath by remember(filePath) { mutableStateOf(filePath) }
    var document by remember { mutableStateOf<Document?>(null) }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var titleInput by remember { mutableStateOf("") }
    var initialAutoFocusConsumed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var editorFocusNonce by remember { mutableIntStateOf(0) }
    var isFocusMode by remember { mutableStateOf(false) }

    var showSearchDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showOutlinePanel by remember { mutableStateOf(false) }
    var showDocumentInfoDialog by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }

    val noteColor by viewModel.noteColor.collectAsState()
    val assetManager: IAssetRepository = koinInject()
    var pendingImageInsert by remember { mutableStateOf<PickedImage?>(null) }
    val launchImagePicker = rememberImagePickerLauncher { pendingImageInsert = it }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val outlineItems = remember(content.text) { parseOutline(content.text) }
    val documentInfoStats = remember(content.text, outlineItems.size) {
        buildDocumentInfoStats(content.text, outlineItems.size)
    }
    val slashCommandState = remember(content.text, content.selection) {
        detectSlashCommand(content)
    }

    suspend fun commitSession(saveIfNeeded: Boolean = hasUnsavedChanges) {
        val result = commitEditorSession(
            titleInput = titleInput,
            document = document,
            contentText = content.text,
            hasUnsavedChanges = saveIfNeeded,
            renameDocument = viewModel::renameDocument,
            saveDocument = { doc, text -> viewModel.saveDocument(doc, text) },
        )
        titleInput = result.titleInput
        document = result.document
        result.activeFilePath?.let { activeFilePath = it }
        if (saveIfNeeded) hasUnsavedChanges = false
    }

    PlatformBackHandler(enabled = true) {
        scope.launch {
            commitSession()
            onNavigateBack()
        }
    }

    LaunchedEffect(activeFilePath) {
        isLoading = true
        document = viewModel.loadDocument(activeFilePath)
        val loadedDocument = document
        titleInput = loadedDocument?.title?.ifBlank {
            loadedDocument.name.substringBeforeLast(".")
        } ?: ""
        content = TextFieldValue(loadedDocument?.content ?: "")
        isLoading = false
    }

    LaunchedEffect(content) {
        if (hasUnsavedChanges && document != null) {
            delay(2_000)
            viewModel.saveDocument(document!!, content.text)
            hasUnsavedChanges = false
        }
    }

    LaunchedEffect(pendingImageInsert) {
        val image = pendingImageInsert ?: return@LaunchedEffect
        pendingImageInsert = null
        val relativePath = assetManager.addImage(activeFilePath.toPath(), image.data, image.fileName)
        if (relativePath != null) {
            content = insertAtCursor(content, "![]($relativePath)", "")
            hasUnsavedChanges = true
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val editorBackgroundColor = colorScheme.background
    val editorContentSurfaceColor = remember(noteColor, colorScheme) {
        resolveNoteSurfaceColor(noteColor, colorScheme, fallback = colorScheme.surfaceContainerLow)
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .editorKeyboardShortcuts(
                EditorKeyboardShortcutHandlers(
                    showSearchDialog = showSearchDialog,
                    onDismissSearch = { showSearchDialog = false },
                    onShowSearch = { showSearchDialog = true },
                    document = document,
                    content = content,
                    onContentChange = { content = it },
                    onMarkUnsaved = { hasUnsavedChanges = true },
                    onSave = {
                        document?.let { doc ->
                            viewModel.saveDocument(doc, content.text)
                            hasUnsavedChanges = false
                        }
                    },
                    undoRedo = undoRedo,
                    scope = scope,
                ),
            ),
        containerColor = editorBackgroundColor,
        topBar = {
            EditorTopBar(
                scrollBehavior = scrollBehavior,
                isPreviewMode = isPreviewMode,
                canUndo = undoRedo.undoStack.isNotEmpty(),
                canRedo = undoRedo.redoStack.isNotEmpty(),
                hasOutline = outlineItems.isNotEmpty(),
                isFocusMode = isFocusMode,
                onBack = {
                    scope.launch {
                        commitSession()
                        onNavigateBack()
                    }
                },
                onTogglePreview = {
                    scope.launch {
                        commitSession(saveIfNeeded = false)
                        isPreviewMode = !isPreviewMode
                    }
                },
                onShowColorSheet = { showColorSheet = true },
                onArchive = {
                    viewModel.setArchived(activeFilePath, true)
                    onNavigateBack()
                },
                onUndo = {
                    undoRedo.undo(content)?.let {
                        content = it
                        hasUnsavedChanges = true
                    }
                },
                onRedo = {
                    undoRedo.redo(content)?.let {
                        content = it
                        hasUnsavedChanges = true
                    }
                },
                onFindReplace = { showSearchDialog = true },
                onShowOutline = { showOutlinePanel = true },
                onShowDocumentInfo = { showDocumentInfoDialog = true },
                onExport = { showExportDialog = true },
                onToggleFocusMode = { isFocusMode = !isFocusMode },
            )
        },
        bottomBar = {
            if (!isPreviewMode) {
                FormatToolbar { action ->
                    if (action != EditorAction.UNDO &&
                        action != EditorAction.REDO &&
                        action != EditorAction.SEARCH
                    ) {
                        undoRedo.pushImmediate(content)
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
                        EditorAction.UNDO, EditorAction.REDO, EditorAction.SEARCH -> Unit
                    }
                    if (action != EditorAction.SEARCH &&
                        action != EditorAction.UNDO &&
                        action != EditorAction.REDO
                    ) {
                        hasUnsavedChanges = true
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            EditorFlipContent(
                isPreviewMode = isPreviewMode,
                activeFilePath = activeFilePath,
                titleInput = titleInput,
                content = content,
                showLineNumbers = showLineNumbers,
                editorFontSize = editorFontSize,
                wordWrap = wordWrap,
                surfaceColor = editorContentSurfaceColor,
                noteAccentColor = noteColor?.let(::Color),
                isFocusMode = isFocusMode,
                editorFocusNonce = editorFocusNonce,
                autoFocusOnStart = openKeyboardOnStart &&
                    !initialAutoFocusConsumed &&
                    !isLoading &&
                    (document?.content?.isBlank() == true),
                onAutoFocusConsumed = { initialAutoFocusConsumed = true },
                onTitleChange = { titleInput = it },
                onTitleCommit = { scope.launch { commitSession(saveIfNeeded = false) } },
                onContentChange = { newContent ->
                    undoRedo.trackContentChange(content, newContent)
                    content = newContent
                    hasUnsavedChanges = true
                },
                onTapToEdit = {
                    editorFocusNonce++
                    isPreviewMode = false
                },
            )

            EditorOverlays(
                showSearchDialog = showSearchDialog,
                showExportDialog = showExportDialog,
                showOutlinePanel = showOutlinePanel,
                showDocumentInfoDialog = showDocumentInfoDialog,
                showColorSheet = showColorSheet,
                showSlashMenu = slashCommandState != null,
                isPreviewMode = isPreviewMode,
                slashQuery = slashCommandState?.second ?: "",
                slashStartIndex = slashCommandState?.first ?: -1,
                content = content,
                document = document,
                activeFilePath = activeFilePath,
                noteColor = noteColor,
                outlineItems = outlineItems,
                documentInfoStats = documentInfoStats,
                undoRedo = undoRedo,
                shareService = shareService,
                onContentChange = { content = it },
                onMarkUnsaved = { hasUnsavedChanges = true },
                onDismissSearch = { showSearchDialog = false },
                onDismissExport = { showExportDialog = false },
                onDismissOutline = { showOutlinePanel = false },
                onDismissDocumentInfo = { showDocumentInfoDialog = false },
                onDismissColorSheet = { showColorSheet = false },
                onColorSelected = { color -> viewModel.setColor(activeFilePath, color) },
            )
        }
    }
}