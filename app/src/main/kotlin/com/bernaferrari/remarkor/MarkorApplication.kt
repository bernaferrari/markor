package com.bernaferrari.remarkor

import android.app.Application
import androidx.appfunctions.service.AppFunctionConfiguration
import com.bernaferrari.remarkor.appfunctions.NoteFunctions
import com.bernaferrari.remarkor.di.MarkorAppDiModule
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [MarkorAppDiModule::class])
class MarkorApplication : Application(), AppFunctionConfiguration.Provider {
    private lateinit var noteFunctions: NoteFunctions

    override fun onCreate() {
        super.onCreate()
        startKoin<MarkorApplication> {
            androidContext(this@MarkorApplication)
        }

        noteFunctions = getKoin().get()
    }

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(NoteFunctions::class.java) { noteFunctions }
            .build()
}