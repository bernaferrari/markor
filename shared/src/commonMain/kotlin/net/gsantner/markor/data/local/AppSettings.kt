package net.gsantner.markor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
}

class AppSettings(private val dataStore: DataStore<Preferences>) {

    private object PrefsKeys {
        val FILE_BROWSER_SHOW_HIDDEN = booleanPreferencesKey("pref_key__file_browser_show_hidden_files")
        val FILE_BROWSER_SHOW_EXT = booleanPreferencesKey("pref_key__file_browser_show_file_ext")
        val FILE_BROWSER_SORT_ORDER = stringPreferencesKey("pref_key__file_browser_sort_order")
        val FILE_BROWSER_FOLDER_FIRST = booleanPreferencesKey("pref_key__file_browser_folder_first")
        val SHOW_LINE_NUMBERS = booleanPreferencesKey("pref_key__show_line_numbers")
        val WORD_WRAP = booleanPreferencesKey("pref_key__word_wrap")
        val EDITOR_AUTO_FORMAT = booleanPreferencesKey("pref_key__editor_auto_format")
        val EDITOR_FONT_SIZE = intPreferencesKey("pref_key__editor_font_size")
        val NOTEBOOK_DIRECTORY = stringPreferencesKey("pref_key__notebook_directory")
        val QUICKNOTE_PATH = stringPreferencesKey("pref_key__quicknote_path")
        val TODO_FILE_PATH = stringPreferencesKey("pref_key__todo_file_path")
        val APP_THEME = stringPreferencesKey("pref_key__app_theme")
        val EDITOR_FOREGROUND = stringPreferencesKey("pref_key__editor_foreground")
        val EDITOR_BACKGROUND = stringPreferencesKey("pref_key__editor_background")
        val IS_FIRST_RUN = booleanPreferencesKey("pref_key__is_first_run")
        val IS_EXTERNAL_STORAGE_ENABLED = booleanPreferencesKey("pref_key__is_external_storage_enabled")
    }

    val isFirstRun: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.IS_FIRST_RUN] ?: true
    }

    suspend fun setFirstRun(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.IS_FIRST_RUN] = value
        }
    }

    val isFileBrowserShowHiddenFiles: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.FILE_BROWSER_SHOW_HIDDEN] ?: false
    }

    val isFileBrowserShowFileExtensions: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.FILE_BROWSER_SHOW_EXT] ?: true
    }

    val getFileBrowserSortOrder: Flow<String> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.FILE_BROWSER_SORT_ORDER] ?: "date"
    }

    val isFileBrowserFolderFirst: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.FILE_BROWSER_FOLDER_FIRST] ?: true
    }

    val isShowLineNumbers: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.SHOW_LINE_NUMBERS] ?: true
    }

    val isWordWrap: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.WORD_WRAP] ?: false
    }

    val isEditorAutoFormat: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.EDITOR_AUTO_FORMAT] ?: true
    }

    val getEditorFontSize: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.EDITOR_FONT_SIZE] ?: 14
    }

    val getNotebookDirectory: Flow<String> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.NOTEBOOK_DIRECTORY] ?: ""
    }

    val getQuickNoteFilePath: Flow<String> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.QUICKNOTE_PATH] ?: ""
    }

    val getTodoFilePath: Flow<String> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.TODO_FILE_PATH] ?: ""
    }

    val getAppTheme: Flow<String> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.APP_THEME] ?: "markor"
    }

    val getEditorForeground: Flow<String> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.EDITOR_FOREGROUND] ?: "#000000"
    }

    val getEditorBackground: Flow<String> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.EDITOR_BACKGROUND] ?: "#FFFFFF"
    }

    val isExternalStorageEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PrefsKeys.IS_EXTERNAL_STORAGE_ENABLED] ?: false
    }

    suspend fun setFileBrowserShowHiddenFiles(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.FILE_BROWSER_SHOW_HIDDEN] = value
        }
    }

    suspend fun setFileBrowserShowFileExtensions(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.FILE_BROWSER_SHOW_EXT] = value
        }
    }

    suspend fun setFileBrowserSortOrder(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.FILE_BROWSER_SORT_ORDER] = value
        }
    }

    suspend fun setFileBrowserFolderFirst(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.FILE_BROWSER_FOLDER_FIRST] = value
        }
    }

    suspend fun setShowLineNumbers(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.SHOW_LINE_NUMBERS] = value
        }
    }

    suspend fun setWordWrap(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.WORD_WRAP] = value
        }
    }

    suspend fun setEditorAutoFormat(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.EDITOR_AUTO_FORMAT] = value
        }
    }

    suspend fun setEditorFontSize(value: Int) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.EDITOR_FONT_SIZE] = value
        }
    }

    suspend fun setNotebookDirectory(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.NOTEBOOK_DIRECTORY] = value
        }
    }

    suspend fun setQuickNoteFilePath(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.QUICKNOTE_PATH] = value
        }
    }

    suspend fun setTodoFilePath(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.TODO_FILE_PATH] = value
        }
    }

    suspend fun setAppTheme(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.APP_THEME] = value
        }
    }

    suspend fun setEditorForeground(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.EDITOR_FOREGROUND] = value
        }
    }

    suspend fun setEditorBackground(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.EDITOR_BACKGROUND] = value
        }
    }

    suspend fun setExternalStorageEnabled(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.IS_EXTERNAL_STORAGE_ENABLED] = value
        }
    }
}
