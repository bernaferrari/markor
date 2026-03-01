package com.bernaferrari.remarkor.data.local.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import okio.Path

class NoteMetadataIndexer(
    private val fileRepository: IFileRepository,
    private val metadataRepository: NoteMetadataRepository
) {
    suspend fun indexDirectory(path: Path, nowMillis: Long) = withContext(Dispatchers.Default) {
        val files = fileRepository.listFilesRecursively(path)
        files.filter { isIndexable(it.path.name) }.forEach { fileInfo ->
            val content = fileRepository.readText(fileInfo.path)
            metadataRepository.upsertFromContent(fileInfo.path.toString(), content, nowMillis)
        }
    }

    private fun isIndexable(fileName: String): Boolean {
        val ext = fileName.substringAfterLast(".", "").lowercase()
        return ext in setOf("md", "markdown", "txt", "todo", "org", "adoc", "asciidoc", "rst")
    }
}
