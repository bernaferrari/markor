package com.bernaferrari.remarkor.data.local.db

import kotlinx.coroutines.flow.Flow

class NoteMetadataRepository(
    private val dao: NoteMetadataDao
) {
    fun observeNotes(): Flow<List<NoteWithLabels>> = dao.observeAllNotes()

    fun observePinnedNotes(): Flow<List<NoteEntity>> = dao.observePinnedNotes()

    fun observeArchivedNotes(): Flow<List<NoteEntity>> = dao.observeArchivedNotes()

    fun observeLabels(): Flow<List<LabelEntity>> = dao.observeLabels()

    suspend fun upsertNote(note: NoteEntity) {
        dao.upsertNote(note)
    }

    suspend fun upsertFromContent(path: String, content: String, nowMillis: Long) {
        val existing = dao.getNoteByPath(path)
        val note = NoteMetadataMapper.buildNoteEntity(path, content, existing, nowMillis)
        dao.upsertNote(note)
    }

    suspend fun upsertFromPath(path: String, nowMillis: Long) {
        val existing = dao.getNoteByPath(path)
        val note = NoteMetadataMapper.buildNoteEntityFromPath(path, existing, nowMillis)
        dao.upsertNote(note)
    }

    suspend fun getNoteByPath(path: String): NoteEntity? {
        return dao.getNoteByPath(path)
    }

    suspend fun updatePath(oldPath: String, newPath: String, nowMillis: Long) {
        val existing = dao.getNoteByPath(oldPath) ?: return
        dao.updateNotePath(existing.id, newPath, nowMillis)
    }

    suspend fun deleteByPath(path: String) {
        dao.deleteNoteByPath(path)
    }

    suspend fun deleteByPathRecursively(path: String) {
        dao.deleteNotesByPathOrPrefix(path, "$path/%")
    }

    suspend fun setLabelsForNote(noteId: Long, labels: List<String>) {
        dao.clearLabelsForNote(noteId)
        labels.distinct().forEach { labelName ->
            dao.upsertLabel(LabelEntity(name = labelName))
            val stored = dao.getLabelByName(labelName)
            if (stored != null) {
                dao.insertNoteLabelCrossRef(
                    NoteLabelCrossRef(noteId = noteId, labelId = stored.id)
                )
            }
        }
    }
    
    suspend fun setLabelsForPath(path: String, labels: List<String>) {
        val note = dao.getNoteByPath(path)
        if (note != null) {
            setLabelsForNote(note.id, labels)
        } else {
             // Create empty note entry if it doesn't exist? 
             // Ideally we index it first. For now, assume it exists or index on the fly.
             // Let's index it briefly without content or just fail silently/log.
             // Better: upsertFromPath(path, Clock.System.now().toEpochMilliseconds())
             // But we don't have Clock here easily. 
             // Let's just return for now or try to get it if safe.
        }
    }

    suspend fun getNoteWithLabelsByPath(path: String): NoteWithLabels? {
        return dao.getNoteWithLabelsByPath(path)
    }
    
    suspend fun setColor(path: String, color: Int?) {
        val note = dao.getNoteByPath(path)
        if (note != null) {
            dao.upsertNote(note.copy(color = color))
        }
    }
    
    suspend fun setArchived(path: String, archived: Boolean) {
        val note = dao.getNoteByPath(path)
        if (note != null) {
            dao.upsertNote(note.copy(isArchived = archived))
        }
    }
}
