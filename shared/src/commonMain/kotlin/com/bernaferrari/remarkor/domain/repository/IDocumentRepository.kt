package com.bernaferrari.remarkor.domain.repository

import com.bernaferrari.remarkor.domain.model.Document
import kotlinx.coroutines.flow.Flow
import okio.Path

interface IDocumentRepository {
    suspend fun loadDocument(path: Path): Document?
    suspend fun saveDocument(document: Document, content: String): Boolean
    suspend fun createDocument(path: Path, content: String = ""): Document?
    suspend fun deleteDocument(path: Path): Boolean
    suspend fun renameDocument(document: Document, newName: String): Path?
    fun observeDocument(path: Path): Flow<Document?>
    suspend fun documentExists(path: Path): Boolean
    suspend fun readContent(path: Path): String?
}
