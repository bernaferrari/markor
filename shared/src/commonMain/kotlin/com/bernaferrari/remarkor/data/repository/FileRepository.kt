package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.data.local.AppSettings
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import com.bernaferrari.remarkor.util.nowMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.directory_does_not_exist_with_arg
import markor.shared.generated.resources.failed_to_list_files_with_arg
import markor.shared.generated.resources.not_a_directory_with_arg
import markor.shared.generated.resources.permission_denied_with_arg
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.jetbrains.compose.resources.getString

class FileRepository(
    private val appSettings: AppSettings
) : IFileRepository {

    private val fileSystem = FileSystem.Companion.SYSTEM

    private val trashDirectoryName = ".trash"

    // Error tracking
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: Flow<String?> = _lastError

    private fun setError(message: String) {
        _lastError.value = message
    }

    private fun clearError() {
        _lastError.value = null
    }

    override suspend fun getTrashPath(): Path {
        val notebookDir = appSettings.getNotebookDirectory.first()
        return if (notebookDir.isNotEmpty()) {
            notebookDir.toPath() / trashDirectoryName
        } else {
            ".trash".toPath()
        }
    }

    override suspend fun listFiles(directory: Path): List<FileInfo> =
        withContext(Dispatchers.Default) {
            try {
                clearError()
                if (!fileSystem.exists(directory)) {
                    setError(
                        getString(
                            Res.string.directory_does_not_exist_with_arg,
                            directory.name
                        )
                    )
                    return@withContext emptyList()
                }
                val metadata = fileSystem.metadata(directory)
                if (!metadata.isDirectory) {
                    setError(getString(Res.string.not_a_directory_with_arg, directory.name))
                    return@withContext emptyList()
                }

                val showHidden = appSettings.isFileBrowserShowHiddenFiles.first()
                val sortOrder = appSettings.getFileBrowserSortOrder.first()

                val files = fileSystem.list(directory)

                files
                    .filter { path ->
                        val name = path.name
                        showHidden || !name.startsWith(".")
                    }
                    .mapNotNull { path ->
                        getFileInfo(path)
                    }
                    .let { fileInfos ->
                        when (sortOrder) {
                            "name" -> fileInfos.sortedWith(
                                compareBy<FileInfo> { !it.isDirectory }.thenBy { it.name.lowercase() }
                            )

                            "date" -> fileInfos.sortedByDescending { it.lastModified }
                            "oldest" -> fileInfos.sortedBy { it.lastModified }
                            "size" -> fileInfos.sortedByDescending { it.size }
                            else -> fileInfos
                        }
                    }
            } catch (e: IOException) {
                setError(getString(Res.string.failed_to_list_files_with_arg, e.message ?: ""))
                emptyList()
            } catch (e: Exception) {
                setError(getString(Res.string.permission_denied_with_arg, directory.name))
                emptyList()
            }
        }

    override suspend fun listFilesRecursively(directory: Path): List<FileInfo> =
        withContext(Dispatchers.Default) {
            try {
                fileSystem.listRecursively(directory).toList()
                    .filter { path ->
                        val metadata = fileSystem.metadata(path)
                        metadata.isRegularFile
                    }
                    .mapNotNull { getFileInfo(it) }
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun searchFiles(
        directory: Path,
        query: String,
        recursive: Boolean
    ): List<FileInfo> = withContext(Dispatchers.Default) {
        try {
            val fileList = if (recursive) {
                fileSystem.listRecursively(directory).toList()
            } else {
                fileSystem.list(directory)
            }

            fileList.filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isRegularFile && path.name.contains(query, ignoreCase = true)
            }.mapNotNull { getFileInfo(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFileInfo(path: Path): FileInfo? = withContext(Dispatchers.Default) {
        try {
            if (!fileSystem.exists(path)) return@withContext null
            val metadata = fileSystem.metadata(path)

            val name = path.name
            val extension = if (name.contains(".")) name.substringAfterLast(".") else ""

            val isTextFile = !metadata.isDirectory && (extension.lowercase() in setOf(
                "md",
                "txt",
                "markdown",
                "org",
                "todo"
            ))
            val preview = if (isTextFile) {
                try {
                    fileSystem.read(path) {
                        val limit = 1500L
                        val available = if (request(limit)) limit else buffer.size
                        readUtf8(available)
                    }
                } catch (e: Exception) {
                    null
                }
            } else null

            FileInfo(
                path = path,
                name = name,
                isDirectory = metadata.isDirectory,
                size = metadata.size ?: 0L,
                lastModified = metadata.lastModifiedAtMillis ?: 0L,
                extension = extension,
                preview = preview
            )
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun createFile(parent: Path, name: String): Path? =
        withContext(Dispatchers.Default) {
            try {
                if (!fileSystem.exists(parent)) {
                    fileSystem.createDirectories(parent)
                }
                val filePath = resolveUniquePath(parent, name)
                fileSystem.write(filePath) {
                }
                filePath
            } catch (e: IOException) {
                null
            }
        }

    override suspend fun createFileWithContent(parent: Path, name: String, content: String): Path? =
        withContext(Dispatchers.Default) {
            try {
                if (!fileSystem.exists(parent)) {
                    fileSystem.createDirectories(parent)
                }
                val filePath = resolveUniquePath(parent, name)
                fileSystem.write(filePath) {
                    writeUtf8(content)
                }
                filePath
            } catch (e: IOException) {
                null
            }
        }

    override suspend fun createDirectory(parent: Path, name: String): Path? =
        withContext(Dispatchers.Default) {
            try {
                val dirPath = resolveUniquePath(parent, name)
                fileSystem.createDirectories(dirPath)
                dirPath
            } catch (e: IOException) {
                null
            }
        }

    override suspend fun deleteFile(path: Path): Boolean = withContext(Dispatchers.Default) {
        try {
            val metadata = fileSystem.metadata(path)
            if (metadata.isDirectory) {
                fileSystem.deleteRecursively(path)
            } else {
                fileSystem.delete(path)
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun moveToTrash(path: Path): Boolean = withContext(Dispatchers.Default) {
        try {
            val trashPath = getTrashPath()
            if (!fileSystem.exists(trashPath)) {
                fileSystem.createDirectories(trashPath)
            }

            val fileName = path.name
            val timestamp = formatTimestampForTrash(nowMillis())
            val trashFileName = "${timestamp}_$fileName"
            val destination = trashPath / trashFileName

            fileSystem.atomicMove(path, destination)
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun formatTimestampForTrash(epochMillis: Long): String {
        val datetime = kotlin.time.Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return "${datetime.year}" +
                "${(datetime.month.ordinal + 1).toString().padStart(2, '0')}" +
                "${datetime.day.toString().padStart(2, '0')}_" +
                "${datetime.hour.toString().padStart(2, '0')}" +
                "${datetime.minute.toString().padStart(2, '0')}" +
                "${datetime.second.toString().padStart(2, '0')}"
    }

    override suspend fun restoreFromTrash(path: Path, originalPath: Path): Boolean =
        withContext(Dispatchers.Default) {
            try {
                // Create parent directories if needed
                val parent = originalPath.parent
                if (parent != null && !fileSystem.exists(parent)) {
                    fileSystem.createDirectories(parent)
                }
                fileSystem.atomicMove(path, originalPath)
                true
            } catch (e: IOException) {
                false
            }
        }

    override suspend fun emptyTrash(): Boolean = withContext(Dispatchers.Default) {
        try {
            val trashPath = getTrashPath()
            if (fileSystem.exists(trashPath)) {
                fileSystem.deleteRecursively(trashPath)
                fileSystem.createDirectories(trashPath)
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun listTrash(): List<FileInfo> = withContext(Dispatchers.Default) {
        try {
            val trashPath = getTrashPath()
            if (!fileSystem.exists(trashPath)) return@withContext emptyList()

            fileSystem.list(trashPath)
                .filter { path ->
                    val name = path.name
                    !name.startsWith(".") && name.contains("_")
                }
                .mapNotNull { path ->
                    getFileInfo(path)
                }
                .sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchContent(
        directory: Path,
        query: String,
        recursive: Boolean
    ): List<FileInfo> = withContext(Dispatchers.Default) {
        try {
            val fileList = if (recursive) {
                fileSystem.listRecursively(directory).toList()
            } else {
                fileSystem.list(directory)
            }

            fileList.filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isRegularFile && isTextFile(path)
            }.filter { path ->
                try {
                    val content = fileSystem.read(path) {
                        readUtf8()
                    }
                    content.contains(query, ignoreCase = true)
                } catch (e: Exception) {
                    false
                }
            }.mapNotNull { getFileInfo(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isTextFile(path: Path): Boolean {
        val extension = path.name.substringAfterLast(".", "").lowercase()
        return extension in setOf("md", "txt", "markdown", "org", "todo", " rst", "adoc")
    }

    override suspend fun readText(path: Path): String = withContext(Dispatchers.Default) {
        try {
            fileSystem.read(path) {
                readUtf8()
            }
        } catch (e: IOException) {
            ""
        }
    }

    override suspend fun renameFile(path: Path, newName: String): Path? =
        withContext(Dispatchers.Default) {
            try {
                val parent = path.parent ?: ".".toPath()
                val dest = resolveUniquePath(parent, newName, excludePath = path)
                if (dest == path) return@withContext path
                fileSystem.atomicMove(path, dest)
                dest
            } catch (e: IOException) {
                null
            }
        }

    override suspend fun copyFile(source: Path, destination: Path): Boolean =
        withContext(Dispatchers.Default) {
            try {
                fileSystem.copy(source, destination)
                true
            } catch (e: IOException) {
                false
            }
        }

    override suspend fun moveFile(source: Path, destination: Path): Boolean =
        withContext(Dispatchers.Default) {
            try {
                fileSystem.atomicMove(source, destination)
                true
            } catch (e: IOException) {
                false
            }
        }

    override fun observeFiles(directory: Path): Flow<List<Path>> {
        return MutableStateFlow(emptyList())
    }

    override suspend fun isDirectory(path: Path): Boolean = withContext(Dispatchers.Default) {
        try {
            fileSystem.metadata(path).isDirectory
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun isFile(path: Path): Boolean = withContext(Dispatchers.Default) {
        try {
            fileSystem.metadata(path).isRegularFile
        } catch (e: IOException) {
            false
        }
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
