package net.gsantner.markor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.gsantner.markor.data.local.AppSettings
import net.gsantner.markor.domain.repository.IFileRepository
import okio.Path.Companion.toPath
import net.gsantner.markor.ui.theme.ThemePaletteOption

enum class ThemeModeOption(val token: String) {
    AUTO("auto"),
    LIGHT("light"),
    DARK("dark")
}

class SettingsViewModel(
    private val appSettings: AppSettings,
    private val fileRepository: IFileRepository,
    private val sharedNotebookPath: String,
    private val privateNotebookPath: String
) : ViewModel() {

    val showLineNumbers = appSettings.isShowLineNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val wordWrap = appSettings.isWordWrap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoFormat = appSettings.isEditorAutoFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appTheme = appSettings.getAppTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "markor")

    val themeMode = appSettings.getAppTheme
        .map { parseThemeSelection(it).second }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeModeOption.AUTO)

    val themePalette = appSettings.getAppTheme
        .map { parseThemeSelection(it).first }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePaletteOption.DYNAMIC)

    // File Browser
    val fileBrowserShowHidden = appSettings.isFileBrowserShowHiddenFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val fileBrowserShowExt = appSettings.isFileBrowserShowFileExtensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val fileBrowserSortOrder = appSettings.getFileBrowserSortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "name")
    val fileBrowserFolderFirst = appSettings.isFileBrowserFolderFirst
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Editor
    val editorFontSize = appSettings.getEditorFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16)
    
    // Storage
    val notebookDirectory = appSettings.getNotebookDirectory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val isExternalStorageEnabled = appSettings.isExternalStorageEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val quickNotePath = appSettings.getQuickNoteFilePath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val todoFilePath = appSettings.getTodoFilePath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setShowLineNumbers(value: Boolean) {
        viewModelScope.launch { appSettings.setShowLineNumbers(value) }
    }

    fun setWordWrap(value: Boolean) {
        viewModelScope.launch { appSettings.setWordWrap(value) }
    }

    fun setAutoFormat(value: Boolean) {
        viewModelScope.launch { appSettings.setEditorAutoFormat(value) }
    }
    
    fun setFileBrowserShowHidden(value: Boolean) {
        viewModelScope.launch { appSettings.setFileBrowserShowHiddenFiles(value) }
    }

    fun setFileBrowserShowExt(value: Boolean) {
        viewModelScope.launch { appSettings.setFileBrowserShowFileExtensions(value) }
    }

    fun setFileBrowserSortOrder(value: String) {
        viewModelScope.launch { appSettings.setFileBrowserSortOrder(value) }
    }

    fun setFileBrowserFolderFirst(value: Boolean) {
        viewModelScope.launch { appSettings.setFileBrowserFolderFirst(value) }
    }
    
    fun setEditorFontSize(value: Int) {
        viewModelScope.launch { appSettings.setEditorFontSize(value) }
    }

    fun setNotebookDirectory(value: String) {
        viewModelScope.launch { appSettings.setNotebookDirectory(value) }
    }

    fun setQuickNotePath(value: String) {
        viewModelScope.launch { appSettings.setQuickNoteFilePath(value) }
    }

    fun setTodoFilePath(value: String) {
        viewModelScope.launch { appSettings.setTodoFilePath(value) }
    }

    fun setThemeMode(mode: ThemeModeOption) {
        viewModelScope.launch {
            val palette = themePalette.value
            appSettings.setAppTheme("${palette.token} ${mode.token}")
        }
    }

    fun setThemePalette(palette: ThemePaletteOption) {
        viewModelScope.launch {
            val mode = themeMode.value
            appSettings.setAppTheme("${palette.token} ${mode.token}")
        }
    }

    fun switchStorageMode(useSharedStorage: Boolean) {
        viewModelScope.launch {
            val notebookDir = if (useSharedStorage) sharedNotebookPath else privateNotebookPath
            val notebookPath = notebookDir.toPath()
            val parent = notebookPath.parent ?: notebookPath

            appSettings.setExternalStorageEnabled(useSharedStorage)
            appSettings.setNotebookDirectory(notebookDir)
            appSettings.setTodoFilePath("$notebookDir/todo.txt")
            appSettings.setQuickNoteFilePath("$notebookDir/quicknote.md")

            fileRepository.createDirectory(parent, notebookPath.name)
        }
    }

    private fun parseThemeSelection(rawValue: String): Pair<ThemePaletteOption, ThemeModeOption> {
        if (rawValue.isBlank()) return ThemePaletteOption.DYNAMIC to ThemeModeOption.AUTO

        var palette = ThemePaletteOption.DYNAMIC
        var mode = ThemeModeOption.AUTO
        val tokens = rawValue
            .trim()
            .lowercase()
            .split(Regex("[\\s_\\-|]+"))
            .filter { it.isNotBlank() }

        tokens.forEach { token ->
            ThemePaletteOption.fromToken(token)?.let {
                palette = it
            }

            when (token) {
                "light" -> mode = ThemeModeOption.LIGHT
                "dark" -> mode = ThemeModeOption.DARK
                "system", "auto" -> mode = ThemeModeOption.AUTO
            }
        }

        return palette to mode
    }
}
