package com.bernaferrari.remarkor.di

import com.bernaferrari.remarkor.data.local.AppSettings
import com.bernaferrari.remarkor.data.local.db.NoteMetadataIndexer
import com.bernaferrari.remarkor.data.local.db.NoteMetadataRepository
import com.bernaferrari.remarkor.data.repository.DocumentRepository
import com.bernaferrari.remarkor.data.repository.FileRepository
import com.bernaferrari.remarkor.domain.repository.FavoritesRepository
import com.bernaferrari.remarkor.domain.repository.IDocumentRepository
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import com.bernaferrari.remarkor.domain.service.ImageAssetManager
import com.bernaferrari.remarkor.ui.viewmodel.EditorViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.IntroViewModel
import com.bernaferrari.remarkor.ui.viewmodel.MainViewModel
import com.bernaferrari.remarkor.ui.viewmodel.SettingsViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

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
        FileBrowserViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(org.koin.core.qualifier.named("default_notebook_path"))
        )
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
