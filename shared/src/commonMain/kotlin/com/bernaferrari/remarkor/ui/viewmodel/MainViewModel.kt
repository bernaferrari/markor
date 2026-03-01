package com.bernaferrari.remarkor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.remarkor.data.local.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val appSettings: AppSettings,
    private val defaultNotebookPath: String
) : ViewModel() {
    val notebookDirectory: StateFlow<String> = appSettings.getNotebookDirectory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val quickNotePath: StateFlow<String> = appSettings.getQuickNoteFilePath
        .map {
            it.ifEmpty {
                // Simple path concatenation for default
                val separator = if (defaultNotebookPath.endsWith("/")) "" else "/"
                "${defaultNotebookPath}${separator}quicknote.md"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val todoFilePath: StateFlow<String> = appSettings.getTodoFilePath
        .map {
            it.ifEmpty {
                val separator = if (defaultNotebookPath.endsWith("/")) "" else "/"
                "${defaultNotebookPath}${separator}todo.txt"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
}
