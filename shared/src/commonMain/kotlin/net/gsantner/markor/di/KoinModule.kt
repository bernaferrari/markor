package net.gsantner.markor.di

import net.gsantner.markor.data.local.AppSettings
import net.gsantner.markor.data.local.db.NoteMetadataIndexer
import net.gsantner.markor.data.local.db.NoteMetadataRepository
import net.gsantner.markor.data.repository.DocumentRepository
import net.gsantner.markor.data.repository.FileRepository
import net.gsantner.markor.domain.repository.FavoritesRepository
import net.gsantner.markor.domain.repository.IDocumentRepository
import net.gsantner.markor.domain.repository.IFileRepository
import net.gsantner.markor.domain.service.ImageAssetManager
import net.gsantner.markor.ui.viewmodel.EditorViewModel
import net.gsantner.markor.ui.viewmodel.FileBrowserViewModel
import net.gsantner.markor.ui.viewmodel.MainViewModel
import net.gsantner.markor.ui.viewmodel.SettingsViewModel
import net.gsantner.markor.ui.viewmodel.IntroViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.compose.viewmodel.dsl.viewModel

expect val platformModule: Module

val appModule = module {
    includes(platformModule)
    singleOf(::AppSettings)
    singleOf(::DocumentRepository) bind IDocumentRepository::class
    singleOf(::FileRepository) bind IFileRepository::class
    
    // FavoritesRepository - shares DataStore with AppSettings
    single { FavoritesRepository(get()) }
    single { NoteMetadataIndexer(get(), get()) }
    singleOf(::NoteMetadataRepository)
    singleOf(::ImageAssetManager)
    
    viewModelOf(::EditorViewModel)
    factory { 
        FileBrowserViewModel(get(), get(), get(), get(), get(), get(org.koin.core.qualifier.named("default_notebook_path"))) 
    }
    factory {
        MainViewModel(get(), get(org.koin.core.qualifier.named("default_notebook_path")))
    }
    viewModel {
        SettingsViewModel(
            appSettings = get(),
            fileRepository = get(),
            sharedNotebookPath = get(named("default_notebook_path")),
            privateNotebookPath = get(named("internal_notebook_path"))
        )
    }
    factory { IntroViewModel(get(), get(), get()) }
}

/**
 * Initialize Koin DI - call this once at app startup
 */
fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
