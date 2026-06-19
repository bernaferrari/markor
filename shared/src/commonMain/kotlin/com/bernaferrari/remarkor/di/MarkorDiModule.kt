package com.bernaferrari.remarkor.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [PlatformDiModule::class])
@ComponentScan("com.bernaferrari.remarkor")
class MarkorSharedDiModule