package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.data.local.BrowserStorage
import com.bernaferrari.remarkor.domain.repository.IFavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [IFavoritesRepository::class])
internal class WasmFavoritesRepository : IFavoritesRepository {
    private val json = Json { ignoreUnknownKeys = true }

    private fun loadSet(key: String): Set<String> {
        val raw = BrowserStorage.getString(key) ?: return emptySet()
        return runCatching { json.decodeFromString<List<String>>(raw).toSet() }.getOrDefault(emptySet())
    }

    private fun loadList(key: String): List<String> {
        val raw = BrowserStorage.getString(key) ?: return emptyList()
        return runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
    }

    private val favoritesState = MutableStateFlow(loadSet(FAV_KEY))
    private val recentState = MutableStateFlow(loadList(RECENT_KEY))

    override val favorites: Flow<Set<String>> = favoritesState.asStateFlow()
    override val recentFiles: Flow<List<String>> = recentState.asStateFlow()

    override fun isFavorite(path: String): Flow<Boolean> = favorites.map { it.contains(path) }

    override suspend fun toggleFavorite(path: String) {
        favoritesState.value = favoritesState.value.toMutableSet().apply {
            if (contains(path)) remove(path) else add(path)
        }
        persistFav()
    }

    override suspend fun addFavorite(path: String) {
        favoritesState.value = favoritesState.value + path
        persistFav()
    }

    override suspend fun removeFavorite(path: String) {
        favoritesState.value = favoritesState.value - path
        persistFav()
    }

    override suspend fun updatePath(oldPath: String, newPath: String) {
        favoritesState.value = favoritesState.value.map { it.replaceMovedPath(oldPath, newPath) }.toSet()
        recentState.value = recentState.value.map { it.replaceMovedPath(oldPath, newPath) }
        persistFav()
        BrowserStorage.setString(RECENT_KEY, json.encodeToString(recentState.value))
    }

    override suspend fun recordFileAccess(path: String) {
        recentState.value = listOf(path) + recentState.value.filter { it != path }.take(9)
        BrowserStorage.setString(RECENT_KEY, json.encodeToString(recentState.value))
    }

    override suspend fun clearRecentFiles() {
        recentState.value = emptyList()
        BrowserStorage.setString(RECENT_KEY, "[]")
    }

    private fun persistFav() {
        BrowserStorage.setString(FAV_KEY, json.encodeToString(favoritesState.value.toList()))
    }

    private companion object {
        const val FAV_KEY = "markor.web.favorites.v1"
        const val RECENT_KEY = "markor.web.recent.v1"
    }
}

private fun String.replaceMovedPath(oldPath: String, newPath: String): String = when {
    this == oldPath -> newPath
    this.startsWith("$oldPath/") -> newPath + this.removePrefix(oldPath)
    else -> this
}
