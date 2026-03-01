package com.bernaferrari.remarkor.util

import kotlin.time.Clock

actual fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
