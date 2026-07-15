package com.bernaferrari.remarkor.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.bernaferrari.remarkor.domain.repository.IFavoritesRepository
import com.bernaferrari.remarkor.util.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [IFavoritesRepository::class])
class FavoritesRepository(
    private val dataStore: DataStore<Preferences>,
) : IFavoritesRepository {

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorites_file_paths")
        val RECENT_FILES = stringSetPreferencesKey("recent_file_paths")
    }

    override val favorites: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES] ?: emptySet()
    }

    override val recentFiles: Flow<List<String>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.RECENT_FILES] ?: emptySet()
        raw.sortedByDescending { it.substringBefore("|") }
            .map { it.substringAfter("|") }
            .take(10)
    }

    override fun isFavorite(path: String): Flow<Boolean> = favorites.map { it.contains(path) }

    override suspend fun toggleFavorite(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(path)) current.remove(path) else current.add(path)
            prefs[Keys.FAVORITES] = current
        }
    }

    override suspend fun addFavorite(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            current.add(path)
            prefs[Keys.FAVORITES] = current
        }
    }

    override suspend fun removeFavorite(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            current.remove(path)
            prefs[Keys.FAVORITES] = current
        }
    }

    override suspend fun updatePath(oldPath: String, newPath: String) {
        dataStore.edit { prefs ->
            prefs[Keys.FAVORITES] = (prefs[Keys.FAVORITES] ?: emptySet()).map { path ->
                path.replaceMovedPath(oldPath, newPath)
            }.toSet()
            prefs[Keys.RECENT_FILES] = (prefs[Keys.RECENT_FILES] ?: emptySet()).map { entry ->
                val timestamp = entry.substringBefore("|")
                val path = entry.substringAfter("|", missingDelimiterValue = entry)
                "$timestamp|${path.replaceMovedPath(oldPath, newPath)}"
            }.toSet()
        }
    }

    override suspend fun recordFileAccess(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_FILES]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.substringAfter("|") == path }
            current.add("${nowMillis()}|$path")
            prefs[Keys.RECENT_FILES] = current.sortedByDescending { it.substringBefore("|") }
                .take(20)
                .toSet()
        }
    }

    override suspend fun clearRecentFiles() {
        dataStore.edit { prefs ->
            prefs[Keys.RECENT_FILES] = emptySet()
        }
    }
}

private fun String.replaceMovedPath(oldPath: String, newPath: String): String = when {
    this == oldPath -> newPath
    this.startsWith("$oldPath/") -> newPath + this.removePrefix(oldPath)
    else -> this
}
