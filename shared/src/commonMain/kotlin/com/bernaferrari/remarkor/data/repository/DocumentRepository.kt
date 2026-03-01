package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.data.local.AppSettings
import com.bernaferrari.remarkor.data.local.db.NoteMetadataRepository
import com.bernaferrari.remarkor.domain.model.Document
import com.bernaferrari.remarkor.domain.repository.IDocumentRepository
import com.bernaferrari.remarkor.util.nowMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

class DocumentRepository(
    private val appSettings: AppSettings,
    private val noteMetadataRepository: NoteMetadataRepository
) : IDocumentRepository {

    private val documentCache = MutableStateFlow<Map<String, Document>>(emptyMap())
    private val fileSystem = FileSystem.Companion.SYSTEM

    override suspend fun loadDocument(path: Path): Document? = withContext(Dispatchers.Default) {
        try {
            if (!fileSystem.exists(path)) return@withContext null
            val metadata = fileSystem.metadata(path)
            if (metadata.isDirectory) return@withContext null

            // Read content
            val content = fileSystem.read(path) {
                readUtf8()
            }

            val lastModified = metadata.lastModifiedAtMillis ?: 0L

            val document =
                Document.fromPath(path).copy(content = content, lastModified = lastModified)
            updateCache(document)
            document
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun saveDocument(document: Document, content: String): Boolean =
        withContext(Dispatchers.Default) {
            try {
                fileSystem.write(document.path) {
                    writeUtf8(content)
                }
                val timestamp = nowMillis()
                val updated = document.copy(
                    content = content,
                    lastModified = timestamp
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

    override suspend fun createDocument(path: Path, content: String): Document? =
        withContext(Dispatchers.Default) {
            try {
                val parent = path.parent ?: ".".toPath()
                parent.let {
                    if (!fileSystem.exists(parent)) {
                        fileSystem.createDirectories(parent)
                    }
                }
                val actualPath = resolveUniquePath(parent, path.name)
                fileSystem.write(actualPath) {
                    writeUtf8(content)
                }

                val metadata = fileSystem.metadata(actualPath)
                val document = Document.fromPath(actualPath)
                    .copy(content = content, lastModified = metadata.lastModifiedAtMillis ?: 0L)
                updateCache(document)
                noteMetadataRepository.upsertFromContent(
                    path = actualPath.toString(),
                    content = content,
                    nowMillis = document.lastModified
                )
                document
            } catch (e: IOException) {
                null
            }
        }

    override suspend fun deleteDocument(path: Path): Boolean = withContext(Dispatchers.Default) {
        try {
            fileSystem.delete(path)
            removeFromCache(path)
            noteMetadataRepository.deleteByPath(path.toString())
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun renameDocument(document: Document, newName: String): Path? =
        withContext(Dispatchers.Default) {
            try {
                val oldPath = document.path
                val parent = oldPath.parent ?: ".".toPath()
                val newPath = resolveUniquePath(parent, newName, excludePath = oldPath)
                if (newPath == oldPath) return@withContext oldPath
                fileSystem.atomicMove(oldPath, newPath)
                removeFromCache(oldPath)
                noteMetadataRepository.updatePath(
                    oldPath = oldPath.toString(),
                    newPath = newPath.toString(),
                    nowMillis = nowMillis()
                )
                newPath
            } catch (e: IOException) {
                null
            }
        }

    override fun observeDocument(path: Path): Flow<Document?> {
        return MutableStateFlow(documentCache.value[path.toString()])
    }

    override suspend fun documentExists(path: Path): Boolean = withContext(Dispatchers.Default) {
        fileSystem.exists(path)
    }

    override suspend fun readContent(path: Path): String? = withContext(Dispatchers.Default) {
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

    private fun resolveUniquePath(
        parent: Path,
        requestedName: String,
        excludePath: Path? = null
    ): Path {
        val normalizedName = requestedName.trim().ifBlank { "Untitled" }
        val (baseName, extension) = splitBaseAndExtension(normalizedName)
        var index = 0

        while (true) {
            val candidateName = if (index == 0) {
                normalizedName
            } else {
                "$baseName ($index)$extension"
            }
            val candidatePath = parent / candidateName
            if (excludePath != null && candidatePath == excludePath) return candidatePath
            if (!fileSystem.exists(candidatePath)) return candidatePath
            index++
        }
    }

    private fun splitBaseAndExtension(fileName: String): Pair<String, String> {
        val dotIndex = fileName.lastIndexOf('.')
        if (dotIndex <= 0 || dotIndex == fileName.length - 1) {
            return fileName to ""
        }
        return fileName.substring(0, dotIndex) to fileName.substring(dotIndex)
    }
}
