package com.bernaferrari.remarkor.domain.usecase

import com.bernaferrari.remarkor.domain.model.NoteLabel
import com.bernaferrari.remarkor.domain.model.NoteMetadata
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import com.bernaferrari.remarkor.domain.repository.INoteMetadataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import okio.Path
import okio.Path.Companion.toPath

class IndexNoteMetadataUseCaseTest {
    @Test
    fun invoke_indexesOnlySupportedExtensions() = runTest {
        val indexed = mutableListOf<String>()
        val useCase = IndexNoteMetadataUseCase(
            fileRepository = FakeFileRepository(),
            metadataRepository = FakeMetadataRepository(indexed),
        )

        useCase("/notes".toPath(), nowMillis = 1L)

        assertEquals(listOf("/notes/readme.md", "/notes/todo.txt"), indexed)
    }
}

private class FakeFileRepository : IFileRepository {
    override suspend fun listFilesRecursively(directory: Path): List<FileInfo> = listOf(
        file("readme.md"),
        file("photo.jpg"),
        file("todo.txt"),
    )

    override suspend fun readText(path: Path): String = "content:${path.name}"

    private fun file(name: String) = FileInfo(
        path = "/notes/$name".toPath(),
        name = name,
        isDirectory = false,
        size = 1,
        lastModified = 0,
        extension = name.substringAfterLast('.', ""),
    )

    override suspend fun listFiles(directory: Path): List<FileInfo> = emptyList()
    override suspend fun searchFiles(directory: Path, query: String, recursive: Boolean): List<FileInfo> = emptyList()
    override suspend fun searchContent(directory: Path, query: String, recursive: Boolean): List<FileInfo> = emptyList()
    override suspend fun getFileInfo(path: Path): FileInfo? = null
    override suspend fun createFile(parent: Path, name: String): Path? = null
    override suspend fun createFileWithContent(parent: Path, name: String, content: String): Path? = null
    override suspend fun createDirectory(parent: Path, name: String): Path? = null
    override suspend fun deleteFile(path: Path): Boolean = false
    override suspend fun moveToTrash(path: Path): Boolean = false
    override suspend fun restoreFromTrash(path: Path, originalPath: Path): Boolean = false
    override suspend fun emptyTrash(): Boolean = false
    override suspend fun listTrash(): List<FileInfo> = emptyList()
    override suspend fun getTrashPath(): Path = "/trash".toPath()
    override suspend fun renameFile(path: Path, newName: String): Path? = null
    override suspend fun copyFile(source: Path, destination: Path): Boolean = false
    override suspend fun moveFile(source: Path, destination: Path): Boolean = false
    override fun observeFiles(directory: Path): Flow<List<Path>> = flowOf(emptyList())
    override suspend fun isDirectory(path: Path): Boolean = false
    override suspend fun isFile(path: Path): Boolean = true
}

private class FakeMetadataRepository(
    private val indexed: MutableList<String>,
) : INoteMetadataRepository {
    override suspend fun upsertFromContent(path: String, content: String, nowMillis: Long) {
        indexed += path
    }

    override fun observeNotes(): Flow<List<NoteMetadata>> = flowOf(emptyList())
    override fun observeLabels(): Flow<List<NoteLabel>> = flowOf(emptyList())
    override suspend fun upsertFromPath(path: String, nowMillis: Long) = Unit
    override suspend fun getNoteByPath(path: String) = null
    override suspend fun updatePath(oldPath: String, newPath: String, nowMillis: Long) = Unit
    override suspend fun deleteByPath(path: String) = Unit
    override suspend fun deleteByPathRecursively(path: String) = Unit
    override suspend fun setLabelsForPath(path: String, labels: List<String>) = Unit
    override suspend fun setColor(path: String, color: Int?) = Unit
    override suspend fun setArchived(path: String, archived: Boolean) = Unit
    override suspend fun togglePinned(path: String, nowMillis: Long) = Unit
}