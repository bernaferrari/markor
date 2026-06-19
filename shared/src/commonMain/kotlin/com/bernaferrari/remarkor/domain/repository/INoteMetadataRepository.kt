package com.bernaferrari.remarkor.domain.repository

import com.bernaferrari.remarkor.domain.model.NoteLabel
import com.bernaferrari.remarkor.domain.model.NoteMetadata
import kotlinx.coroutines.flow.Flow

interface INoteMetadataRepository {
    fun observeNotes(): Flow<List<NoteMetadata>>
    fun observeLabels(): Flow<List<NoteLabel>>
    suspend fun upsertFromContent(path: String, content: String, nowMillis: Long)
    suspend fun upsertFromPath(path: String, nowMillis: Long)
    suspend fun getNoteByPath(path: String): NoteMetadata?
    suspend fun updatePath(oldPath: String, newPath: String, nowMillis: Long)
    suspend fun deleteByPath(path: String)
    suspend fun deleteByPathRecursively(path: String)
    suspend fun setLabelsForPath(path: String, labels: List<String>)
    suspend fun setColor(path: String, color: Int?)
    suspend fun setArchived(path: String, archived: Boolean)
    suspend fun togglePinned(path: String, nowMillis: Long)
}