package com.bernaferrari.remarkor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import com.bernaferrari.remarkor.ui.theme.ThemePaletteOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

enum class ThemeModeOption(val token: String) {
    AUTO("auto"),
    LIGHT("light"),
    DARK("dark")
}

@KoinViewModel
class SettingsViewModel(
    private val settingsRepository: ISettingsRepository,
    private val fileRepository: IFileRepository,
    @Named("default_notebook_path") private val sharedNotebookPath: String,
    @Named("internal_notebook_path") private val privateNotebookPath: String
) : ViewModel() {

    val showLineNumbers = settingsRepository.isShowLineNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val wordWrap = settingsRepository.isWordWrap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoFormat = settingsRepository.isEditorAutoFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appTheme = settingsRepository.getAppTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "markor")

    val themeMode = settingsRepository.getAppTheme
        .map { parseThemeSelection(it).second }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeModeOption.AUTO)

    val themePalette = settingsRepository.getAppTheme
        .map { parseThemeSelection(it).first }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePaletteOption.DYNAMIC)

    // File Browser
    val fileBrowserShowHidden = settingsRepository.isFileBrowserShowHiddenFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val fileBrowserShowExt = settingsRepository.isFileBrowserShowFileExtensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val fileBrowserSortOrder = settingsRepository.getFileBrowserSortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "name")
    val fileBrowserFolderFirst = settingsRepository.isFileBrowserFolderFirst
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Editor
    val editorFontSize = settingsRepository.getEditorFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16)

    // Storage
    val notebookDirectory = settingsRepository.getNotebookDirectory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val isExternalStorageEnabled = settingsRepository.isExternalStorageEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val quickNotePath = settingsRepository.getQuickNoteFilePath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val todoFilePath = settingsRepository.getTodoFilePath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setShowLineNumbers(value: Boolean) {
        viewModelScope.launch { settingsRepository.setShowLineNumbers(value) }
    }

    fun setWordWrap(value: Boolean) {
        viewModelScope.launch { settingsRepository.setWordWrap(value) }
    }

    fun setAutoFormat(value: Boolean) {
        viewModelScope.launch { settingsRepository.setEditorAutoFormat(value) }
    }

    fun setFileBrowserShowHidden(value: Boolean) {
        viewModelScope.launch { settingsRepository.setFileBrowserShowHiddenFiles(value) }
    }

    fun setFileBrowserShowExt(value: Boolean) {
        viewModelScope.launch { settingsRepository.setFileBrowserShowFileExtensions(value) }
    }

    fun setFileBrowserSortOrder(value: String) {
        viewModelScope.launch { settingsRepository.setFileBrowserSortOrder(value) }
    }

    fun setFileBrowserFolderFirst(value: Boolean) {
        viewModelScope.launch { settingsRepository.setFileBrowserFolderFirst(value) }
    }

    fun setEditorFontSize(value: Int) {
        viewModelScope.launch { settingsRepository.setEditorFontSize(value) }
    }

    fun setNotebookDirectory(value: String) {
        viewModelScope.launch { settingsRepository.setNotebookDirectory(value) }
    }

    fun setQuickNotePath(value: String) {
        viewModelScope.launch { settingsRepository.setQuickNoteFilePath(value) }
    }

    fun setTodoFilePath(value: String) {
        viewModelScope.launch { settingsRepository.setTodoFilePath(value) }
    }

    fun setThemeMode(mode: ThemeModeOption) {
        viewModelScope.launch {
            val palette = themePalette.value
            settingsRepository.setAppTheme("${palette.token} ${mode.token}")
        }
    }

    fun setThemePalette(palette: ThemePaletteOption) {
        viewModelScope.launch {
            val mode = themeMode.value
            settingsRepository.setAppTheme("${palette.token} ${mode.token}")
        }
    }

    fun switchStorageMode(useSharedStorage: Boolean) {
        viewModelScope.launch {
            val notebookDir = if (useSharedStorage) sharedNotebookPath else privateNotebookPath
            val notebookPath = notebookDir.toPath()
            val parent = notebookPath.parent ?: notebookPath

            settingsRepository.setExternalStorageEnabled(useSharedStorage)
            settingsRepository.setNotebookDirectory(notebookDir)
            settingsRepository.setTodoFilePath("$notebookDir/todo.txt")
            settingsRepository.setQuickNoteFilePath("$notebookDir/quicknote.md")

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
