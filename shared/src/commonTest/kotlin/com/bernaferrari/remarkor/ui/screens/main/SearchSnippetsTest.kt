package com.bernaferrari.remarkor.ui.screens.main

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchSnippetsTest {
    private val note = """
        # Lisbon in May

        Four slow days. **Book the tram early**, walk everything else.

        ## Day one
        - [x] Coffee at the miradouro
        - [ ] Tram 28 before 9 am
    """.trimIndent()

    @Test
    fun matchingLinesAreReturnedWithoutMarkdownMarkers() {
        assertEquals(
            listOf("Four slow days. Book the tram early, walk everything else.", "Tram 28 before 9 am"),
            searchSnippets(note, "TRAM"),
        )
    }

    @Test
    fun noContentMatchFallsBackToTheFirstLines() {
        assertEquals(listOf("Lisbon in May", "Four slow days. Book the tram early, walk everything else."), searchSnippets(note, "lisbon trip"))
    }

    @Test
    fun everyOccurrenceIsHighlighted() {
        val text = highlightMatches("Tram, then tram again", "tram", Color.Yellow, Color.Black)
        assertEquals(listOf(0 to 4, 11 to 15), text.spanStyles.map { it.start to it.end })
    }

    @Test
    fun longLinesStartNearTheMatch() {
        val line = "There is a particular quiet before seven, when the kettle is the loudest thing in the house."
        assertEquals(listOf("…when the kettle is the loudest thing in the house."), searchSnippets(line, "loudest"))
    }
}
