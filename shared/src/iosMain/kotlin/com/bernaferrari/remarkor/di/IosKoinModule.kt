package com.bernaferrari.remarkor.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.bernaferrari.remarkor.data.local.createDataStore
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDao
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabaseBuilder
import com.bernaferrari.remarkor.di.NotebookPaths
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
actual class PlatformDiModule {

    @Single
    @OptIn(ExperimentalForeignApi::class)
    fun provideDataStore(): DataStore<Preferences> =
        createDataStore {
            val documentDirectory =
                platform.Foundation.NSFileManager.defaultManager.URLForDirectory(
                    directory = platform.Foundation.NSDocumentDirectory,
                    inDomain = platform.Foundation.NSUserDomainMask,
                    appropriateForURL = null,
                    create = true,
                    error = null
                )
            requireNotNull(documentDirectory).path + "/markor_settings.preferences_pb"
        }

    @Single
    @Named("default_notebook_path")
    @OptIn(ExperimentalForeignApi::class)
    fun provideDefaultNotebookPath(): String {
        val documentDirectory = platform.Foundation.NSFileManager.defaultManager.URLForDirectory(
            directory = platform.Foundation.NSDocumentDirectory,
            inDomain = platform.Foundation.NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        val path = requireNotNull(documentDirectory).path + "/Notebook"
        ensureDirectory(path)
        return path
    }

    @Single
    @Named("internal_notebook_path")
    @OptIn(ExperimentalForeignApi::class)
    fun provideInternalNotebookPath(): String {
        val documentDirectory = platform.Foundation.NSFileManager.defaultManager.URLForDirectory(
            directory = platform.Foundation.NSDocumentDirectory,
            inDomain = platform.Foundation.NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        val path = requireNotNull(documentDirectory).path + "/NotebookPrivate"
        ensureDirectory(path)
        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ensureDirectory(path: String) {
        platform.Foundation.NSFileManager.defaultManager.createDirectoryAtPath(
            path = path,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
    }

    @Single
    fun provideDatabase(): NoteMetadataDatabase =
        getNoteMetadataDatabase(
            getNoteMetadataDatabaseBuilder(),
            BundledSQLiteDriver(),
        )

    @Single
    fun provideNoteMetadataDao(database: NoteMetadataDatabase): NoteMetadataDao =
        database.noteMetadataDao()

    @Single
    fun provideNotebookPaths(
        @Named("default_notebook_path") shared: String,
        @Named("internal_notebook_path") private: String,
    ): NotebookPaths = NotebookPaths(shared, private)
}