package com.bernaferrari.remarkor.appfunctions

import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import com.bernaferrari.remarkor.domain.model.Document
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.domain.repository.IDocumentRepository
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import kotlinx.coroutines.flow.first
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

/**
 * Exposes Markor note-taking capabilities to Android AppFunctions (on-device AI agents).
 */
@Single
class NoteFunctions(
    private val fileRepository: IFileRepository,
    private val documentRepository: IDocumentRepository,
    private val settingsRepository: ISettingsRepository,
    @Named("default_notebook_path") private val defaultNotebookPath: String,
) {
    /** A note returned by AppFunctions. */
    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class Note(
        /** The absolute file path, used as a stable identifier. */
        val id: String,
        /** The note title (filename without extension). */
        val title: String,
        /** The note body content. */
        val content: String,
    )

    /**
     * Lists all notes in the user's notebook directory.
     *
     * @param appFunctionContext The context in which the AppFunction is executed.
     * @return All notes, or null if the notebook is empty.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun listNotes(appFunctionContext: AppFunctionContext): List<Note>? {
        val notes = listNoteFiles().map { file -> file.toNote() }
        return notes.ifEmpty { null }
    }

    /**
     * Creates a new markdown note in the user's notebook.
     *
     * @param appFunctionContext The context in which the AppFunction is executed.
     * @param title The title of the note (used as the filename).
     * @param content The note body content. Defaults to empty.
     * @return The created note.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createNote(
        appFunctionContext: AppFunctionContext,
        title: String,
        content: String?,
    ): Note {
        if (title.isBlank()) {
            throw AppFunctionInvalidArgumentException("Title cannot be blank")
        }

        val noteContent = content.orEmpty()
        val notebook = getNotebookPath()
        val fileName = "${sanitizeFileName(title)}.md"
        val path = fileRepository.createFileWithContent(notebook, fileName, noteContent)
            ?: throw AppFunctionAppUnknownException("Failed to create note")

        return Note(
            id = path.toString(),
            title = title.trim(),
            content = noteContent,
        )
    }

    /**
     * Searches notes by filename or content within the notebook.
     *
     * @param appFunctionContext The context in which the AppFunction is executed.
     * @param query The search term to match against note titles and content.
     * @return Matching notes.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchNotes(
        appFunctionContext: AppFunctionContext,
        query: String,
    ): List<Note> {
        if (query.isBlank()) {
            throw AppFunctionInvalidArgumentException("Search query cannot be blank")
        }

        val notebook = getNotebookPath()
        val byName = fileRepository.searchFiles(notebook, query)
        val byContent = fileRepository.searchContent(notebook, query)
        val matches = (byName + byContent)
            .distinctBy { it.path }
            .filter { isNoteFile(it) }

        if (matches.isEmpty()) {
            throw AppFunctionElementNotFoundException("No notes found matching: $query")
        }

        return matches.map { it.toNote() }
    }

    /**
     * Appends text to the Quick Note scratchpad.
     *
     * @param appFunctionContext The context in which the AppFunction is executed.
     * @param text The text to append to the quick note.
     * @return The updated quick note.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun appendToQuickNote(
        appFunctionContext: AppFunctionContext,
        text: String,
    ): Note {
        if (text.isBlank()) {
            throw AppFunctionInvalidArgumentException("Text to append cannot be blank")
        }

        val quickNotePath = settingsRepository.getQuickNoteFilePath.first()
        val path = if (quickNotePath.isNotEmpty()) {
            quickNotePath.toPath()
        } else {
            val notebook = getNotebookPath()
            fileRepository.createFileWithContent(notebook, "quicknote.md", "")
                ?: throw AppFunctionAppUnknownException("Failed to create quick note")
        }

        val existing = documentRepository.readContent(path).orEmpty()
        val updated = if (existing.isEmpty()) text else "$existing\n$text"
        val document = Document.fromPath(path)
        if (!documentRepository.saveDocument(document, updated)) {
            throw AppFunctionAppUnknownException("Failed to save quick note")
        }

        return Note(
            id = path.toString(),
            title = "Quick Note",
            content = updated,
        )
    }

    private suspend fun getNotebookPath(): Path {
        val configured = settingsRepository.getNotebookDirectory.first()
        val path = configured.ifEmpty { defaultNotebookPath }
        return path.toPath()
    }

    private suspend fun listNoteFiles(): List<FileInfo> {
        val notebook = getNotebookPath()
        return fileRepository.listFilesRecursively(notebook).filter { isNoteFile(it) }
    }

    private suspend fun FileInfo.toNote(): Note {
        val body = preview ?: fileRepository.readText(path).take(MAX_PREVIEW_LENGTH)
        return Note(
            id = path.toString(),
            title = name.substringBeforeLast("."),
            content = body,
        )
    }

    private fun isNoteFile(fileInfo: FileInfo): Boolean {
        return fileInfo.extension.lowercase() in NOTE_EXTENSIONS
    }

    private fun sanitizeFileName(title: String): String {
        return title.trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .ifEmpty { "note" }
    }

    private companion object {
        const val MAX_PREVIEW_LENGTH = 500
        val NOTE_EXTENSIONS = setOf("md", "txt", "org", "adoc", "wiki", "csv", "keyvalue")
    }
}