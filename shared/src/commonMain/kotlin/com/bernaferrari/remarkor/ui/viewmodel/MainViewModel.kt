package com.bernaferrari.remarkor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class MainViewModel(
    private val settingsRepository: ISettingsRepository,
    @Named("default_notebook_path") private val defaultNotebookPath: String
) : ViewModel() {
    val notebookDirectory: StateFlow<String> = settingsRepository.getNotebookDirectory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val quickNotePath: StateFlow<String> = settingsRepository.getQuickNoteFilePath
        .map {
            it.ifEmpty {
                // Simple path concatenation for default
                val separator = if (defaultNotebookPath.endsWith("/")) "" else "/"
                "${defaultNotebookPath}${separator}quicknote.md"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val todoFilePath: StateFlow<String> = settingsRepository.getTodoFilePath
        .map {
            it.ifEmpty {
                val separator = if (defaultNotebookPath.endsWith("/")) "" else "/"
                "${defaultNotebookPath}${separator}todo.txt"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
}
