package com.bernaferrari.remarkor.data.local.db

import kotlin.math.min

object NoteMetadataMapper {
    fun buildNoteEntity(
        path: String,
        content: String,
        existing: NoteEntity?,
        nowMillis: Long
    ): NoteEntity {
        val title = extractTitle(content, path)
        val preview = extractPreview(content)
        val wordCount = countWords(content)
        val charCount = content.length
        val noteType = inferNoteType(path)
        val imagePreviewUrl = extractFirstImage(content)

        return NoteEntity(
            id = existing?.id ?: 0,
            path = path,
            title = title,
            noteType = noteType,
            createdAt = existing?.createdAt ?: nowMillis,
            updatedAt = nowMillis,
            pinned = existing?.pinned ?: false,
            isArchived = existing?.isArchived ?: false,
            color = existing?.color,
            preview = preview,
            imagePreviewUrl = imagePreviewUrl,
            wordCount = wordCount,
            charCount = charCount
        )
    }

    fun buildNoteEntityFromPath(
        path: String,
        existing: NoteEntity?,
        nowMillis: Long
    ): NoteEntity {
        val title = path.substringAfterLast("/").substringBeforeLast(".")
        return NoteEntity(
            id = existing?.id ?: 0,
            path = path,
            title = title,
            noteType = inferNoteType(path),
            createdAt = existing?.createdAt ?: nowMillis,
            updatedAt = nowMillis,
            pinned = existing?.pinned ?: false,
            isArchived = existing?.isArchived ?: false,
            color = existing?.color,
            preview = existing?.preview,
            imagePreviewUrl = existing?.imagePreviewUrl,
            wordCount = existing?.wordCount,
            charCount = existing?.charCount
        )
    }

    private fun extractTitle(content: String, path: String): String {
        val lines = content.lineSequence()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val withoutHashes = trimmed.trimStart('#').trim()
            if (withoutHashes.isNotEmpty()) return withoutHashes
        }
        return path.substringAfterLast("/").substringBeforeLast(".")
    }

    private fun extractPreview(content: String): String? {
        if (content.isBlank()) return null
        val normalized = content.replace(Regex("\\s+"), " ").trim()
        if (normalized.isEmpty()) return null
        val maxLen = 180
        return normalized.substring(0, min(normalized.length, maxLen))
    }

    private fun countWords(content: String): Int {
        if (content.isBlank()) return 0
        return Regex("\\b\\w+\\b").findAll(content).count()
    }

    private fun inferNoteType(path: String): String {
        val ext = path.substringAfterLast(".", "").lowercase()
        return when (ext) {
            "md", "markdown" -> "markdown"
            "txt", "todo" -> "text"
            "org" -> "org"
            "adoc", "asciidoc" -> "asciidoc"
            else -> ext.ifEmpty { "text" }
        }
    }

    private fun extractFirstImage(content: String): String? {
        // Regex to match markdown images: ![alt](url)
        val regex = Regex("!\\[.*?]\\((.*?)\\)")
        val match = regex.find(content)
        return match?.groupValues?.get(1)
    }
}
