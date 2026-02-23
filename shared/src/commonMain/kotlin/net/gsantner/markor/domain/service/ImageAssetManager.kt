package net.gsantner.markor.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.gsantner.markor.util.nowMillis
import okio.FileSystem
import okio.SYSTEM
import okio.IOException
import okio.Path
import kotlin.math.roundToInt

/**
 * Manages image assets for markdown files.
 * Images are stored in a sibling folder: `filename_assets/`
 */
class ImageAssetManager {
    
    private val fileSystem = FileSystem.Companion.SYSTEM
    
    // Supported image extensions
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "svg", "bmp")
    
    /**
     * Get the assets folder path for a markdown file.
     * E.g., for "/notes/mydoc.md" -> "/notes/mydoc_assets/"
     */
    fun getAssetsFolderPath(filePath: Path): Path {
        val fileName = filePath.name.substringBeforeLast(".")
        val parent = filePath.parent ?: filePath
        return parent / "${fileName}_assets"
    }
    
    /**
     * Check if a file has an associated assets folder.
     */
    fun hasAssetsFolder(filePath: Path): Boolean {
        val assetsPath = getAssetsFolderPath(filePath)
        return fileSystem.exists(assetsPath) && fileSystem.metadata(assetsPath).isDirectory
    }
    
    /**
     * Create the assets folder if it doesn't exist.
     */
    suspend fun ensureAssetsFolder(filePath: Path): Path? = withContext(Dispatchers.Default) {
        try {
            val assetsPath = getAssetsFolderPath(filePath)
            if (!fileSystem.exists(assetsPath)) {
                fileSystem.createDirectories(assetsPath)
            }
            assetsPath
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * Add an image to the assets folder.
     * @param filePath The markdown file path
     * @param imageData The image data
     * @param originalName Original filename (to preserve extension)
     * @return The relative path to use in markdown (e.g., "./mydoc_assets/image.jpg")
     */
    suspend fun addImage(
        filePath: Path, 
        imageData: ByteArray, 
        originalName: String
    ): String? = withContext(Dispatchers.Default) {
        try {
            val assetsPath = ensureAssetsFolder(filePath) ?: return@withContext null
            
            // Generate unique filename
            val extension = originalName.substringAfterLast(".", "jpg").lowercase()
            val baseName = originalName.substringBeforeLast(".")
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .take(50)
            val timestamp = nowMillis()
            val fileName = "${baseName}_$timestamp.$extension"
            
            val imagePath = assetsPath / fileName
            fileSystem.write(imagePath) {
                write(imageData)
            }
            
            // Return relative path for markdown
            "./${assetsPath.name}/$fileName"
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * Copy an image from a source path to the assets folder.
     */
    suspend fun copyImageToAssets(
        filePath: Path,
        sourceImagePath: Path
    ): String? = withContext(Dispatchers.Default) {
        try {
            val assetsPath = ensureAssetsFolder(filePath) ?: return@withContext null
            
            val originalName = sourceImagePath.name
            val extension = originalName.substringAfterLast(".", "jpg").lowercase()
            val baseName = originalName.substringBeforeLast(".")
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .take(50)
            val timestamp = nowMillis()
            val fileName = "${baseName}_$timestamp.$extension"
            
            val destPath = assetsPath / fileName
            fileSystem.copy(sourceImagePath, destPath)
            
            "./${assetsPath.name}/$fileName"
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * Get all image files in the assets folder.
     */
    suspend fun listAssets(filePath: Path): List<AssetInfo> = withContext(Dispatchers.Default) {
        try {
            val assetsPath = getAssetsFolderPath(filePath)
            if (!fileSystem.exists(assetsPath)) return@withContext emptyList()
            
            fileSystem.list(assetsPath)
                .filter { path ->
                    val ext = path.name.substringAfterLast(".", "").lowercase()
                    ext in imageExtensions
                }
                .mapNotNull { path ->
                    val metadata = fileSystem.metadata(path)
                    AssetInfo(
                        path = path,
                        name = path.name,
                        size = metadata.size ?: 0L,
                        lastModified = metadata.lastModifiedAtMillis ?: 0L
                    )
                }
                .sortedByDescending { it.lastModified }
        } catch (e: IOException) {
            emptyList()
        }
    }
    
    /**
     * Extract image references from markdown content.
     * Supports: ![](path), ![alt](path), <img src="path">
     */
    fun extractImageReferences(content: String): Set<String> {
        val references = mutableSetOf<String>()
        
        // Match ![...](path)
        val markdownImageRegex = """!\[.*?\]\(([^)]+)\)""".toRegex()
        markdownImageRegex.findAll(content).forEach { match ->
            references.add(match.groupValues[1])
        }
        
        // Match <img src="path">
        val htmlImageRegex = """<img[^>]+src=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        htmlImageRegex.findAll(content).forEach { match ->
            references.add(match.groupValues[1])
        }
        
        return references
    }
    
    /**
     * Find orphaned images (in assets folder but not referenced in markdown).
     */
    suspend fun findOrphanedAssets(filePath: Path, content: String): List<AssetInfo> = withContext(Dispatchers.Default) {
        val allAssets = listAssets(filePath)
        val references = extractImageReferences(content)
        
        allAssets.filter { asset ->
            // Check if this asset is referenced
            val assetName = asset.name
            references.none { ref ->
                ref.contains(assetName) || 
                ref.endsWith("/$assetName") ||
                ref == "./${getAssetsFolderPath(filePath).name}/$assetName"
            }
        }
    }
    
    /**
     * Delete a specific asset.
     */
    suspend fun deleteAsset(assetPath: Path): Boolean = withContext(Dispatchers.Default) {
        try {
            fileSystem.delete(assetPath)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Delete all orphaned assets.
     */
    suspend fun deleteOrphanedAssets(filePath: Path, content: String): Int = withContext(Dispatchers.Default) {
        val orphans = findOrphanedAssets(filePath, content)
        var deleted = 0
        orphans.forEach { asset ->
            if (deleteAsset(asset.path)) {
                deleted++
            }
        }
        deleted
    }
    
    /**
     * Delete the entire assets folder.
     */
    suspend fun deleteAssetsFolder(filePath: Path): Boolean = withContext(Dispatchers.Default) {
        try {
            val assetsPath = getAssetsFolderPath(filePath)
            if (fileSystem.exists(assetsPath)) {
                fileSystem.deleteRecursively(assetsPath)
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Check if the markdown file has images.
     */
    fun hasImages(content: String): Boolean {
        return extractImageReferences(content).isNotEmpty()
    }
    
    /**
     * Get total size of assets folder.
     */
    suspend fun getAssetsSize(filePath: Path): Long = withContext(Dispatchers.Default) {
        try {
            val assetsPath = getAssetsFolderPath(filePath)
            if (!fileSystem.exists(assetsPath)) return@withContext 0L
            
            fileSystem.list(assetsPath)
                .filter { path ->
                    val ext = path.name.substringAfterLast(".", "").lowercase()
                    ext in imageExtensions
                }
                .sumOf { path ->
                    fileSystem.metadata(path).size ?: 0L
                }
        } catch (e: IOException) {
            0L
        }
    }
    
    /**
     * Check if assets folder is empty (no images).
     */
    suspend fun isAssetsFolderEmpty(filePath: Path): Boolean = withContext(Dispatchers.Default) {
        val assets = listAssets(filePath)
        assets.isEmpty()
    }
    
    /**
     * Clean up empty assets folder.
     */
    suspend fun cleanupEmptyAssetsFolder(filePath: Path) = withContext(Dispatchers.Default) {
        try {
            val assetsPath = getAssetsFolderPath(filePath)
            if (fileSystem.exists(assetsPath)) {
                val hasFiles = fileSystem.list(assetsPath).isNotEmpty()
                if (!hasFiles) {
                    fileSystem.delete(assetsPath)
                }
            }
        } catch (e: IOException) {
            // Ignore
        }
    }
}

/**
 * Information about an asset file.
 */
data class AssetInfo(
    val path: Path,
    val name: String,
    val size: Long,
    val lastModified: Long
) {
    fun formatSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> {
                val megabytes = size / (1024.0 * 1024.0)
                val roundedMegabytes = (megabytes * 10.0).roundToInt() / 10.0
                "$roundedMegabytes MB"
            }
        }
    }
}
