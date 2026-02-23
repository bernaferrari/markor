package net.gsantner.markor.util

import kotlin.time.Clock

actual fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
