package com.bernaferrari.remarkor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.remarkor.data.local.AppSettings
import com.bernaferrari.remarkor.data.local.db.NoteMetadataIndexer
import com.bernaferrari.remarkor.data.local.db.NoteMetadataRepository
import com.bernaferrari.remarkor.domain.repository.FavoritesRepository
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import com.bernaferrari.remarkor.ui.components.UserMessageManager
import com.bernaferrari.remarkor.util.nowMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.deleted_permanently
import markor.shared.generated.resources.failed_to_delete_permanently
import markor.shared.generated.resources.failed_to_delete_with_arg
import markor.shared.generated.resources.failed_to_empty_trash
import markor.shared.generated.resources.failed_to_empty_trash_with_arg
import markor.shared.generated.resources.failed_to_index_files
import markor.shared.generated.resources.failed_to_load_files
import markor.shared.generated.resources.failed_to_load_files_with_arg
import markor.shared.generated.resources.failed_to_move_to_trash
import markor.shared.generated.resources.failed_to_restore_file
import markor.shared.generated.resources.failed_to_restore_with_arg
import markor.shared.generated.resources.file_restored
import markor.shared.generated.resources.moved_to_trash
import markor.shared.generated.resources.some_items_not_deleted
import markor.shared.generated.resources.trash_emptied
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.getString

enum class FileFilterMode {
    ALL, FAVORITES, ARCHIVE, LABEL, TRASH
}

enum class FileTypeFilter(val displayName: String, val extensions: List<String>) {
    ALL("All", listOf("*")),
    MARKDOWN("MD", listOf("md", "markdown")),
    TEXT("TXT", listOf("txt")),
    TODO("Todo", listOf("todo")),
    CODE("Code", listOf("py", "js", "kt", "java", "cpp", "c", "h", "rs", "go", "rb"))
}

class FileBrowserViewModel(
    private val fileRepository: IFileRepository,
    private val appSettings: AppSettings,
    private val favoritesRepository: FavoritesRepository,
    private val noteMetadataRepository: NoteMetadataRepository,
    private val noteMetadataIndexer: NoteMetadataIndexer,
    private val defaultNotebookPath: String
) : ViewModel() {

    // User message management
    val messageManager = UserMessageManager()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _files = MutableStateFlow<List<FileInfo>>(emptyList())
    val files: StateFlow<List<FileInfo>> = _files.asStateFlow()

    private val _trashFiles = MutableStateFlow<List<FileInfo>>(emptyList())
    val trashFiles: StateFlow<List<FileInfo>> = _trashFiles.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<Path>>(emptySet())
    val selectedFiles: StateFlow<Set<Path>> = _selectedFiles.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search in content flag
    private val _searchInContent = MutableStateFlow(false)
    val searchInContent: StateFlow<Boolean> = _searchInContent.asStateFlow()

    // Content search results
    private val _contentSearchResults = MutableStateFlow<List<FileInfo>>(emptyList())
    val contentSearchResults: StateFlow<List<FileInfo>> = _contentSearchResults.asStateFlow()

    // Favorites
    val favorites: StateFlow<Set<String>> = favoritesRepository.favorites
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    // Recent Files
    val recentFiles: StateFlow<List<String>> = favoritesRepository.recentFiles
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sortOrder: StateFlow<String> = appSettings.getFileBrowserSortOrder
        .stateIn(viewModelScope, SharingStarted.Lazily, "date")

    val noteMetadataByPath: StateFlow<Map<String, com.bernaferrari.remarkor.data.local.db.NoteWithLabels>> =
        noteMetadataRepository.observeNotes()
            .map { notes -> notes.associateBy { it.note.path } }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Filter Mode
    private val _filterMode = MutableStateFlow(FileFilterMode.ALL)
    val filterMode: StateFlow<FileFilterMode> = _filterMode.asStateFlow()

    // Label Filter
    private val _currentLabel = MutableStateFlow<String?>(null)
    val currentLabel: StateFlow<String?> = _currentLabel.asStateFlow()

    // Labels List
    val labels: StateFlow<List<com.bernaferrari.remarkor.data.local.db.LabelEntity>> =
        noteMetadataRepository.observeLabels()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // File Type Filter
    private val _fileTypeFilter = MutableStateFlow(FileTypeFilter.ALL)
    val fileTypeFilter: StateFlow<FileTypeFilter> = _fileTypeFilter.asStateFlow()

    private var currentPathString: String? = null
    private var didRunIndexer = false

    suspend fun loadFiles(path: String?): String {
        _isLoading.value = true
        _errorMessage.value = null

        val targetPathString = if (path.isNullOrEmpty()) {
            val notebookDir = appSettings.getNotebookDirectory.first()
            if (notebookDir.isEmpty()) {
                defaultNotebookPath
            } else {
                notebookDir
            }
        } else {
            path
        }

        val resolvedPathString = resolveNotebookPath(targetPathString)
        currentPathString = resolvedPathString

        try {
            refreshFiles()
        } catch (e: Exception) {
            _errorMessage.value =
                getString(Res.string.failed_to_load_files_with_arg, e.message ?: "")
            messageManager.error(getString(Res.string.failed_to_load_files))
        } finally {
            _isLoading.value = false
        }

        if (!didRunIndexer) {
            didRunIndexer = true
            viewModelScope.launch {
                try {
                    noteMetadataIndexer.indexDirectory(resolvedPathString.toPath(), nowMillis())
                } catch (e: Exception) {
                    messageManager.error(getString(Res.string.failed_to_index_files))
                }
            }
        }
        return resolvedPathString
    }

    private suspend fun resolveNotebookPath(pathString: String): String {
        val path = pathString.toPath()
        val parent = path.parent

        if (fileRepository.isDirectory(path)) {
            return pathString
        }

        // Try creating the target folder if parent is accessible.
        if (parent != null && fileRepository.isDirectory(parent)) {
            fileRepository.createDirectory(parent, path.name)
            if (fileRepository.isDirectory(path)) {
                return pathString
            }
        }

        return pathString
    }

    private suspend fun refreshFiles() {
        val path = currentPathString?.toPath() ?: return

        if (_filterMode.value == FileFilterMode.TRASH) {
            _trashFiles.value = fileRepository.listTrash()
            _files.value = emptyList()
        } else {
            _files.value = fileRepository.listFiles(path)
            _trashFiles.value = emptyList()
        }
    }

    suspend fun loadTrashFiles() {
        _trashFiles.value = fileRepository.listTrash()
    }

    fun deleteFile(path: Path) {
        viewModelScope.launch {
            try {
                val pathString = path.toString()
                val isTrashMode = _filterMode.value == FileFilterMode.TRASH
                val isDirectory = fileRepository.isDirectory(path)

                if (isTrashMode) {
                    // Permanent delete from trash should remove instantly from UI.
                    _trashFiles.value = _trashFiles.value.filterNot { it.path == path }
                    val success = fileRepository.deleteFile(path)
                    if (success) {
                        messageManager.success(getString(Res.string.deleted_permanently))
                    } else {
                        messageManager.error(getString(Res.string.failed_to_delete_permanently))
                    }
                } else {
                    // Optimistic update: hide entry immediately while move runs.
                    _files.value = _files.value.filterNot { it.path == path }
                    val success = fileRepository.moveToTrash(path)
                    if (success) {
                        if (isDirectory) {
                            noteMetadataRepository.deleteByPathRecursively(pathString)
                        } else {
                            noteMetadataRepository.deleteByPath(pathString)
                        }
                        messageManager.success(getString(Res.string.moved_to_trash))
                    } else {
                        messageManager.error(getString(Res.string.failed_to_move_to_trash))
                    }
                }
                refreshFiles()
            } catch (e: Exception) {
                messageManager.error(
                    getString(
                        Res.string.failed_to_delete_with_arg,
                        e.message ?: ""
                    )
                )
            }
        }
    }

    fun restoreFile(path: Path, originalPath: Path) {
        viewModelScope.launch {
            try {
                val success = fileRepository.restoreFromTrash(path, originalPath)
                if (success) {
                    messageManager.success(getString(Res.string.file_restored))
                } else {
                    messageManager.error(getString(Res.string.failed_to_restore_file))
                }
                refreshFiles()
                loadTrashFiles()
            } catch (e: Exception) {
                messageManager.error(
                    getString(
                        Res.string.failed_to_restore_with_arg,
                        e.message ?: ""
                    )
                )
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            try {
                val success = fileRepository.emptyTrash()
                if (success) {
                    messageManager.success(getString(Res.string.trash_emptied))
                } else {
                    messageManager.error(getString(Res.string.failed_to_empty_trash))
                }
                loadTrashFiles()
            } catch (e: Exception) {
                messageManager.error(
                    getString(
                        Res.string.failed_to_empty_trash_with_arg,
                        e.message ?: ""
                    )
                )
            }
        }
    }

    fun deleteSelectedFiles() {
        viewModelScope.launch {
            val selected = _selectedFiles.value.toList()
            if (selected.isEmpty()) {
                clearSelection()
                return@launch
            }

            val isTrashMode = _filterMode.value == FileFilterMode.TRASH
            var failures = 0

            if (isTrashMode) {
                // Permanent delete in trash mode.
                val selectedSet = selected.toSet()
                _trashFiles.value = _trashFiles.value.filterNot { it.path in selectedSet }
                selected.forEach { path ->
                    val success = fileRepository.deleteFile(path)
                    if (!success) failures++
                }
            } else {
                // Move to trash in regular mode.
                val selectedSet = selected.toSet()
                _files.value = _files.value.filterNot { it.path in selectedSet }
                selected.forEach { path ->
                    val isDirectory = fileRepository.isDirectory(path)
                    val success = fileRepository.moveToTrash(path)
                    if (success) {
                        if (isDirectory) {
                            noteMetadataRepository.deleteByPathRecursively(path.toString())
                        } else {
                            noteMetadataRepository.deleteByPath(path.toString())
                        }
                    } else {
                        failures++
                    }
                }
            }

            clearSelection()
            refreshFiles()

            if (failures > 0) {
                messageManager.error(getString(Res.string.some_items_not_deleted))
            } else if (isTrashMode) {
                messageManager.success(getString(Res.string.deleted_permanently))
            } else {
                messageManager.success(getString(Res.string.moved_to_trash))
            }
        }
    }

    fun createNewFile(
        parent: Path,
        name: String = "NewFile_${kotlin.random.Random.nextInt(1000)}.md",
        onCreated: ((Path) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (currentPathString == null) {
                currentPathString = parent.toString()
            }
            val created = fileRepository.createFile(parent, name)
            if (created != null) {
                noteMetadataRepository.upsertFromPath(created.toString(), nowMillis())
                onCreated?.invoke(created)
            }
            refreshFiles()
        }
    }

    fun createNewFolder(parent: Path, name: String) {
        viewModelScope.launch {
            if (currentPathString == null) {
                currentPathString = parent.toString()
            }
            fileRepository.createDirectory(parent, name)
            refreshFiles()
        }
    }

    fun renameFile(path: Path, newName: String) {
        viewModelScope.launch {
            val renamedPath = fileRepository.renameFile(path, newName)
            if (renamedPath != null) {
                noteMetadataRepository.updatePath(
                    oldPath = path.toString(),
                    newPath = renamedPath.toString(),
                    nowMillis = nowMillis()
                )
            }
            refreshFiles()
        }
    }

    fun toggleSelection(path: Path) {
        val currentSelection = _selectedFiles.value.toMutableSet()
        if (currentSelection.contains(path)) {
            currentSelection.remove(path)
        } else {
            currentSelection.add(path)
        }
        _selectedFiles.value = currentSelection

        if (currentSelection.isEmpty()) {
            _isSelectionMode.value = false
        } else {
            _isSelectionMode.value = true
        }
    }

    fun enterSelectionMode(initialPath: Path) {
        _isSelectionMode.value = true
        _selectedFiles.value = setOf(initialPath)
    }

    fun selectAll() {
        val allPaths = _files.value.map { it.path }.toSet()
        _selectedFiles.value = allPaths
    }

    fun toggleSelectAll() {
        val allPaths = if (_filterMode.value == FileFilterMode.TRASH) {
            _trashFiles.value.map { it.path }.toSet()
        } else {
            _files.value.map { it.path }.toSet()
        }

        val allAreAlreadySelected = allPaths.isNotEmpty() &&
                _selectedFiles.value.size == allPaths.size &&
                _selectedFiles.value.containsAll(allPaths)

        if (allAreAlreadySelected) {
            clearSelection()
        } else {
            _selectedFiles.value = allPaths
            _isSelectionMode.value = allPaths.isNotEmpty()
        }
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
        _isSelectionMode.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Trigger content search if enabled
        if (_searchInContent.value && query.isNotBlank()) {
            viewModelScope.launch {
                val path = currentPathString?.toPath() ?: return@launch
                _contentSearchResults.value = fileRepository.searchContent(path, query)
            }
        } else if (query.isBlank()) {
            _contentSearchResults.value = emptyList()
        }
    }

    // Favorites operations
    fun toggleFavorite(path: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(path)
        }
    }

    fun isFavorite(path: String): Boolean {
        return favorites.value.contains(path)
    }

    // Recent files operations
    fun recordFileAccess(path: String) {
        viewModelScope.launch {
            favoritesRepository.recordFileAccess(path)
        }
    }

    // Filter mode
    fun setFilterMode(mode: FileFilterMode) {
        _filterMode.value = mode
        viewModelScope.launch {
            refreshFiles()
        }
    }

    fun setSortOrder(order: String) {
        viewModelScope.launch {
            appSettings.setFileBrowserSortOrder(order)
            refreshFiles()
        }
    }

    // File type filter
    fun setFileTypeFilter(filter: FileTypeFilter) {
        _fileTypeFilter.value = filter
    }

    fun setLabelFilter(label: String) {
        _currentLabel.value = label
        setFilterMode(FileFilterMode.LABEL)
    }

    // Search in content
    fun setSearchInContent(enabled: Boolean) {
        _searchInContent.value = enabled
        // Trigger content search if enabling
        if (enabled && _searchQuery.value.isNotBlank()) {
            viewModelScope.launch {
                val path = currentPathString?.toPath() ?: return@launch
                _contentSearchResults.value = fileRepository.searchContent(path, _searchQuery.value)
            }
        } else if (!enabled) {
            _contentSearchResults.value = emptyList()
        }
    }

    /**
     * Returns files filtered by the current search query and filter mode.
     * Matches filename (case-insensitive) or content if searchInContent is enabled.
     */
    fun getFilteredFiles(): List<FileInfo> {
        val query = _searchQuery.value
        val mode = _filterMode.value
        val typeFilter = _fileTypeFilter.value
        val searchInContent = _searchInContent.value
        val contentResults = _contentSearchResults.value
        val favs = favorites.value
        val metadata = noteMetadataByPath.value

        // For trash mode, return trash files directly
        if (mode == FileFilterMode.TRASH) {
            var result = _trashFiles.value
            if (query.isNotBlank()) {
                result = result.filter { it.name.contains(query, ignoreCase = true) }
            }
            return result
        }

        var result = _files.value

        // Filter by Archive Status
        if (mode == FileFilterMode.ARCHIVE) {
            result = result.filter { file ->
                metadata[file.path.toString()]?.note?.isArchived == true
            }
        } else if (mode == FileFilterMode.LABEL) {
            // Filter by Label
            val label = _currentLabel.value
            if (label != null) {
                result = result.filter { file ->
                    val noteWithLabels = metadata[file.path.toString()]
                    noteWithLabels?.labels?.any { it.name == label } == true && noteWithLabels.note.isArchived != true
                }
            }
        } else {
            // In ALL or FAVORITES, hide archived files
            result = result.filter { file ->
                metadata[file.path.toString()]?.note?.isArchived != true
            }
        }

        // Apply favorites filter
        if (mode == FileFilterMode.FAVORITES) {
            result = result.filter { favs.contains(it.path.toString()) }
        }

        // Apply file type filter
        if (typeFilter != FileTypeFilter.ALL) {
            result = result.filter { file ->
                val ext = file.extension.lowercase()
                typeFilter.extensions.contains(ext)
            }
        }

        // Apply search filter (filename or content)
        if (query.isNotBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }

            // Add content matches if searching in content
            if (searchInContent && contentResults.isNotEmpty()) {
                val contentMatchingPaths = contentResults.map { it.path }.toSet()
                val nameMatchingPaths = result.map { it.path }.toSet()
                // Add content matches that aren't already in name matches
                result = result + contentResults.filter {
                    contentMatchingPaths.contains(it.path) && !nameMatchingPaths.contains(it.path)
                }
            }
        }

        val contentSortComparator = when (sortOrder.value) {
            "name" -> compareBy<FileInfo> { it.name.lowercase() }
            "size" -> compareByDescending<FileInfo> { it.size }
            "oldest" -> compareBy<FileInfo> { it.lastModified }
            else -> compareByDescending<FileInfo> { it.lastModified } // "date" => recent first
        }

        return result.sortedWith(
            compareByDescending<FileInfo> { metadata[it.path.toString()]?.note?.pinned == true }
                .then(contentSortComparator)
        )
    }

    /**
     * Returns files matching the search query in content.
     */
    suspend fun searchInContent(query: String): List<FileInfo> {
        val path = currentPathString?.toPath() ?: return emptyList()
        return fileRepository.searchContent(path, query)
    }

    fun togglePin(path: Path) {
        viewModelScope.launch {
            val existing = noteMetadataRepository.getNoteByPath(path.toString())
            val now = nowMillis()
            if (existing == null) {
                val note = com.bernaferrari.remarkor.data.local.db.NoteMetadataMapper
                    .buildNoteEntityFromPath(path.toString(), null, now)
                    .copy(pinned = true)
                noteMetadataRepository.upsertNote(note)
            } else {
                noteMetadataRepository.upsertNote(
                    existing.copy(
                        pinned = !existing.pinned,
                        updatedAt = now
                    )
                )
            }
        }
    }

    fun setLabels(path: String, labels: List<String>) {
        viewModelScope.launch {
            val now = nowMillis()
            // Ensure note exists
            val existing = noteMetadataRepository.getNoteByPath(path)
            if (existing == null) {
                val note = com.bernaferrari.remarkor.data.local.db.NoteMetadataMapper
                    .buildNoteEntityFromPath(path, null, now)
                noteMetadataRepository.upsertNote(note)
                // Need the ID now
                val inserted = noteMetadataRepository.getNoteByPath(path)
                if (inserted != null) {
                    noteMetadataRepository.setLabelsForNote(inserted.id, labels)
                }
            } else {
                noteMetadataRepository.setLabelsForNote(existing.id, labels)
            }
        }
    }

    fun setColorForSelectedFiles(color: Int?) {
        viewModelScope.launch {
            val selected = _selectedFiles.value.toList()
            if (selected.isEmpty()) {
                clearSelection()
                return@launch
            }

            val now = nowMillis()
            selected.forEach { path ->
                // Note color applies to notes only, not directories.
                if (fileRepository.isDirectory(path)) {
                    return@forEach
                }

                val pathString = path.toString()
                val existing = noteMetadataRepository.getNoteByPath(pathString)
                if (existing == null) {
                    val note = com.bernaferrari.remarkor.data.local.db.NoteMetadataMapper
                        .buildNoteEntityFromPath(pathString, null, now)
                        .copy(color = color, updatedAt = now)
                    noteMetadataRepository.upsertNote(note)
                } else {
                    noteMetadataRepository.upsertNote(existing.copy(color = color, updatedAt = now))
                }
            }

            clearSelection()
        }
    }
}
