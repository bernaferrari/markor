package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.CoroutineScope

class EditorUndoRedoTest {
    private val scope = CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)

    @Test
    fun pushImmediate_andUndo_redo_roundTrip() {
        val state = EditorUndoRedoState(scope)
        val first = TextFieldValue("one", TextRange(3))
        val second = TextFieldValue("two", TextRange(3))

        state.pushImmediate(first)
        val undone = state.undo(second)
        assertEquals("one", undone?.text)

        val redone = state.redo(undone!!)
        assertEquals("two", redone?.text)
    }

    @Test
    fun undo_onEmptyStack_returnsNull() {
        val state = EditorUndoRedoState(scope)
        assertNull(state.undo(TextFieldValue("x")))
    }

    @Test
    fun pushImmediate_deduplicatesIdenticalText() = runTest {
        val state = EditorUndoRedoState(this)
        val value = TextFieldValue("same")
        state.pushImmediate(value)
        state.pushImmediate(value)
        assertEquals(1, state.undoStack.size)
    }
}