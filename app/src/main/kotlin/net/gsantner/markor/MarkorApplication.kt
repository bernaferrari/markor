package net.gsantner.markor

import android.app.Application

import net.gsantner.markor.di.appKoinModule
import net.gsantner.markor.di.appModule
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
