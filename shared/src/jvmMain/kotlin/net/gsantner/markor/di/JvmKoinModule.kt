package net.gsantner.markor.di

import java.io.File
import net.gsantner.markor.data.local.createDataStore
import net.gsantner.markor.data.local.db.NoteMetadataDatabase
import net.gsantner.markor.data.local.db.NoteMetadataRepository
import net.gsantner.markor.data.local.db.getNoteMetadataDatabase
import net.gsantner.markor.data.local.db.getNoteMetadataDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

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
    single { getNoteMetadataDatabase(getNoteMetadataDatabaseBuilder()) }
    single { get<NoteMetadataDatabase>().noteMetadataDao() }
    single { NoteMetadataRepository(get()) }
}
