package net.gsantner.markor.domain.service

import android.content.Context
import android.content.Intent
import okio.Path
import androidx.core.content.FileProvider
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
    
    override fun shareMarkdownWithAssets(markdownPath: Path, assetsFolderPath: Path, title: String?) {
        val markdownFile = File(markdownPath.toString())
        val assetsFolder = File(assetsFolderPath.toString())
        
        // Create zip file in cache
        val cachePath = File(context.cacheDir, "shared_files")
        cachePath.mkdirs()
        val zipFileName = markdownFile.nameWithoutExtension + ".zip"
        val zipFile = File(cachePath, zipFileName)
        
        try {
            ZipOutputStream(zipFile.outputStream()).use { zipOut ->
                // Add markdown file
                val mdEntry = ZipEntry(markdownFile.name)
                zipOut.putNextEntry(mdEntry)
                markdownFile.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
                
                // Add all assets
                if (assetsFolder.exists() && assetsFolder.isDirectory) {
                    val assetsDirName = assetsFolder.name
                    assetsFolder.listFiles()?.forEach { assetFile ->
                        if (assetFile.isFile) {
                            val assetEntry = ZipEntry("$assetsDirName/${assetFile.name}")
                            zipOut.putNextEntry(assetEntry)
                            assetFile.inputStream().use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                    }
                }
            }
            
            // Share the zip
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, title ?: "Share Note with Images")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to sharing just the markdown file
            shareFile(markdownPath, title)
        }
    }
}
