package com.bernaferrari.remarkor.domain.model

import okio.Path
import kotlin.math.roundToInt

data class AssetInfo(
    val path: Path,
    val name: String,
    val size: Long,
    val lastModified: Long,
) {
    fun formatSize(): String = when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> {
            val megabytes = size / (1024.0 * 1024.0)
            val roundedMegabytes = (megabytes * 10.0).roundToInt() / 10.0
            "$roundedMegabytes MB"
        }
    }
}