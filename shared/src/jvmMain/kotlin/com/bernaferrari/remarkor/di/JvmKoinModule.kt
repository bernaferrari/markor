package com.bernaferrari.remarkor.di

import com.bernaferrari.remarkor.data.local.createDataStore
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.NoteMetadataRepository
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single {
        val baseDir = File(System.getProperty("user.home"), ".markor")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        val dataStoreFile = File(baseDir, "settings.preferences_pb")
        createDataStore { dataStoreFile.absolutePath }
    }
    single(org.koin.core.qualifier.named("default_notebook_path")) {
        val documentsDir = File(System.getProperty("user.home"), "Documents/Markor")
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }
        documentsDir.absolutePath
    }
    single(org.koin.core.qualifier.named("internal_notebook_path")) {
        val notebookDir = File(System.getProperty("user.home"), ".markor/Notebook")
        if (!notebookDir.exists()) {
            notebookDir.mkdirs()
        }
        notebookDir.absolutePath
    }
    single { getNoteMetadataDatabase(getNoteMetadataDatabaseBuilder()) }
    single { get<NoteMetadataDatabase>().noteMetadataDao() }
    single { NoteMetadataRepository(get()) }
}
