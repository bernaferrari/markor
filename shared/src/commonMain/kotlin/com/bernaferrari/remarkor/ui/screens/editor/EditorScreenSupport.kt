package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import com.bernaferrari.remarkor.domain.model.Document
import okio.Path
import kotlin.math.roundToInt

internal data class DocumentInfoStats(
    val headings: Int,
    val lines: Int,
    val words: Int,
    val characters: Int,
    val charactersNoSpaces: Int,
    val bytes: Int
)

internal fun buildDocumentInfoStats(text: String, headings: Int): DocumentInfoStats {
    val lines = if (text.isEmpty()) 0 else text.count { it == '\n' } + 1
    val words = Regex("\\S+").findAll(text).count()
    val characters = text.length
    val charactersNoSpaces = text.count { !it.isWhitespace() }
    val bytes = text.encodeToByteArray().size
    return DocumentInfoStats(
        headings = headings,
        lines = lines,
        words = words,
        characters = characters,
        charactersNoSpaces = charactersNoSpaces,
        bytes = bytes
    )
}

internal fun formatStorageSize(bytes: Int): String {
    if (bytes < 1024) return "$bytes B"
    val kib = bytes / 1024.0
    val kibRounded = (kib * 10).roundToInt() / 10.0
    return "$bytes B ($kibRounded KiB)"
}

internal val intPlaceholderRegex = Regex("%(?:\\d+\\$)?d")

internal fun formatIntStringResource(template: String, value: Int): String {
    return intPlaceholderRegex.replace(template, value.toString())
}

internal data class TitleRenameResult(
    val document: Document?,
    val updatedTitleInput: String,
    val updatedPath: Path? = null
)

internal suspend fun commitTitleRenameIfNeeded(
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
        return TitleRenameResult(
            document = document.copy(title = trimmedTitle),
            updatedTitleInput = trimmedTitle
        )
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

internal fun buildTargetName(title: String, currentFileName: String): String {
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

internal data class LineNumberMeta(
    val number: Int?,
    val lineHeightPx: Float = 0f
)

internal fun darkenColor(color: Color, amount: Float): Color {
    val factor = amount.coerceIn(0f, 1f)
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

internal fun buildLineNumberMetadata(text: String): List<LineNumberMeta> {
    return buildLogicalLineNumberMetadata(text)
}

internal fun buildWrappedLineNumberMetadata(
    text: String,
    layoutResult: TextLayoutResult
): List<LineNumberMeta> {
    if (text.isEmpty()) {
        return listOf(LineNumberMeta(number = 1))
    }
    if (layoutResult.lineCount <= 0) {
        return buildLogicalLineNumberMetadata(text)
    }

    val logicalLineMetadata = buildLogicalLineNumberMetadata(text)
    val logicalLineStarts = buildLogicalLineStarts(text)
    val usedLogicalLineIndexes = hashSetOf<Int>()
    val result = MutableList(layoutResult.lineCount) { visualLine ->
        LineNumberMeta(
            number = null,
            lineHeightPx = layoutResult.getLineBottom(visualLine) - layoutResult.getLineTop(
                visualLine
            )
        )
    }

    for ((logicalLineIndex, lineStart) in logicalLineStarts.withIndex()) {
        val logicalMeta = logicalLineMetadata.getOrNull(logicalLineIndex)
            ?: continue

        val visualLine = if (lineStart >= text.length && text.isNotEmpty()) {
            layoutResult.lineCount - 1
        } else {
            layoutResult.getLineForOffset(lineStart)
        }

        if (!usedLogicalLineIndexes.contains(logicalLineIndex) && visualLine in result.indices) {
            result[visualLine] = result[visualLine].copy(number = logicalMeta.number)
            usedLogicalLineIndexes.add(logicalLineIndex)
        }
    }
    return result.ifEmpty { listOf(LineNumberMeta(number = 1)) }
}

internal fun buildLogicalLineNumberMetadata(text: String): List<LineNumberMeta> =
    if (text.isEmpty()) listOf(LineNumberMeta(number = 1))
    else text.split('\n').mapIndexed { index, line ->
        LineNumberMeta(number = index + 1)
    }

internal fun buildLogicalLineStarts(text: String): IntArray {
    if (text.isEmpty()) return intArrayOf(0)

    val lineCount = text.count { it == '\n' } + 1
    val starts = IntArray(lineCount)
    var currentLine = 0
    starts[0] = 0
    for (index in text.indices) {
        if (text[index] == '\n' && currentLine + 1 < lineCount) {
            currentLine++
            starts[currentLine] = index + 1
        }
    }
    return starts
}
// Helper functions for text manipulation
internal fun wrapSelection(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
    val before = value.text.substring(0, value.selection.start)
    val selected = value.text.substring(value.selection.start, value.selection.end)
    val after = value.text.substring(value.selection.end)

    val newText = before + prefix + selected + suffix + after
    val newCursor = value.selection.start + prefix.length + selected.length

    return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newCursor))
}

internal fun insertAtCursor(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
    val before = value.text.substring(0, value.selection.start)
    val after = value.text.substring(value.selection.end)
    val newText = before + prefix + suffix + after
    val newCursor = value.selection.start + prefix.length
    return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newCursor))
}

internal fun insertAtStartOfLine(value: TextFieldValue, textToInsert: String): TextFieldValue {
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
