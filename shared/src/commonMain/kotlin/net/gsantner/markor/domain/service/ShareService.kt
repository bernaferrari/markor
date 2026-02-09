package net.gsantner.markor.domain.service

import okio.Path

interface ShareService {
    fun shareFile(path: Path, title: String? = null)
    fun shareFile(fileName: String, content: ByteArray, title: String? = null, mimeType: String = "text/plain")
    fun shareText(text: String, title: String? = null)
}
