package com.bernaferrari.remarkor.di

import com.bernaferrari.remarkor.data.local.createDataStore
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.NoteMetadataRepository
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabaseBuilder
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
    single { com.bernaferrari.remarkor.domain.service.AndroidShareService(androidContext()) } bind com.bernaferrari.remarkor.domain.service.ShareService::class
}
