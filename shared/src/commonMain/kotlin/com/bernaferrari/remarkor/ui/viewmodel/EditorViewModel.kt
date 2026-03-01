package com.bernaferrari.remarkor.ui.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.remarkor.domain.model.Document
import com.bernaferrari.remarkor.domain.repository.IDocumentRepository
import com.bernaferrari.remarkor.ui.components.UserMessageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.document_created
import markor.shared.generated.resources.failed_to_create_document
import markor.shared.generated.resources.failed_to_load_document_with_arg
import markor.shared.generated.resources.failed_to_rename
import markor.shared.generated.resources.failed_to_rename_with_arg
import markor.shared.generated.resources.failed_to_save
import markor.shared.generated.resources.renamed_to_with_arg
import markor.shared.generated.resources.saved
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.getString

/**
 * Represents a state in the editor's history for undo/redo.
 */
data class EditorHistoryState(
    val text: String,
    val selection: TextRange,
    val timestamp: Long
)

class EditorViewModel(
    private val documentRepository: IDocumentRepository,
    private val metadataRepository: com.bernaferrari.remarkor.data.local.db.NoteMetadataRepository
) : ViewModel() {

    // User message management
    val messageManager = UserMessageManager()

    // Note color state
    private val _noteColor = MutableStateFlow<Int?>(null)
    val noteColor: StateFlow<Int?> = _noteColor.asStateFlow()

    // Document state
    private val _document = MutableStateFlow<Document?>(null)
    val document: StateFlow<Document?> = _document.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    // Undo/Redo stacks
    private val undoStack = MutableStateFlow<List<EditorHistoryState>>(emptyList())
    private val redoStack = MutableStateFlow<List<EditorHistoryState>>(emptyList())

    val canUndo: StateFlow<Boolean> = undoStack
        .map { list -> list.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canRedo: StateFlow<Boolean> = redoStack
        .map { list -> list.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Debounce job for undo stack commits
    private var undoDebounceJob: Job? = null

    // Maximum history size
    private val maxHistorySize = 50

    // Auto-save debounce
    private var autoSaveJob: Job? = null
    private val autoSaveDelay = 2000L // 2 seconds

    suspend fun loadDocument(filePath: String): Document? {
        _isLoading.value = true
        try {
            val path = filePath.toPath()
            val doc = documentRepository.loadDocument(path)
            _document.value = doc
            // Load note color from metadata
            val metadata = metadataRepository.getNoteByPath(filePath)
            _noteColor.value = metadata?.color
            // Clear history when loading new document
            undoStack.value = emptyList()
            redoStack.value = emptyList()
            _hasUnsavedChanges.value = false
            return doc
        } catch (e: Exception) {
            messageManager.error(
                getString(
                    Res.string.failed_to_load_document_with_arg,
                    e.message ?: ""
                )
            )
            return null
        } finally {
            _isLoading.value = false
        }
    }

    fun saveDocument(document: Document, content: String) {
        viewModelScope.launch {
            try {
                documentRepository.saveDocument(document, content)
                _hasUnsavedChanges.value = false
                messageManager.success(getString(Res.string.saved))
            } catch (e: Exception) {
                messageManager.error(getString(Res.string.failed_to_save) + ": ${e.message}")
            }
        }
    }

    /**
     * Auto-save with debounce.
     */
    fun autoSave(document: Document, content: String) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(autoSaveDelay)
            saveDocument(document, content)
        }
    }

    fun createNewDocument(path: Path, content: String = "") {
        viewModelScope.launch {
            try {
                documentRepository.createDocument(path, content)
                messageManager.success(getString(Res.string.document_created))
            } catch (e: Exception) {
                messageManager.error(getString(Res.string.failed_to_create_document))
            }
        }
    }

    suspend fun renameDocument(document: Document, newName: String): Path? {
        return try {
            val renamedPath = documentRepository.renameDocument(document, newName)
            if (renamedPath != null) {
                messageManager.success(getString(Res.string.renamed_to_with_arg, renamedPath.name))
            } else {
                messageManager.error(getString(Res.string.failed_to_rename))
            }
            renamedPath
        } catch (e: Exception) {
            messageManager.error(getString(Res.string.failed_to_rename_with_arg, e.message ?: ""))
            null
        }
    }

    fun setColor(path: String, color: Int?) {
        _noteColor.value = color
        viewModelScope.launch {
            metadataRepository.setColor(path, color)
        }
    }

    fun setArchived(path: String, archived: Boolean) {
        viewModelScope.launch {
            metadataRepository.setArchived(path, archived)
        }
    }

    // ========== Undo/Redo Management ==========

    /**
     * Push a state to the undo stack.
     * Uses debouncing to avoid cluttering history with every character.
     */
    fun pushToUndo(state: EditorHistoryState) {
        undoDebounceJob?.cancel()

        // If this is a significant change (e.g., paste > 10 chars), commit immediately
        val currentState = undoStack.value.lastOrNull()
        val isSignificantChange = currentState != null &&
                kotlin.math.abs(state.text.length - currentState.text.length) > 10

        if (isSignificantChange) {
            commitToUndoStack(state)
        } else {
            // Debounce normal typing
            undoDebounceJob = viewModelScope.launch {
                delay(1500) // 1.5 second pause = commit point
                commitToUndoStack(state)
            }
        }
    }

    private fun commitToUndoStack(state: EditorHistoryState) {
        val currentStack = undoStack.value.toMutableList()

        // Don't add duplicates
        if (currentStack.isNotEmpty() && currentStack.last().text == state.text) {
            return
        }

        currentStack.add(state)

        // Limit stack size
        if (currentStack.size > maxHistorySize) {
            currentStack.removeAt(0)
        }

        undoStack.value = currentStack
        redoStack.value = emptyList() // Clear redo on new change
    }

    /**
     * Perform undo operation.
     * @param currentState The current editor state (to push to redo stack)
     * @return The state to restore, or null if undo not possible
     */
    fun undo(currentState: EditorHistoryState): EditorHistoryState? {
        val undoStackList = undoStack.value.toMutableList()
        if (undoStackList.isEmpty()) return null

        // Remove last state from undo
        val stateToRestore = undoStackList.removeLast()
        undoStack.value = undoStackList

        // Push current state to redo
        val redoStackList = redoStack.value.toMutableList()
        redoStackList.add(currentState)
        redoStack.value = redoStackList

        return stateToRestore
    }

    /**
     * Perform redo operation.
     * @param currentState The current editor state (to push to undo stack)
     * @return The state to restore, or null if redo not possible
     */
    fun redo(currentState: EditorHistoryState): EditorHistoryState? {
        val redoStackList = redoStack.value.toMutableList()
        if (redoStackList.isEmpty()) return null

        // Remove last state from redo
        val stateToRestore = redoStackList.removeLast()
        redoStack.value = redoStackList

        // Push current state to undo
        val undoStackList = undoStack.value.toMutableList()
        undoStackList.add(currentState)
        undoStack.value = undoStackList

        return stateToRestore
    }

    /**
     * Clear all history.
     */
    fun clearHistory() {
        undoStack.value = emptyList()
        redoStack.value = emptyList()
    }

    /**
     * Mark that there are unsaved changes.
     */
    fun markChanged() {
        _hasUnsavedChanges.value = true
    }

    // ========== Text Manipulation Helpers ==========

    fun wrapSelection(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
        val before = value.text.substring(0, value.selection.start)
        val selected = value.text.substring(value.selection.start, value.selection.end)
        val after = value.text.substring(value.selection.end)

        val newText = before + prefix + selected + suffix + after
        val newCursor = value.selection.start + prefix.length + selected.length

        return value.copy(text = newText, selection = TextRange(newCursor))
    }

    fun insertAtCursor(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
        val before = value.text.substring(0, value.selection.start)
        val after = value.text.substring(value.selection.end)
        val newText = before + prefix + suffix + after
        val newCursor = value.selection.start + prefix.length
        return value.copy(text = newText, selection = TextRange(newCursor))
    }

    fun insertAtStartOfLine(value: TextFieldValue, textToInsert: String): TextFieldValue {
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
        return value.copy(text = newText, selection = TextRange(newCursor))
    }
}
