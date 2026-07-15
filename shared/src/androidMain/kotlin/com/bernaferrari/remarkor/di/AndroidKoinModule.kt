package com.bernaferrari.remarkor.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.bernaferrari.remarkor.data.local.createDataStore
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDao
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabaseBuilder
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
actual class PlatformDiModule {

    @Single
    fun provideDataStore(@Provided context: Context): DataStore<Preferences> =
        createDataStore {
            context.filesDir.resolve("datastore/settings.preferences_pb").absolutePath
        }

    @Single
    @Named("default_notebook_path")
    fun provideDefaultNotebookPath(): String {
        val path = android.os.Environment
            .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
            .resolve("Markor")
        if (!path.exists()) path.mkdirs()
        return path.absolutePath
    }

    @Single
    @Named("internal_notebook_path")
    fun provideInternalNotebookPath(@Provided context: Context): String {
        val path = context.filesDir.resolve("Notebook")
        if (!path.exists()) path.mkdirs()
        return path.absolutePath
    }

    @Single
    fun provideDatabase(@Provided context: Context): NoteMetadataDatabase =
        getNoteMetadataDatabase(
            getNoteMetadataDatabaseBuilder(context),
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