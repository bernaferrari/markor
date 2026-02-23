package net.gsantner.markor.domain.service

import okio.Path

interface ShareService {
    fun shareFile(path: Path, title: String? = null)
    fun shareFile(fileName: String, content: ByteArray, title: String? = null, mimeType: String = "text/plain")
    fun shareText(text: String, title: String? = null)
    
    /**
     * Share a markdown file with its assets as a zip.
     * @param markdownPath Path to the markdown file
     * @param assetsFolderPath Path to the assets folder (e.g., filename_assets/)
     * @param title Optional title for the share dialog
     */
    fun shareMarkdownWithAssets(markdownPath: Path, assetsFolderPath: Path, title: String? = null)
}
