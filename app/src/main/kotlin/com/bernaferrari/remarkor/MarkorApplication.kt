package com.bernaferrari.remarkor

import android.app.Application
import androidx.appfunctions.service.AppFunctionConfiguration
import com.bernaferrari.remarkor.appfunctions.NoteFunctions
import com.bernaferrari.remarkor.data.local.AppSettings
import com.bernaferrari.remarkor.di.appKoinModule
import com.bernaferrari.remarkor.di.appModule
import com.bernaferrari.remarkor.domain.repository.IDocumentRepository
import com.bernaferrari.remarkor.domain.repository.IFileRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named

class MarkorApplication : Application(), AppFunctionConfiguration.Provider {
    private lateinit var noteFunctions: NoteFunctions

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MarkorApplication)
            modules(appModule, appKoinModule)
        }

        val koin = GlobalContext.get()
        noteFunctions = NoteFunctions(
            fileRepository = koin.get<IFileRepository>(),
            documentRepository = koin.get<IDocumentRepository>(),
            appSettings = koin.get<AppSettings>(),
            defaultNotebookPath = koin.get(named("default_notebook_path")),
        )
    }

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(NoteFunctions::class.java) { noteFunctions }
            .build()
}
