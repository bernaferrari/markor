package com.bernaferrari.remarkor.ui.screens.filebrowser

/**
 * Extracts the original filename from a trash filename.
 * Trash format: "yyyyMMdd_HHmmss_originalfilename.ext"
 */
internal fun getOriginalPath(trashFileName: String): String {
    val dateSeparator = trashFileName.indexOf("_")
    val timeSeparator = trashFileName.indexOf("_", startIndex = dateSeparator + 1)
    return if (timeSeparator > 0) {
        trashFileName.substring(timeSeparator + 1)
    } else {
        trashFileName
    }
}

internal const val BACK_ROW_RESHOW_DELAY_MS = 180L
