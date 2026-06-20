package com.bernaferrari.remarkor.di

import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
actual class PlatformDiModule {

    @Single
    @Named("default_notebook_path")
    fun provideDefaultNotebookPath(): String = "/Notebook"

    @Single
    @Named("internal_notebook_path")
    fun provideInternalNotebookPath(): String = "/NotebookPrivate"

    @Single
    fun provideNotebookPaths(
        @Named("default_notebook_path") shared: String,
        @Named("internal_notebook_path") private: String,
    ): NotebookPaths = NotebookPaths(shared, private)
}