package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [ISettingsRepository::class])
internal class WasmSettingsRepository : ISettingsRepository {
    private val isFirstRunState = MutableStateFlow(true)
    private val showHiddenState = MutableStateFlow(false)
    private val showExtState = MutableStateFlow(true)
    private val sortOrderState = MutableStateFlow("date")
    private val folderFirstState = MutableStateFlow(true)
    private val lineNumbersState = MutableStateFlow(false)
    private val wordWrapState = MutableStateFlow(true)
    private val autoFormatState = MutableStateFlow(true)
    private val fontSizeState = MutableStateFlow(14)
    private val notebookDirState = MutableStateFlow("")
    private val quickNotePathState = MutableStateFlow("")
    private val todoPathState = MutableStateFlow("")
    private val themeState = MutableStateFlow("markor")
    private val externalStorageState = MutableStateFlow(false)

    override val isFirstRun: Flow<Boolean> = isFirstRunState.asStateFlow()
    override val isFileBrowserShowHiddenFiles: Flow<Boolean> = showHiddenState.asStateFlow()
    override val isFileBrowserShowFileExtensions: Flow<Boolean> = showExtState.asStateFlow()
    override val getFileBrowserSortOrder: Flow<String> = sortOrderState.asStateFlow()
    override val isFileBrowserFolderFirst: Flow<Boolean> = folderFirstState.asStateFlow()
    override val isShowLineNumbers: Flow<Boolean> = lineNumbersState.asStateFlow()
    override val isWordWrap: Flow<Boolean> = wordWrapState.asStateFlow()
    override val isEditorAutoFormat: Flow<Boolean> = autoFormatState.asStateFlow()
    override val getEditorFontSize: Flow<Int> = fontSizeState.asStateFlow()
    override val getNotebookDirectory: Flow<String> = notebookDirState.asStateFlow()
    override val getQuickNoteFilePath: Flow<String> = quickNotePathState.asStateFlow()
    override val getTodoFilePath: Flow<String> = todoPathState.asStateFlow()
    override val getAppTheme: Flow<String> = themeState.asStateFlow()
    override val isExternalStorageEnabled: Flow<Boolean> = externalStorageState.asStateFlow()

    override suspend fun setFirstRun(value: Boolean) {
        isFirstRunState.value = value
    }

    override suspend fun setFileBrowserShowHiddenFiles(value: Boolean) {
        showHiddenState.value = value
    }

    override suspend fun setFileBrowserShowFileExtensions(value: Boolean) {
        showExtState.value = value
    }

    override suspend fun setFileBrowserSortOrder(value: String) {
        sortOrderState.value = value
    }

    override suspend fun setFileBrowserFolderFirst(value: Boolean) {
        folderFirstState.value = value
    }

    override suspend fun setShowLineNumbers(value: Boolean) {
        lineNumbersState.value = value
    }

    override suspend fun setWordWrap(value: Boolean) {
        wordWrapState.value = value
    }

    override suspend fun setEditorAutoFormat(value: Boolean) {
        autoFormatState.value = value
    }

    override suspend fun setEditorFontSize(value: Int) {
        fontSizeState.value = value
    }

    override suspend fun setNotebookDirectory(value: String) {
        notebookDirState.value = value
    }

    override suspend fun setQuickNoteFilePath(value: String) {
        quickNotePathState.value = value
    }

    override suspend fun setTodoFilePath(value: String) {
        todoPathState.value = value
    }

    override suspend fun setAppTheme(value: String) {
        themeState.value = value
    }

    override suspend fun setExternalStorageEnabled(value: Boolean) {
        externalStorageState.value = value
    }
}