package com.bernaferrari.remarkor.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FocusModeTest {
    @Test
    fun calculateParagraphAlpha_disabled_returnsFullOpacity() {
        assertEquals(1f, calculateParagraphAlpha(0, 2, isFocusMode = false))
    }

    @Test
    fun calculateParagraphAlpha_activeLine_isFullyVisible() {
        assertEquals(1f, calculateParagraphAlpha(3, 3, isFocusMode = true))
    }

    @Test
    fun calculateParagraphAlpha_distantLine_isDimmed() {
        assertEquals(0.2f, calculateParagraphAlpha(0, 5, isFocusMode = true))
    }

    @Test
    fun getCurrentParagraphIndex_countsLinesBeforeCursor() {
        val text = "first\nsecond\nthird"
        assertEquals(0, getCurrentParagraphIndex(text, 0))
        assertEquals(0, getCurrentParagraphIndex(text, 4))
        assertEquals(1, getCurrentParagraphIndex(text, 6))
        assertEquals(2, getCurrentParagraphIndex(text, text.length))
    }

    @Test
    fun getCurrentParagraphIndex_emptyText_returnsZero() {
        assertEquals(0, getCurrentParagraphIndex("", 0))
    }
}