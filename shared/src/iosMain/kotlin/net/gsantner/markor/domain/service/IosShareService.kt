package net.gsantner.markor.domain.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/**
 * iOS implementation of ShareService using UIActivityViewController.
 * Provides native iOS sharing functionality for files and text.
 */
@OptIn(ExperimentalForeignApi::class)
class IosShareService : ShareService {
    
    // Reference to the current view controller - should be set from iOS app
    var rootViewController: UIViewController? = null

    override fun shareFile(path: Path, title: String?) {
        try {
            val fileUrl = NSURL.fileURLWithPath(path.toString())
            shareItems(listOf(fileUrl), title)
        } catch (e: Exception) {
            println("ShareService: Failed to share file - ${e.message}")
        }
    }

    override fun shareText(text: String, title: String?) {
        try {
            shareItems(listOf(text), title)
        } catch (e: Exception) {
            println("ShareService: Failed to share text - ${e.message}")
        }
    }

    override fun shareFile(fileName: String, content: ByteArray, title: String?, mimeType: String) {
        try {
            // Create temporary file
            val tempDir = NSFileManager.defaultManager.temporaryDirectory
            val fileUrl = tempDir.URLByAppendingPathComponent(fileName)
                ?: return println("ShareService: Failed to create temp file URL")
            
            // Write content to temp file
            content.toNSData().writeToURL(fileUrl, atomically = true)
            
            shareItems(listOf(fileUrl), title)
        } catch (e: Exception) {
            println("ShareService: Failed to share file content - ${e.message}")
        }
    }
    
    override fun shareMarkdownWithAssets(markdownPath: Path, assetsFolderPath: Path, title: String?) {
        try {
            val fileManager = NSFileManager.defaultManager
            
            // Create temp zip file
            val tempDir = fileManager.temporaryDirectory
            val zipFileName = markdownPath.name.substringBeforeLast(".") + ".zip"
            val zipUrl = tempDir.URLByAppendingPathComponent(zipFileName)
                ?: return shareFile(markdownPath, title) // Fallback
            
            // Remove existing zip if present
            fileManager.removeItemAtURL(zipUrl, null)
            
            // Create zip using pure Kotlin ZipWriter
            val zipPath = zipUrl.path?.toPath()
                ?: return shareFile(markdownPath, title)
            val zipWriter = net.gsantner.markor.util.ZipWriter(zipPath)
            
            // Add markdown file
            zipWriter.addFile(markdownPath.name, markdownPath)
            
            // Add assets folder if it exists
            val assetsPath = assetsFolderPath.toString()
            if (fileManager.fileExistsAtPath(assetsPath)) {
                zipWriter.addDirectory(assetsFolderPath.name, assetsFolderPath)
            }
            
            zipWriter.write()
            
            // Share the zip file
            shareItems(listOf(zipUrl), title)
        } catch (e: Exception) {
            println("ShareService: Failed to share with assets - ${e.message}")
            // Fallback to just the markdown file
            shareFile(markdownPath, title)
        }
    }
    
    private fun shareItems(items: List<Any>, title: String?) {
        val viewController = rootViewController ?: getCurrentViewController() ?: run {
            println("ShareService: No view controller available")
            return
        }
        
        val activityViewController = UIActivityViewController(
            activityItems = items,
            applicationActivities = null
        )
        
        // Set subject for email etc. (optional)
        // Note: setValue:forKey: requires NSObject interop, skipping for simplicity
        
        viewController.presentViewController(activityViewController, animated = true, completion = null)
    }
    
    private fun getCurrentViewController(): UIViewController? {
        return try {
            val app = UIApplication.sharedApplication
            @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            val scene = app.connectedScenes.firstOrNull() as? platform.UIKit.UIWindowScene
            val window = scene?.windows?.firstOrNull() as? platform.UIKit.UIWindow
            window?.rootViewController
        } catch (e: Exception) {
            null
        }
    }
    
    // Helper extension to convert ByteArray to NSData
    private fun ByteArray.toNSData(): NSData {
        return if (this.isEmpty()) {
            NSData()
        } else {
            this.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
            }
        }
    }
}
