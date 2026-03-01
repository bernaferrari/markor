package com.bernaferrari.remarkor.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.bernaferrari.remarkor.util.nowMillis

/**
 * Repository for managing favorited files.
 * Persists favorites as a set of file paths in DataStore.
 */
class FavoritesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorites_file_paths")
        val RECENT_FILES = stringSetPreferencesKey("recent_file_paths")
    }

    /**
     * Flow of all favorited file paths.
     */
    val favorites: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES] ?: emptySet()
    }

    /**
     * Flow of recently accessed file paths (ordered by recency, limited to 10).
     */
    val recentFiles: Flow<List<String>> = dataStore.data.map { prefs ->
        // We store as Set but maintain order via timestamp prefix
        val raw = prefs[Keys.RECENT_FILES] ?: emptySet()
        raw.sortedByDescending { it.substringBefore("|") }
            .map { it.substringAfter("|") }
            .take(10)
    }

    /**
     * Check if a specific file is favorited.
     */
    fun isFavorite(path: String): Flow<Boolean> = favorites.map { it.contains(path) }

    /**
     * Toggle favorite status for a file.
     */
    suspend fun toggleFavorite(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(path)) {
                current.remove(path)
            } else {
                current.add(path)
            }
            prefs[Keys.FAVORITES] = current
        }
    }

    /**
     * Add a file to favorites.
     */
    suspend fun addFavorite(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            current.add(path)
            prefs[Keys.FAVORITES] = current
        }
    }

    /**
     * Remove a file from favorites.
     */
    suspend fun removeFavorite(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            current.remove(path)
            prefs[Keys.FAVORITES] = current
        }
    }

    /**
     * Record a file access for recent files tracking.
     * Stores with timestamp prefix for ordering.
     */
    suspend fun recordFileAccess(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_FILES]?.toMutableSet() ?: mutableSetOf()
            // Remove old entry for this path if exists
            current.removeAll { it.substringAfter("|") == path }
            
            // Add new entry with current timestamp
            current.add("${nowMillis()}|$path")
            // Keep only last 20 entries
            val sorted = current.sortedByDescending { it.substringBefore("|") }
            prefs[Keys.RECENT_FILES] = sorted.take(20).toSet()
        }
    }

    /**
     * Clear all recent files.
     */
    suspend fun clearRecentFiles() {
        dataStore.edit { prefs ->
            prefs[Keys.RECENT_FILES] = emptySet()
        }
    }
}
