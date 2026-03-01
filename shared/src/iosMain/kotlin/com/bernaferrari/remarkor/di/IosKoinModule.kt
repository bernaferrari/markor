package com.bernaferrari.remarkor.di

import com.bernaferrari.remarkor.data.local.createDataStore
import com.bernaferrari.remarkor.data.local.db.NoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.NoteMetadataRepository
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabase
import com.bernaferrari.remarkor.data.local.db.getNoteMetadataDatabaseBuilder
import com.bernaferrari.remarkor.domain.service.IosShareService
import com.bernaferrari.remarkor.domain.service.ShareService
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    @OptIn(ExperimentalForeignApi::class)
    single {
        createDataStore {
            // iOS path
            val documentDirectory =
                platform.Foundation.NSFileManager.defaultManager.URLForDirectory(
                    directory = platform.Foundation.NSDocumentDirectory,
                    inDomain = platform.Foundation.NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null
                )
            requireNotNull(documentDirectory).path + "/markor_settings.preferences_pb"
        }
    }

    // Default notebook path for iOS
    @OptIn(ExperimentalForeignApi::class)
    single(org.koin.core.qualifier.named("default_notebook_path")) {
        val documentDirectory = platform.Foundation.NSFileManager.defaultManager.URLForDirectory(
            directory = platform.Foundation.NSDocumentDirectory,
            inDomain = platform.Foundation.NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(documentDirectory).path + "/Notebook"
    }
    @OptIn(ExperimentalForeignApi::class)
    single(org.koin.core.qualifier.named("internal_notebook_path")) {
        val documentDirectory = platform.Foundation.NSFileManager.defaultManager.URLForDirectory(
            directory = platform.Foundation.NSDocumentDirectory,
            inDomain = platform.Foundation.NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(documentDirectory).path + "/NotebookPrivate"
    }

    // Database
    single {
        getNoteMetadataDatabase(getNoteMetadataDatabaseBuilder())
    }
    single { get<NoteMetadataDatabase>().noteMetadataDao() }
    single { NoteMetadataRepository(get()) }

    singleOf(::IosShareService) bind ShareService::class
}
