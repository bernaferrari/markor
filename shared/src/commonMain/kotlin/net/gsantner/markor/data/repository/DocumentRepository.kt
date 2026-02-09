package net.gsantner.markor.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.gsantner.markor.data.local.AppSettings
import net.gsantner.markor.data.local.db.NoteMetadataRepository
import net.gsantner.markor.domain.model.Document
import net.gsantner.markor.domain.repository.IDocumentRepository
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

class DocumentRepository(
    private val appSettings: AppSettings,
    private val noteMetadataRepository: NoteMetadataRepository
) : IDocumentRepository {

    private val documentCache = MutableStateFlow<Map<String, Document>>(emptyMap())
    private val fileSystem = FileSystem.SYSTEM

    override suspend fun loadDocument(path: Path): Document? = withContext(Dispatchers.IO) {
        try {
            if (!fileSystem.exists(path)) return@withContext null
            val metadata = fileSystem.metadata(path)
            if (metadata.isDirectory) return@withContext null

            // Read content
            val content = fileSystem.read(path) {
                readUtf8()
            }
            
            val lastModified = metadata.lastModifiedAtMillis ?: 0L

            val document = Document.fromPath(path).copy(content = content, lastModified = lastModified)
            updateCache(document)
            document
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun saveDocument(document: Document, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            fileSystem.write(document.path) {
                writeUtf8(content)
            }
            val updated = document.copy(
                content = content,
                lastModified = Clock.System.now().toEpochMilliseconds()
            )
            updateCache(updated)
            noteMetadataRepository.upsertFromContent(
                path = document.path.toString(),
                content = content,
                nowMillis = updated.lastModified
            )
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun createDocument(path: Path, content: String): Document? = withContext(Dispatchers.IO) {
        try {
            path.parent?.let { parent ->
                if (!fileSystem.exists(parent)) {
                    fileSystem.createDirectories(parent)
                }
            }
            fileSystem.write(path) {
                writeUtf8(content)
            }
            
            val metadata = fileSystem.metadata(path)
            val document = Document.fromPath(path).copy(content = content, lastModified = metadata.lastModifiedAtMillis ?: 0L)
            updateCache(document)
            noteMetadataRepository.upsertFromContent(
                path = path.toString(),
                content = content,
                nowMillis = document.lastModified
            )
            document
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun deleteDocument(path: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            fileSystem.delete(path)
            removeFromCache(path)
            noteMetadataRepository.deleteByPath(path.toString())
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun renameDocument(document: Document, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val oldPath = document.path
            val newPath = (oldPath.parent ?: ".".toPath()) / newName
            fileSystem.atomicMove(oldPath, newPath)
            removeFromCache(oldPath)
            noteMetadataRepository.updatePath(
                oldPath = oldPath.toString(),
                newPath = newPath.toString(),
                nowMillis = Clock.System.now().toEpochMilliseconds()
            )
            // Update cache with new document?
            // For now just invalidate old one.
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun observeDocument(path: Path): Flow<Document?> {
        return MutableStateFlow(documentCache.value[path.toString()])
    }

    override suspend fun documentExists(path: Path): Boolean = withContext(Dispatchers.IO) {
        fileSystem.exists(path)
    }

    override suspend fun readContent(path: Path): String? = withContext(Dispatchers.IO) {
        try {
            fileSystem.read(path) {
                readUtf8()
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun updateCache(document: Document) {
        documentCache.value = documentCache.value + (document.path.toString() to document)
    }

    private fun removeFromCache(path: Path) {
        documentCache.value = documentCache.value - path.toString()
    }
}
