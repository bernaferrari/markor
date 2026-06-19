package com.bernaferrari.remarkor.data.local.db

import com.bernaferrari.remarkor.domain.model.NoteLabel
import com.bernaferrari.remarkor.domain.model.NoteMetadata
import com.bernaferrari.remarkor.domain.repository.INoteMetadataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [INoteMetadataRepository::class])
internal class NoteMetadataRepository(
    private val dao: NoteMetadataDao,
) : INoteMetadataRepository {

    override fun observeNotes(): Flow<List<NoteMetadata>> =
        dao.observeAllNotes().map { notes -> notes.map { it.toDomain() } }

    override fun observeLabels(): Flow<List<NoteLabel>> =
        dao.observeLabels().map { labels -> labels.map { it.toDomain() } }

    override suspend fun upsertFromContent(path: String, content: String, nowMillis: Long) {
        val existing = dao.getNoteByPath(path)
        val note = NoteMetadataMapper.buildNoteEntity(path, content, existing, nowMillis)
        dao.upsertNote(note)
    }

    override suspend fun upsertFromPath(path: String, nowMillis: Long) {
        val existing = dao.getNoteByPath(path)
        val note = NoteMetadataMapper.buildNoteEntityFromPath(path, existing, nowMillis)
        dao.upsertNote(note)
    }

    override suspend fun getNoteByPath(path: String): NoteMetadata? =
        dao.getNoteByPath(path)?.toDomain()

    override suspend fun updatePath(oldPath: String, newPath: String, nowMillis: Long) {
        val existing = dao.getNoteByPath(oldPath) ?: return
        dao.updateNotePath(existing.id, newPath, nowMillis)
    }

    override suspend fun deleteByPath(path: String) {
        dao.deleteNoteByPath(path)
    }

    override suspend fun deleteByPathRecursively(path: String) {
        dao.deleteNotesByPathOrPrefix(path, "$path/%")
    }

    override suspend fun setLabelsForPath(path: String, labels: List<String>) {
        val note = dao.getNoteByPath(path) ?: return
        setLabelsForNote(note.id, labels)
    }

    private suspend fun setLabelsForNote(noteId: Long, labels: List<String>) {
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

    override suspend fun setColor(path: String, color: Int?) {
        val note = dao.getNoteByPath(path) ?: return
        dao.upsertNote(note.copy(color = color))
    }

    override suspend fun setArchived(path: String, archived: Boolean) {
        val note = dao.getNoteByPath(path) ?: return
        dao.upsertNote(note.copy(isArchived = archived))
    }

    override suspend fun togglePinned(path: String, nowMillis: Long) {
        val existing = dao.getNoteByPath(path)
        if (existing == null) {
            val note = NoteMetadataMapper.buildNoteEntityFromPath(path, null, nowMillis).copy(pinned = true)
            dao.upsertNote(note)
        } else {
            dao.upsertNote(existing.copy(pinned = !existing.pinned, updatedAt = nowMillis))
        }
    }
}