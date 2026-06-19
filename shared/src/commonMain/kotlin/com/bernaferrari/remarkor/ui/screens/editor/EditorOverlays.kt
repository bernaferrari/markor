package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.domain.model.Document
import com.bernaferrari.remarkor.domain.service.ShareService
import com.bernaferrari.remarkor.ui.components.AdvancedSearchReplaceDialog
import com.bernaferrari.remarkor.ui.components.ColorSelectionSheet
import com.bernaferrari.remarkor.ui.components.ExportDialog
import com.bernaferrari.remarkor.ui.components.OutlineItem
import com.bernaferrari.remarkor.ui.components.OutlinePanel
import com.bernaferrari.remarkor.ui.components.SlashCommandMenu
import com.bernaferrari.remarkor.ui.components.applySlashCommand
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.characters_no_spaces_with_arg
import markor.shared.generated.resources.characters_with_arg
import markor.shared.generated.resources.close
import markor.shared.generated.resources.document_info
import markor.shared.generated.resources.headings_with_arg
import markor.shared.generated.resources.lines_with_arg
import markor.shared.generated.resources.size_utf8_with_arg
import markor.shared.generated.resources.words_with_arg
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditorOverlays(
    showSearchDialog: Boolean,
    showExportDialog: Boolean,
    showOutlinePanel: Boolean,
    showDocumentInfoDialog: Boolean,
    showColorSheet: Boolean,
    showSlashMenu: Boolean,
    isPreviewMode: Boolean,
    slashQuery: String,
    slashStartIndex: Int,
    content: TextFieldValue,
    document: Document?,
    activeFilePath: String,
    noteColor: Int?,
    outlineItems: List<OutlineItem>,
    documentInfoStats: DocumentInfoStats,
    undoRedo: EditorUndoRedoState,
    shareService: ShareService,
    onContentChange: (TextFieldValue) -> Unit,
    onMarkUnsaved: () -> Unit,
    onDismissSearch: () -> Unit,
    onDismissExport: () -> Unit,
    onDismissOutline: () -> Unit,
    onDismissDocumentInfo: () -> Unit,
    onDismissColorSheet: () -> Unit,
    onColorSelected: (Int?) -> Unit,
) {
    if (showSearchDialog) {
        AdvancedSearchReplaceDialog(
            onDismiss = onDismissSearch,
            onFindNext = { query ->
                val text = content.text
                val startIndex = content.selection.end
                var index = text.indexOf(query, startIndex, ignoreCase = true)
                if (index == -1) {
                    index = text.indexOf(query, 0, ignoreCase = true)
                }
                if (index != -1) {
                    onContentChange(
                        content.copy(selection = TextRange(index, index + query.length)),
                    )
                }
            },
            onReplace = { query, replacement ->
                val text = content.text
                val selection = content.selection
                val selectedText = if (selection.min != selection.max) {
                    text.substring(selection.min, selection.max)
                } else {
                    ""
                }

                if (selectedText.equals(query, ignoreCase = true)) {
                    val newText = text.replaceRange(selection.min, selection.max, replacement)
                    undoRedo.pushImmediate(content)
                    onContentChange(
                        content.copy(
                            text = newText,
                            selection = TextRange(selection.min + replacement.length),
                        ),
                    )
                    onMarkUnsaved()
                } else {
                    val startIndex = content.selection.end
                    var index = text.indexOf(query, startIndex, ignoreCase = true)
                    if (index == -1) index = text.indexOf(query, 0, ignoreCase = true)
                    if (index != -1) {
                        onContentChange(
                            content.copy(selection = TextRange(index, index + query.length)),
                        )
                    }
                }
            },
            onReplaceAll = { query, replacement ->
                val newText = content.text.replace(query, replacement, ignoreCase = true)
                if (newText != content.text) {
                    undoRedo.pushImmediate(content)
                    onContentChange(content.copy(text = newText))
                    onMarkUnsaved()
                }
            },
        )
    }

    SlashCommandMenu(
        visible = showSlashMenu && !isPreviewMode,
        query = slashQuery,
        onSelect = { command ->
            val newContent = applySlashCommand(content, command, slashStartIndex)
            undoRedo.pushImmediate(content)
            onContentChange(newContent)
            onMarkUnsaved()
        },
        onDismiss = {},
    )

    if (showExportDialog && document != null) {
        val doc = document
        ExportDialog(
            filePath = activeFilePath,
            fileName = doc.name,
            markdownContent = content.text,
            onDismiss = onDismissExport,
            onShareHtml = onDismissExport,
            onPrint = onDismissExport,
            onShareMarkdown = {
                onDismissExport()
                shareService.shareFile(
                    fileName = doc.name,
                    content = content.text.encodeToByteArray(),
                    title = "Share Markdown",
                    mimeType = "text/markdown",
                )
            },
        )
    }

    if (showOutlinePanel) {
        OutlinePanel(
            items = outlineItems,
            currentCharOffset = content.selection.start,
            onItemClick = { item ->
                onContentChange(content.copy(selection = TextRange(item.charOffset)))
            },
            onDismiss = onDismissOutline,
        )
    }

    if (showDocumentInfoDialog) {
        EditorDocumentInfoDialog(
            stats = documentInfoStats,
            onDismiss = onDismissDocumentInfo,
        )
    }

    if (showColorSheet) {
        ColorSelectionSheet(
            currentColor = noteColor,
            onColorSelected = { color ->
                onColorSelected(color)
                onDismissColorSheet()
            },
            onDismiss = onDismissColorSheet,
        )
    }
}

@Composable
private fun EditorDocumentInfoDialog(
    stats: DocumentInfoStats,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.document_info)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    formatIntStringResource(
                        stringResource(Res.string.headings_with_arg),
                        stats.headings,
                    ),
                )
                Text(
                    formatIntStringResource(
                        stringResource(Res.string.lines_with_arg),
                        stats.lines,
                    ),
                )
                Text(
                    formatIntStringResource(
                        stringResource(Res.string.words_with_arg),
                        stats.words,
                    ),
                )
                Text(
                    formatIntStringResource(
                        stringResource(Res.string.characters_with_arg),
                        stats.characters,
                    ),
                )
                Text(
                    formatIntStringResource(
                        stringResource(Res.string.characters_no_spaces_with_arg),
                        stats.charactersNoSpaces,
                    ),
                )
                Text(
                    stringResource(
                        Res.string.size_utf8_with_arg,
                        formatStorageSize(stats.bytes),
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close))
            }
        },
    )
}