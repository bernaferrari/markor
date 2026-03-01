package com.bernaferrari.remarkor.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun nowMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
