package net.gsantner.markor.di

import net.gsantner.markor.data.local.createDataStore
import net.gsantner.markor.data.local.db.*
import net.gsantner.markor.domain.service.*
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf

actual val platformModule: Module = module {
    single {
        createDataStore {
             // iOS path
             val documentDirectory = platform.Foundation.NSFileManager.defaultManager.URLForDirectory(
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

    // Database
    single {
        getNoteMetadataDatabase(getNoteMetadataDatabaseBuilder())
    }
    single { get<NoteMetadataDatabase>().noteMetadataDao() }
    single { NoteMetadataRepository(get()) }

    singleOf(::IosShareService) bind ShareService::class
}
