package com.bernaferrari.remarkor.domain.usecase

import com.bernaferrari.remarkor.di.NotebookPaths
import com.bernaferrari.remarkor.domain.repository.IFavoritesRepository
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single

@Single
class InitializeNotebookUseCase(
    private val settingsRepository: ISettingsRepository,
    private val fileRepository: IFileRepository,
    private val favoritesRepository: IFavoritesRepository,
    private val notebookPaths: NotebookPaths,
) {
    suspend operator fun invoke(isExternal: Boolean, internalFilesDir: String) {
        settingsRepository.setExternalStorageEnabled(isExternal)

        val notebookDir = if (isExternal) {
            notebookPaths.shared
        } else {
            "$internalFilesDir/Notebook"
        }
        val todoPath = "$notebookDir/todo.txt"
        val quicknotePath = "$notebookDir/quicknote.md"

        settingsRepository.setNotebookDirectory(notebookDir)
        settingsRepository.setTodoFilePath(todoPath)
        settingsRepository.setQuickNoteFilePath(quicknotePath)

        initializeDefaultFiles(notebookDir.toPath(), todoPath, quicknotePath)
    }

    private suspend fun initializeDefaultFiles(
        notebookDir: Path,
        todoPath: String,
        quicknotePath: String,
    ) {
        val notebookParent = notebookDir.parent ?: notebookDir
        fileRepository.createDirectory(notebookParent, notebookDir.name)

        val todoFilePath = fileRepository.createFileWithContent(
            notebookDir,
            todoPath.toPath().name,
            DEFAULT_TODO_CONTENT,
        )

        val quicknoteFilePath = fileRepository.createFileWithContent(
            notebookDir,
            quicknotePath.toPath().name,
            DEFAULT_QUICKNOTE_CONTENT,
        )

        todoFilePath?.toString()?.let { favoritesRepository.addFavorite(it) }
        quicknoteFilePath?.toString()?.let { favoritesRepository.addFavorite(it) }
    }

    private companion object {
        const val DEFAULT_TODO_CONTENT = """# Todo List

## Inbox
- [ ] Welcome to Markor!
- [ ] Try creating your first todo item

## Today

## This Week
"""
        const val DEFAULT_QUICKNOTE_CONTENT = """# Quick Note

Welcome to Markor! This is your quick note file for jotting down thoughts.

## Ideas
- 

## Tasks
- 

## Notes
- 
"""
    }
}