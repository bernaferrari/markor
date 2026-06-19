package com.bernaferrari.remarkor.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val isFirstRun: Flow<Boolean>
    val isFileBrowserShowHiddenFiles: Flow<Boolean>
    val isFileBrowserShowFileExtensions: Flow<Boolean>
    val getFileBrowserSortOrder: Flow<String>
    val isFileBrowserFolderFirst: Flow<Boolean>
    val isShowLineNumbers: Flow<Boolean>
    val isWordWrap: Flow<Boolean>
    val isEditorAutoFormat: Flow<Boolean>
    val getEditorFontSize: Flow<Int>
    val getNotebookDirectory: Flow<String>
    val getQuickNoteFilePath: Flow<String>
    val getTodoFilePath: Flow<String>
    val getAppTheme: Flow<String>
    val isExternalStorageEnabled: Flow<Boolean>

    suspend fun setFirstRun(value: Boolean)
    suspend fun setFileBrowserShowHiddenFiles(value: Boolean)
    suspend fun setFileBrowserShowFileExtensions(value: Boolean)
    suspend fun setFileBrowserSortOrder(value: String)
    suspend fun setFileBrowserFolderFirst(value: Boolean)
    suspend fun setShowLineNumbers(value: Boolean)
    suspend fun setWordWrap(value: Boolean)
    suspend fun setEditorAutoFormat(value: Boolean)
    suspend fun setEditorFontSize(value: Int)
    suspend fun setNotebookDirectory(value: String)
    suspend fun setQuickNoteFilePath(value: String)
    suspend fun setTodoFilePath(value: String)
    suspend fun setAppTheme(value: String)
    suspend fun setExternalStorageEnabled(value: Boolean)
}