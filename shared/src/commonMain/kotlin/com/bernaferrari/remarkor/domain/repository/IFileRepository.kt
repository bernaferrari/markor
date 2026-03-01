package com.bernaferrari.remarkor.domain.repository

import kotlinx.coroutines.flow.Flow
import okio.Path

interface IFileRepository {
    suspend fun listFiles(directory: Path): List<FileInfo>
    suspend fun listFilesRecursively(directory: Path): List<FileInfo>
    suspend fun searchFiles(
        directory: Path,
        query: String,
        recursive: Boolean = true
    ): List<FileInfo>

    suspend fun searchContent(
        directory: Path,
        query: String,
        recursive: Boolean = true
    ): List<FileInfo>

    suspend fun getFileInfo(path: Path): FileInfo?
    suspend fun createFile(parent: Path, name: String): Path?
    suspend fun createFileWithContent(parent: Path, name: String, content: String): Path?
    suspend fun createDirectory(parent: Path, name: String): Path?
    suspend fun deleteFile(path: Path): Boolean
    suspend fun moveToTrash(path: Path): Boolean
    suspend fun restoreFromTrash(path: Path, originalPath: Path): Boolean
    suspend fun emptyTrash(): Boolean
    suspend fun listTrash(): List<FileInfo>
    suspend fun getTrashPath(): Path
    suspend fun renameFile(path: Path, newName: String): Path?
    suspend fun copyFile(source: Path, destination: Path): Boolean
    suspend fun moveFile(source: Path, destination: Path): Boolean
    fun observeFiles(directory: Path): Flow<List<Path>>
    suspend fun isDirectory(path: Path): Boolean
    suspend fun isFile(path: Path): Boolean
    suspend fun readText(path: Path): String
}

data class FileInfo(
    val path: Path,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val preview: String? = null
)
