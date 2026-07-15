package com.bernaferrari.remarkor.domain.repository

import kotlinx.coroutines.flow.Flow

interface IFavoritesRepository {
    val favorites: Flow<Set<String>>
    val recentFiles: Flow<List<String>>

    fun isFavorite(path: String): Flow<Boolean>
    suspend fun toggleFavorite(path: String)
    suspend fun addFavorite(path: String)
    suspend fun removeFavorite(path: String)
    suspend fun updatePath(oldPath: String, newPath: String)
    suspend fun recordFileAccess(path: String)
    suspend fun clearRecentFiles()
}
