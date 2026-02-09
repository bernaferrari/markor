package net.gsantner.markor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.gsantner.markor.data.local.AppSettings

class SettingsViewModel(
    private val appSettings: AppSettings
) : ViewModel() {

    val showLineNumbers = appSettings.isShowLineNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val wordWrap = appSettings.isWordWrap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoFormat = appSettings.isEditorAutoFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkMode = appSettings.getAppTheme // This might need mapping if it's a string
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "markor")

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
}
