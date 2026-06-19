package com.bernaferrari.remarkor.domain.service

import okio.Path
import org.koin.core.annotation.Single

@Single(binds = [ShareService::class])
class JvmShareService : ShareService {
    override fun shareFile(path: Path, title: String?) = Unit

    override fun shareFile(fileName: String, content: ByteArray, title: String?, mimeType: String) = Unit

    override fun shareText(text: String, title: String?) = Unit

    override fun shareMarkdownWithAssets(markdownPath: Path, assetsFolderPath: Path, title: String?) = Unit
}