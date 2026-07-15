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
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.File

@Module
actual class PlatformDiModule {

    @Single
    fun provideDataStore(): DataStore<Preferences> {
        val baseDir = File(System.getProperty("user.home"), ".markor")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        val dataStoreFile = File(baseDir, "settings.preferences_pb")
        return createDataStore { dataStoreFile.absolutePath }
    }

    @Single
    @Named("default_notebook_path")
    fun provideDefaultNotebookPath(): String {
        val documentsDir = File(System.getProperty("user.home"), "Documents/Markor")
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }
        return documentsDir.absolutePath
    }

    @Single
    @Named("internal_notebook_path")
    fun provideInternalNotebookPath(): String {
        val notebookDir = File(System.getProperty("user.home"), ".markor/Notebook")
        if (!notebookDir.exists()) {
            notebookDir.mkdirs()
        }
        return notebookDir.absolutePath
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