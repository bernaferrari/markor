package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.domain.repository.IFavoritesRepository
import com.bernaferrari.remarkor.util.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [IFavoritesRepository::class])
internal class WasmFavoritesRepository : IFavoritesRepository {
    private val favoritesState = MutableStateFlow<Set<String>>(emptySet())
    private val recentState = MutableStateFlow<List<String>>(emptyList())

    override val favorites: Flow<Set<String>> = favoritesState.asStateFlow()
    override val recentFiles: Flow<List<String>> = recentState.asStateFlow()

    override fun isFavorite(path: String): Flow<Boolean> = favorites.map { it.contains(path) }

    override suspend fun toggleFavorite(path: String) {
        favoritesState.value = favoritesState.value.toMutableSet().apply {
            if (contains(path)) remove(path) else add(path)
        }
    }

    override suspend fun addFavorite(path: String) {
        favoritesState.value = favoritesState.value + path
    }

    override suspend fun removeFavorite(path: String) {
        favoritesState.value = favoritesState.value - path
    }

    override suspend fun recordFileAccess(path: String) {
        recentState.value = listOf(path) + recentState.value.filter { it != path }.take(9)
    }

    override suspend fun clearRecentFiles() {
        recentState.value = emptyList()
    }
}