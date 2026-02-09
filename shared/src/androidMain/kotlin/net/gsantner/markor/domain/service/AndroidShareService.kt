package net.gsantner.markor.domain.service

import android.content.Context
import android.content.Intent
import okio.Path
import androidx.core.content.FileProvider
import java.io.File

class AndroidShareService(private val context: Context) : ShareService {
    override fun shareFile(path: Path, title: String?) {
        val file = File(path.toString())
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // Or detect mime type
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, title ?: "Share File")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    override fun shareText(text: String, title: String?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        
        val chooser = Intent.createChooser(intent, title ?: "Share Text")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    override fun shareFile(fileName: String, content: ByteArray, title: String?, mimeType: String) {
        val cachePath = File(context.cacheDir, "shared_files")
        cachePath.mkdirs()
        val file = File(cachePath, fileName)
        file.writeBytes(content)
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
         val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, title ?: "Share File")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
