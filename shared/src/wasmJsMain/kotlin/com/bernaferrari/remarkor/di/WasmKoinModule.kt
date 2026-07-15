package com.bernaferrari.remarkor.di

import com.bernaferrari.remarkor.data.local.db.NoteMetadataDao
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.createWebWorkerSQLiteDriver
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabaseBuilder
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

/**
 * Web platform DI — Room 3 + WebWorkerSQLiteDriver (OPFS).
 *
 * Notebook **files** still use the localStorage-backed FakeFileSystem; Room owns
 * structured note metadata (pins, colors, labels) like other platforms.
 */
@Module
actual class PlatformDiModule {

    @Single
    @Named("default_notebook_path")
    fun provideDefaultNotebookPath(): String = "/Notebook"

    @Single
    @Named("internal_notebook_path")
    fun provideInternalNotebookPath(): String = "/Notebook"

    @Single
    fun provideNotebookPaths(
        @Named("default_notebook_path") shared: String,
        @Named("internal_notebook_path") private: String,
    ): NotebookPaths = NotebookPaths(shared, private)

    @Single
    fun provideDatabase(): NoteMetadataDatabase =
        getNoteMetadataDatabase(
            getNoteMetadataDatabaseBuilder(),
            createWebWorkerSQLiteDriver(),
        )

    @Single
    fun provideNoteMetadataDao(database: NoteMetadataDatabase): NoteMetadataDao =
        database.noteMetadataDao()
}
