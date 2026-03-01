package com.bernaferrari.remarkor.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentFormatTest {
    @Test
    fun testFromExtension() {
        assertEquals(DocumentFormat.MARKDOWN, DocumentFormat.fromExtension(".md"))
        assertEquals(DocumentFormat.MARKDOWN, DocumentFormat.fromExtension("md"))
        assertEquals(DocumentFormat.MARKDOWN, DocumentFormat.fromExtension("MD"))
        assertEquals(DocumentFormat.TODOTXT, DocumentFormat.fromExtension(".txt"))
        assertEquals(DocumentFormat.PLAINTEXT, DocumentFormat.fromExtension(".unknown"))
    }
}
