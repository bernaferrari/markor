package net.gsantner.markor.di

import net.gsantner.markor.data.local.GeneralDataStore
import org.koin.dsl.module

val appKoinModule = module {
    single { GeneralDataStore(get()) }
}
