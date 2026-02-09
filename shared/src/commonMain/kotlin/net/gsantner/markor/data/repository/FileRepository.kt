package net.gsantner.markor.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.gsantner.markor.data.local.AppSettings
import net.gsantner.markor.domain.repository.FileInfo
import net.gsantner.markor.domain.repository.IFileRepository
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileRepository(
    private val appSettings: AppSettings
) : IFileRepository {

    private val fileSystem = FileSystem.SYSTEM

    private val trashDirectoryName = ".trash"

    override suspend fun getTrashPath(): Path {
        val notebookDir = appSettings.getNotebookDirectory.first()
        return if (notebookDir.isNotEmpty()) {
            notebookDir.toPath() / trashDirectoryName
        } else {
            ".trash".toPath()
        }
    }

    override suspend fun listFiles(directory: Path): List<FileInfo> = withContext(Dispatchers.IO) {
        try {
            if (!fileSystem.exists(directory)) return@withContext emptyList()
            val metadata = fileSystem.metadata(directory)
            if (!metadata.isDirectory) return@withContext emptyList()

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
                        "size" -> fileInfos.sortedByDescending { it.size }
                        else -> fileInfos
                    }
                }
        } catch (e: IOException) {
            emptyList()
        }
    }

    override suspend fun listFilesRecursively(directory: Path): List<FileInfo> = withContext(Dispatchers.IO) {
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

    override suspend fun searchFiles(directory: Path, query: String, recursive: Boolean): List<FileInfo> = withContext(Dispatchers.IO) {
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

    override suspend fun getFileInfo(path: Path): FileInfo? = withContext(Dispatchers.IO) {
        try {
            if (!fileSystem.exists(path)) return@withContext null
            val metadata = fileSystem.metadata(path)
            
            val name = path.name
            val extension = if (name.contains(".")) name.substringAfterLast(".") else ""
            
            val isTextFile = !metadata.isDirectory && (extension.lowercase() in setOf("md", "txt", "markdown", "org", "todo"))
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

    override suspend fun createFile(parent: Path, name: String): Path? = withContext(Dispatchers.IO) {
        try {
            if (!fileSystem.exists(parent)) {
                fileSystem.createDirectories(parent)
            }
            val filePath = parent / name
            fileSystem.write(filePath) {
            }
            filePath
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun createFileWithContent(parent: Path, name: String, content: String): Path? = withContext(Dispatchers.IO) {
        try {
            if (!fileSystem.exists(parent)) {
                fileSystem.createDirectories(parent)
            }
            val filePath = parent / name
            fileSystem.write(filePath) {
                writeUtf8(content)
            }
            filePath
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun createDirectory(parent: Path, name: String): Path? = withContext(Dispatchers.IO) {
        try {
            val dirPath = parent / name
            fileSystem.createDirectories(dirPath)
            dirPath
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun deleteFile(path: Path): Boolean = withContext(Dispatchers.IO) {
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

    override suspend fun moveToTrash(path: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            val trashPath = getTrashPath()
            if (!fileSystem.exists(trashPath)) {
                fileSystem.createDirectories(trashPath)
            }

            val fileName = path.name
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val trashFileName = "${timestamp}_$fileName"
            val destination = trashPath / trashFileName

            fileSystem.atomicMove(path, destination)
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun restoreFromTrash(path: Path, originalPath: Path): Boolean = withContext(Dispatchers.IO) {
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

    override suspend fun emptyTrash(): Boolean = withContext(Dispatchers.IO) {
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

    override suspend fun listTrash(): List<FileInfo> = withContext(Dispatchers.IO) {
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

    override suspend fun searchContent(directory: Path, query: String, recursive: Boolean): List<FileInfo> = withContext(Dispatchers.IO) {
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

    override suspend fun readText(path: Path): String = withContext(Dispatchers.IO) {
        try {
            fileSystem.read(path) {
                readUtf8()
            }
        } catch (e: IOException) {
            ""
        }
    }

    override suspend fun renameFile(path: Path, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dest = (path.parent ?: ".".toPath()) / newName
            fileSystem.atomicMove(path, dest)
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun copyFile(source: Path, destination: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            fileSystem.copy(source, destination)
            true
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun moveFile(source: Path, destination: Path): Boolean = withContext(Dispatchers.IO) {
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

    override suspend fun isDirectory(path: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            fileSystem.metadata(path).isDirectory
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun isFile(path: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            fileSystem.metadata(path).isRegularFile
        } catch (e: IOException) {
            false
        }
    }
}
