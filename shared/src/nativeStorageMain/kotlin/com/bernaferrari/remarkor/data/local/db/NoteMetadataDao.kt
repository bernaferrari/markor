package com.bernaferrari.remarkor.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteMetadataDao {
    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Upsert
    suspend fun upsertLabel(label: LabelEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteLabelCrossRef(crossRef: NoteLabelCrossRef)

    @Query("SELECT * FROM notes WHERE path = :path LIMIT 1")
    suspend fun getNoteByPath(path: String): NoteEntity?

    @Query("UPDATE notes SET path = :newPath, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNotePath(id: Long, newPath: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE path = :path")
    suspend fun deleteNoteByPath(path: String)

    @Query("DELETE FROM notes WHERE path = :path OR path LIKE :pathPrefix")
    suspend fun deleteNotesByPathOrPrefix(path: String, pathPrefix: String)

    @Query("SELECT * FROM labels WHERE name = :name LIMIT 1")
    suspend fun getLabelByName(name: String): LabelEntity?

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteWithLabels(id: Long): NoteWithLabels?

    @Transaction
    @Query("SELECT * FROM notes WHERE path = :path LIMIT 1")
    suspend fun getNoteWithLabelsByPath(path: String): NoteWithLabels?

    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteWithLabels>>

    @Query("SELECT * FROM notes WHERE pinned = 1 AND is_archived = 0 ORDER BY updatedAt DESC")
    fun observePinnedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_archived = 1 ORDER BY updatedAt DESC")
    fun observeArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun observeLabels(): Flow<List<LabelEntity>>

    @Query("DELETE FROM note_labels WHERE noteId = :noteId")
    suspend fun clearLabelsForNote(noteId: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}
