package com.bernaferrari.remarkor.domain.repository

import com.bernaferrari.remarkor.domain.model.AssetInfo
import okio.Path

interface IAssetRepository {
    fun getAssetsFolderPath(filePath: Path): Path
    fun hasAssetsFolder(filePath: Path): Boolean
    suspend fun addImage(markdownPath: Path, imageData: ByteArray, fileName: String): String?
    suspend fun listAssets(filePath: Path): List<AssetInfo>
    suspend fun findOrphanedAssets(filePath: Path, content: String): List<AssetInfo>
    suspend fun deleteAsset(assetPath: Path): Boolean
    suspend fun cleanupEmptyAssetsFolder(filePath: Path)
}