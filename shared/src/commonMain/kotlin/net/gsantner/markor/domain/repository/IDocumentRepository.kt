package net.gsantner.markor.domain.repository

import kotlinx.coroutines.flow.Flow
import net.gsantner.markor.domain.model.Document
import okio.Path

interface IDocumentRepository {
    suspend fun loadDocument(path: Path): Document?
    suspend fun saveDocument(document: Document, content: String): Boolean
    suspend fun createDocument(path: Path, content: String = ""): Document?
    suspend fun deleteDocument(path: Path): Boolean
    suspend fun renameDocument(document: Document, newName: String): Boolean
    fun observeDocument(path: Path): Flow<Document?>
    suspend fun documentExists(path: Path): Boolean
    suspend fun readContent(path: Path): String?
}
