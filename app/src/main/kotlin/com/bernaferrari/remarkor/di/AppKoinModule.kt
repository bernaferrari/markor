package com.bernaferrari.remarkor.di

import com.bernaferrari.remarkor.data.local.GeneralDataStore
import org.koin.dsl.module

val appKoinModule = module {
    single { GeneralDataStore(get()) }
}
