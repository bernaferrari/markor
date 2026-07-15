package com.bernaferrari.remarkor.ui.screens.editor

import com.bernaferrari.remarkor.domain.model.Document
import okio.Path

internal data class EditorSessionCommit(
    val titleInput: String,
    val document: Document?,
    val activeFilePath: String?,
    val saveSucceeded: Boolean,
)

internal suspend fun commitEditorSession(
    titleInput: String,
    document: Document?,
    contentText: String,
    hasUnsavedChanges: Boolean,
    renameDocument: suspend (Document, String) -> Path?,
    saveDocument: suspend (Document, String) -> Boolean,
): EditorSessionCommit {
    val renameResult = commitTitleRenameIfNeeded(
        titleInput = titleInput,
        document = document,
        renameDocument = renameDocument,
    )
    val committedDocument = renameResult.document
    val saveSucceeded = !hasUnsavedChanges || committedDocument == null ||
        saveDocument(committedDocument, contentText)
    return EditorSessionCommit(
        titleInput = renameResult.updatedTitleInput,
        document = committedDocument,
        activeFilePath = renameResult.updatedPath?.toString(),
        saveSucceeded = saveSucceeded,
    )
}
