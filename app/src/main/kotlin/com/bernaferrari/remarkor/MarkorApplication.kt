package com.bernaferrari.remarkor

import android.app.Application

import com.bernaferrari.remarkor.di.appKoinModule
import com.bernaferrari.remarkor.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MarkorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MarkorApplication)
            modules(appModule, appKoinModule)
        }
    }
}
