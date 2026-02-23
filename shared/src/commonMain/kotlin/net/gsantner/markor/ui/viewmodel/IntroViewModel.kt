package net.gsantner.markor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.gsantner.markor.data.local.AppSettings
import net.gsantner.markor.domain.repository.FavoritesRepository
import net.gsantner.markor.domain.repository.IFileRepository
import okio.Path
import okio.Path.Companion.toPath

class IntroViewModel(
    private val appSettings: AppSettings,
    private val fileRepository: IFileRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val isFirstRun = appSettings.isFirstRun

    suspend fun setStorageMode(isExternal: Boolean, internalPath: String) {
        appSettings.setExternalStorageEnabled(isExternal)

        val notebookDir = if (isExternal) {
            "/storage/emulated/0/Documents/Markor"
        } else {
            "$internalPath/Notebook"
        }
        val todoPath = "$notebookDir/todo.txt"
        val quicknotePath = "$notebookDir/quicknote.md"

        appSettings.setNotebookDirectory(notebookDir)
        appSettings.setTodoFilePath(todoPath)
        appSettings.setQuickNoteFilePath(quicknotePath)

        initializeDefaultFiles(notebookDir.toPath(), todoPath, quicknotePath)
    }

    private suspend fun initializeDefaultFiles(notebookDir: Path, todoPath: String, quicknotePath: String) {
        val notebookParent = notebookDir.parent ?: notebookDir
        fileRepository.createDirectory(notebookParent, notebookDir.name)
        
        val todoFilePath = fileRepository.createFileWithContent(
            notebookDir,
            todoPath.toPath().name,
            """# Todo List

## Inbox
- [ ] Welcome to Markor!
- [ ] Try creating your first todo item

## Today

## This Week
"""
        )
        
        val quicknoteFilePath = fileRepository.createFileWithContent(
            notebookDir,
            quicknotePath.toPath().name,
            """# Quick Note

Welcome to Markor! This is your quick note file for jotting down thoughts.

## Ideas
- 

## Tasks
- 

## Notes
- 
"""
        )
        
        todoFilePath?.toString()?.let { favoritesRepository.addFavorite(it) }
        quicknoteFilePath?.toString()?.let { favoritesRepository.addFavorite(it) }
    }

    fun markIntroSeen() {
        viewModelScope.launch {
            appSettings.setFirstRun(false)
        }
    }
}
