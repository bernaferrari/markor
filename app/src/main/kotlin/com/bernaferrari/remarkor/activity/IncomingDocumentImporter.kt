package com.bernaferrari.remarkor.activity

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.bernaferrari.remarkor.domain.repository.IDocumentRepository
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath
import java.io.File

/** Converts Android share/open intents into documents the multiplatform editor can open. */
internal class IncomingDocumentImporter(
    private val contentResolver: ContentResolver,
    private val settingsRepository: ISettingsRepository,
    private val documentRepository: IDocumentRepository,
    private val defaultNotebookPath: String,
) {
    suspend fun import(intent: Intent): String? {
        if (intent.action !in SUPPORTED_ACTIONS) return null

        val streamUri = intent.getParcelableExtraCompat(Intent.EXTRA_STREAM)
            ?: intent.clipData?.getItemAt(0)?.uri
            ?: intent.data

        if (streamUri != null) {
            streamUri.directFilePath(intent.action)?.let { return it }

            val content = readText(streamUri) ?: return null
            return createImportedDocument(
                fileName = displayName(streamUri, intent),
                content = content,
            )
        }

        val sharedText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
            ?.toString()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return createImportedDocument(
            fileName = ensureTextExtension(intent.getStringExtra(Intent.EXTRA_TITLE)),
            content = sharedText,
        )
    }

    private suspend fun createImportedDocument(fileName: String, content: String): String? {
        val notebookPath = settingsRepository.getNotebookDirectory.first()
            .ifBlank { defaultNotebookPath }
        val path = notebookPath.toPath() / sanitizeFileName(fileName)
        return documentRepository.createDocument(path, content)?.path?.toString()
    }

    private fun readText(uri: Uri): String? = try {
        contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8).use { reader ->
            reader?.readText()
        }
    } catch (_: Exception) {
        null
    }

    private fun displayName(uri: Uri, intent: Intent): String {
        val queriedName = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

        return ensureTextExtension(
            queriedName
                ?: intent.getStringExtra(Intent.EXTRA_TITLE)
                ?: uri.lastPathSegment?.substringAfterLast('/')
                ?: DEFAULT_FILE_NAME
        )
    }

    private fun Uri.directFilePath(action: String?): String? {
        if (scheme != ContentResolver.SCHEME_FILE) return null
        if (action != Intent.ACTION_VIEW && action != Intent.ACTION_EDIT) return null

        return path
            ?.let(::File)
            ?.takeIf { it.isFile && it.canRead() }
            ?.absolutePath
    }

    private fun sanitizeFileName(fileName: String): String {
        val sanitized = fileName
            .replace('/', '_')
            .replace('\\', '_')
            .replace(Regex("[\\u0000-\\u001F]"), "_")
            .trim()
            .take(MAX_FILE_NAME_LENGTH)

        return sanitized
            .takeUnless { it.isBlank() || it == "." || it == ".." }
            ?: DEFAULT_FILE_NAME
    }

    private fun ensureTextExtension(fileName: String?): String {
        val name = fileName?.trim().orEmpty()
        return if (name.isBlank()) DEFAULT_FILE_NAME
        else if (name.contains('.')) name
        else "$name.txt"
    }

    @Suppress("DEPRECATION")
    private inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? =
        getParcelableExtra(key) as? T

    private companion object {
        val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_SEND,
            Intent.ACTION_VIEW,
            Intent.ACTION_EDIT,
            "com.google.android.gm.action.AUTO_SEND",
            "com.google.android.gms.actions.CREATE_NOTE",
        )
        const val DEFAULT_FILE_NAME = "shared_note.txt"
        const val MAX_FILE_NAME_LENGTH = 120
    }
}
