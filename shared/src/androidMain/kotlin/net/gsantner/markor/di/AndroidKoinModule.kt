package net.gsantner.markor.di

import net.gsantner.markor.data.local.createDataStore
import net.gsantner.markor.data.local.db.NoteMetadataDatabase
import net.gsantner.markor.data.local.db.NoteMetadataRepository
import net.gsantner.markor.data.local.db.getNoteMetadataDatabase
import net.gsantner.markor.data.local.db.getNoteMetadataDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.bind

actual val platformModule: Module = module {
    single {
        createDataStore {
            androidContext().filesDir.resolve("datastore/settings.preferences_pb").absolutePath
        }
    }
    single(org.koin.core.qualifier.named("default_notebook_path")) {
        val path = android.os.Environment
            .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
            .resolve("Markor")
        if (!path.exists()) path.mkdirs()
        path.absolutePath
    }
    single(org.koin.core.qualifier.named("internal_notebook_path")) {
        val path = androidContext().filesDir.resolve("Notebook")
        if (!path.exists()) path.mkdirs()
        path.absolutePath
    }

    single {
        getNoteMetadataDatabase(getNoteMetadataDatabaseBuilder(androidContext()))
    }
    single { get<NoteMetadataDatabase>().noteMetadataDao() }
    single { NoteMetadataRepository(get()) }
    single { net.gsantner.markor.domain.service.AndroidShareService(androidContext()) } bind net.gsantner.markor.domain.service.ShareService::class
}
