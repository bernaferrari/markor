package com.bernaferrari.remarkor.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [MarkorSharedDiModule::class])
@ComponentScan("com.bernaferrari.remarkor.appfunctions", "com.bernaferrari.remarkor.data.local")
class MarkorAppDiModule