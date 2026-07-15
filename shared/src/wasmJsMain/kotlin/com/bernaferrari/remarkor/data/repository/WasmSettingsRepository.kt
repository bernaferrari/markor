package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.data.local.BrowserStorage
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

/**
 * Settings backed by browser localStorage so first-run / notebook paths survive refresh.
 */
@Single(binds = [ISettingsRepository::class])
internal class WasmSettingsRepository : ISettingsRepository {
    private fun boolFlow(key: String, default: Boolean) =
        MutableStateFlow(BrowserStorage.getBoolean(key, default))

    private fun stringFlow(key: String, default: String) =
        MutableStateFlow(BrowserStorage.getString(key) ?: default)

    private fun intFlow(key: String, default: Int) =
        MutableStateFlow(BrowserStorage.getInt(key, default))

    private val isFirstRunState = boolFlow("markor.settings.isFirstRun", true)
    private val showHiddenState = boolFlow("markor.settings.showHidden", false)
    private val showExtState = boolFlow("markor.settings.showExt", true)
    private val sortOrderState = stringFlow("markor.settings.sortOrder", "date")
    private val folderFirstState = boolFlow("markor.settings.folderFirst", true)
    private val lineNumbersState = boolFlow("markor.settings.lineNumbers", false)
    private val wordWrapState = boolFlow("markor.settings.wordWrap", true)
    private val autoFormatState = boolFlow("markor.settings.autoFormat", true)
    private val fontSizeState = intFlow("markor.settings.fontSize", 14)
    private val notebookDirState = stringFlow("markor.settings.notebookDir", "")
    private val quickNotePathState = stringFlow("markor.settings.quickNotePath", "")
    private val todoPathState = stringFlow("markor.settings.todoPath", "")
    private val themeState = stringFlow("markor.settings.theme", "markor")
    private val externalStorageState = boolFlow("markor.settings.externalStorage", false)

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

    private fun setBool(state: MutableStateFlow<Boolean>, key: String, value: Boolean) {
        state.value = value
        BrowserStorage.setBoolean(key, value)
    }

    private fun setString(state: MutableStateFlow<String>, key: String, value: String) {
        state.value = value
        BrowserStorage.setString(key, value)
    }

    private fun setInt(state: MutableStateFlow<Int>, key: String, value: Int) {
        state.value = value
        BrowserStorage.setInt(key, value)
    }

    override suspend fun setFirstRun(value: Boolean) =
        setBool(isFirstRunState, "markor.settings.isFirstRun", value)

    override suspend fun setFileBrowserShowHiddenFiles(value: Boolean) =
        setBool(showHiddenState, "markor.settings.showHidden", value)

    override suspend fun setFileBrowserShowFileExtensions(value: Boolean) =
        setBool(showExtState, "markor.settings.showExt", value)

    override suspend fun setFileBrowserSortOrder(value: String) =
        setString(sortOrderState, "markor.settings.sortOrder", value)

    override suspend fun setFileBrowserFolderFirst(value: Boolean) =
        setBool(folderFirstState, "markor.settings.folderFirst", value)

    override suspend fun setShowLineNumbers(value: Boolean) =
        setBool(lineNumbersState, "markor.settings.lineNumbers", value)

    override suspend fun setWordWrap(value: Boolean) =
        setBool(wordWrapState, "markor.settings.wordWrap", value)

    override suspend fun setEditorAutoFormat(value: Boolean) =
        setBool(autoFormatState, "markor.settings.autoFormat", value)

    override suspend fun setEditorFontSize(value: Int) =
        setInt(fontSizeState, "markor.settings.fontSize", value)

    override suspend fun setNotebookDirectory(value: String) =
        setString(notebookDirState, "markor.settings.notebookDir", value)

    override suspend fun setQuickNoteFilePath(value: String) =
        setString(quickNotePathState, "markor.settings.quickNotePath", value)

    override suspend fun setTodoFilePath(value: String) =
        setString(todoPathState, "markor.settings.todoPath", value)

    override suspend fun setAppTheme(value: String) =
        setString(themeState, "markor.settings.theme", value)

    override suspend fun setExternalStorageEnabled(value: Boolean) =
        setBool(externalStorageState, "markor.settings.externalStorage", value)
}
