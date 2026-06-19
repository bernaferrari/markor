package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

internal class EditorUndoRedoState(
    private val scope: CoroutineScope,
) {
    val undoStack: SnapshotStateList<TextFieldValue> = mutableStateListOf()
    val redoStack: SnapshotStateList<TextFieldValue> = mutableStateListOf()
    private var debounceJob: Job? = null

    fun pushImmediate(value: TextFieldValue) {
        if (undoStack.isEmpty() || undoStack.last().text != value.text) {
            undoStack.add(value)
            redoStack.clear()
            if (undoStack.size > MAX_STACK_SIZE) undoStack.removeAt(0)
        }
    }

    fun trackContentChange(previous: TextFieldValue, newContent: TextFieldValue) {
        val isSignificantChange = abs(newContent.text.length - previous.text.length) > 10
        if (isSignificantChange) {
            pushImmediate(previous)
        } else {
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(DEBOUNCE_MS)
                pushImmediate(newContent)
            }
        }
    }

    fun undo(current: TextFieldValue): TextFieldValue? {
        if (undoStack.isEmpty()) return null
        redoStack.add(current)
        return undoStack.removeAt(undoStack.lastIndex)
    }

    fun redo(current: TextFieldValue): TextFieldValue? {
        if (redoStack.isEmpty()) return null
        undoStack.add(current)
        return redoStack.removeAt(redoStack.lastIndex)
    }

    private companion object {
        const val MAX_STACK_SIZE = 50
        const val DEBOUNCE_MS = 2_000L
    }
}

@Composable
internal fun rememberEditorUndoRedo(): EditorUndoRedoState {
    val scope = rememberCoroutineScope()
    return remember(scope) { EditorUndoRedoState(scope) }
}