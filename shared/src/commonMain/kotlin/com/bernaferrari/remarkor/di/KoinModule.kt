package com.bernaferrari.remarkor.di

import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [MarkorSharedDiModule::class])
object MarkorKoinApp

private var koinStarted = false

fun initKoin() {
    if (koinStarted) return
    startKoin<MarkorKoinApp>()
    koinStarted = true
}