package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.data.local.AppSettings
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [ISettingsRepository::class])
internal class SettingsRepository(
    private val appSettings: AppSettings,
) : ISettingsRepository {
    override val isFirstRun: Flow<Boolean> = appSettings.isFirstRun
    override val isFileBrowserShowHiddenFiles: Flow<Boolean> = appSettings.isFileBrowserShowHiddenFiles
    override val isFileBrowserShowFileExtensions: Flow<Boolean> = appSettings.isFileBrowserShowFileExtensions
    override val getFileBrowserSortOrder: Flow<String> = appSettings.getFileBrowserSortOrder
    override val isFileBrowserFolderFirst: Flow<Boolean> = appSettings.isFileBrowserFolderFirst
    override val isShowLineNumbers: Flow<Boolean> = appSettings.isShowLineNumbers
    override val isWordWrap: Flow<Boolean> = appSettings.isWordWrap
    override val isEditorAutoFormat: Flow<Boolean> = appSettings.isEditorAutoFormat
    override val getEditorFontSize: Flow<Int> = appSettings.getEditorFontSize
    override val getNotebookDirectory: Flow<String> = appSettings.getNotebookDirectory
    override val getQuickNoteFilePath: Flow<String> = appSettings.getQuickNoteFilePath
    override val getTodoFilePath: Flow<String> = appSettings.getTodoFilePath
    override val getAppTheme: Flow<String> = appSettings.getAppTheme
    override val isExternalStorageEnabled: Flow<Boolean> = appSettings.isExternalStorageEnabled

    override suspend fun setFirstRun(value: Boolean) = appSettings.setFirstRun(value)
    override suspend fun setFileBrowserShowHiddenFiles(value: Boolean) =
        appSettings.setFileBrowserShowHiddenFiles(value)
    override suspend fun setFileBrowserShowFileExtensions(value: Boolean) =
        appSettings.setFileBrowserShowFileExtensions(value)
    override suspend fun setFileBrowserSortOrder(value: String) = appSettings.setFileBrowserSortOrder(value)
    override suspend fun setFileBrowserFolderFirst(value: Boolean) = appSettings.setFileBrowserFolderFirst(value)
    override suspend fun setShowLineNumbers(value: Boolean) = appSettings.setShowLineNumbers(value)
    override suspend fun setWordWrap(value: Boolean) = appSettings.setWordWrap(value)
    override suspend fun setEditorAutoFormat(value: Boolean) = appSettings.setEditorAutoFormat(value)
    override suspend fun setEditorFontSize(value: Int) = appSettings.setEditorFontSize(value)
    override suspend fun setNotebookDirectory(value: String) = appSettings.setNotebookDirectory(value)
    override suspend fun setQuickNoteFilePath(value: String) = appSettings.setQuickNoteFilePath(value)
    override suspend fun setTodoFilePath(value: String) = appSettings.setTodoFilePath(value)
    override suspend fun setAppTheme(value: String) = appSettings.setAppTheme(value)
    override suspend fun setExternalStorageEnabled(value: Boolean) =
        appSettings.setExternalStorageEnabled(value)
}