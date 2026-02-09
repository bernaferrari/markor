package net.gsantner.markor.domain.service

import okio.Path

class IosShareService : ShareService {
    override fun shareFile(path: Path, title: String?) {
        // TODO: Implement iOS sharing via ViewController
        println("Share file requested for $path (Not implemented yet)")
    }

    override fun shareText(text: String, title: String?) {
        // TODO: Implement iOS sharing
        println("Share text requested: $text (Not implemented yet)")
    }

    override fun shareFile(fileName: String, content: ByteArray, title: String?, mimeType: String) {
        // TODO: Implement iOS sharing
         println("Share file requested: $fileName (Not implemented yet)")
    }
}
