package com.bernaferrari.remarkor.ui.screens.filebrowser

/**
 * Extracts the original filename from a trash filename.
 * Trash format: "yyyyMMdd_HHmmss_originalfilename.ext"
 */
internal fun getOriginalPath(trashFileName: String): String {
    val underscoreIndex = trashFileName.indexOf("_")
    return if (underscoreIndex > 0) {
        trashFileName.substring(underscoreIndex + 1)
    } else {
        trashFileName
    }
}

internal const val BACK_ROW_RESHOW_DELAY_MS = 180L
