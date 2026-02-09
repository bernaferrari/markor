package net.gsantner.markor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.gsantner.markor.data.local.AppSettings
import net.gsantner.markor.data.local.db.NoteMetadataIndexer
import net.gsantner.markor.data.local.db.NoteMetadataRepository
import net.gsantner.markor.domain.repository.FavoritesRepository
import net.gsantner.markor.domain.repository.FileInfo
import net.gsantner.markor.domain.repository.IFileRepository
import okio.Path
import okio.Path.Companion.toPath
import kotlinx.datetime.Clock

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

    val noteMetadataByPath: StateFlow<Map<String, net.gsantner.markor.data.local.db.NoteWithLabels>> =
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
    val labels: StateFlow<List<net.gsantner.markor.data.local.db.LabelEntity>> = 
        noteMetadataRepository.observeLabels()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // File Type Filter
    private val _fileTypeFilter = MutableStateFlow(FileTypeFilter.ALL)
    val fileTypeFilter: StateFlow<FileTypeFilter> = _fileTypeFilter.asStateFlow()

    private var currentPathString: String? = null
    private var didRunIndexer = false

    suspend fun loadFiles(path: String?): String {
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
        
        currentPathString = targetPathString
        refreshFiles()


        if (!didRunIndexer) {
            didRunIndexer = true
            viewModelScope.launch {
                noteMetadataIndexer.indexDirectory(targetPathString.toPath(), Clock.System.now().toEpochMilliseconds())
            }
        }
        return targetPathString
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
            fileRepository.moveToTrash(path)
            noteMetadataRepository.deleteByPath(path.toString())
            refreshFiles()
        }
    }

    fun restoreFile(path: Path, originalPath: Path) {
        viewModelScope.launch {
            fileRepository.restoreFromTrash(path, originalPath)
            refreshFiles()
            loadTrashFiles()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            fileRepository.emptyTrash()
            loadTrashFiles()
        }
    }

    fun deleteSelectedFiles() {
        viewModelScope.launch {
            _selectedFiles.value.forEach { path ->
                fileRepository.moveToTrash(path)
                noteMetadataRepository.deleteByPath(path.toString())
            }
            clearSelection()
            refreshFiles()
        }
    }

    fun createNewFile(parent: Path, name: String = "NewFile_${kotlin.random.Random.nextInt(1000)}.md") {
        viewModelScope.launch {
            val created = fileRepository.createFile(parent, name)
            if (created != null) {
                noteMetadataRepository.upsertFromPath(created.toString(), System.currentTimeMillis())
            }
            refreshFiles()
        }
    }

    fun createNewFolder(parent: Path, name: String) {
        viewModelScope.launch {
             fileRepository.createDirectory(parent, name)
              refreshFiles()
        }
    }

    fun renameFile(path: Path, newName: String) {
        viewModelScope.launch {
            val success = fileRepository.renameFile(path, newName)
            if (success) {
                val newPath = (path.parent ?: ".".toPath()) / newName
                noteMetadataRepository.updatePath(
                    oldPath = path.toString(),
                    newPath = newPath.toString(),
                    nowMillis = System.currentTimeMillis()
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
                result = result + contentResults.filter { contentMatchingPaths.contains(it.path) && !nameMatchingPaths.contains(it.path) }
            }
        }
        
        return result.sortedWith(
            compareByDescending<FileInfo> { metadata[it.path.toString()]?.note?.pinned == true }
                .thenByDescending { it.lastModified }
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
            val now = Clock.System.now().toEpochMilliseconds()
            if (existing == null) {
                val note = net.gsantner.markor.data.local.db.NoteMetadataMapper
                    .buildNoteEntityFromPath(path.toString(), null, now)
                    .copy(pinned = true)
                noteMetadataRepository.upsertNote(note)
            } else {
                noteMetadataRepository.upsertNote(existing.copy(pinned = !existing.pinned, updatedAt = now))
            }
        }
    }

    fun setLabels(path: String, labels: List<String>) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            // Ensure note exists
            val existing = noteMetadataRepository.getNoteByPath(path)
            if (existing == null) {
                 val note = net.gsantner.markor.data.local.db.NoteMetadataMapper
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
}
