package com.bernaferrari.remarkor.data.repository

import com.bernaferrari.remarkor.domain.model.NoteLabel
import com.bernaferrari.remarkor.domain.model.NoteMetadata
import com.bernaferrari.remarkor.domain.repository.INoteMetadataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.math.min

@Single(binds = [INoteMetadataRepository::class])
internal class WasmNoteMetadataRepository : INoteMetadataRepository {
    private var nextNoteId = 1L
    private var nextLabelId = 1L

    private data class StoredNote(
        val metadata: NoteMetadata,
        val createdAt: Long,
        val updatedAt: Long,
    )

    private val notesByPath = mutableMapOf<String, StoredNote>()
    private val labelsByName = mutableMapOf<String, NoteLabel>()
    private val noteLabels = mutableMapOf<Long, MutableSet<Long>>()

    private val state = MutableStateFlow(0)

    private fun emitChange() {
        state.update { it + 1 }
    }

    override fun observeNotes(): Flow<List<NoteMetadata>> =
        state.map {
            notesByPath.values
                .map { stored ->
                    stored.metadata.copy(labels = labelsForNote(stored.metadata.id))
                }
                .sortedByDescending { it.id }
        }

    override fun observeLabels(): Flow<List<NoteLabel>> =
        state.map { labelsByName.values.sortedBy { it.name } }

    override suspend fun upsertFromContent(path: String, content: String, nowMillis: Long) {
        val existing = notesByPath[path]
        val title = extractTitle(content, path)
        val metadata = NoteMetadata(
            id = existing?.metadata?.id ?: nextNoteId++,
            path = path,
            title = title,
            pinned = existing?.metadata?.pinned ?: false,
            isArchived = existing?.metadata?.isArchived ?: false,
            color = existing?.metadata?.color,
            preview = extractPreview(content),
            imagePreviewUrl = extractFirstImage(content),
            labels = existing?.metadata?.labels.orEmpty(),
        )
        notesByPath[path] = StoredNote(
            metadata = metadata,
            createdAt = existing?.createdAt ?: nowMillis,
            updatedAt = nowMillis,
        )
        emitChange()
    }

    override suspend fun upsertFromPath(path: String, nowMillis: Long) {
        val existing = notesByPath[path]
        val metadata = NoteMetadata(
            id = existing?.metadata?.id ?: nextNoteId++,
            path = path,
            title = path.substringAfterLast("/").substringBeforeLast("."),
            pinned = existing?.metadata?.pinned ?: false,
            isArchived = existing?.metadata?.isArchived ?: false,
            color = existing?.metadata?.color,
            preview = existing?.metadata?.preview,
            imagePreviewUrl = existing?.metadata?.imagePreviewUrl,
            labels = existing?.metadata?.labels.orEmpty(),
        )
        notesByPath[path] = StoredNote(
            metadata = metadata,
            createdAt = existing?.createdAt ?: nowMillis,
            updatedAt = nowMillis,
        )
        emitChange()
    }

    override suspend fun getNoteByPath(path: String): NoteMetadata? =
        notesByPath[path]?.metadata?.copy(labels = labelsForNote(notesByPath[path]!!.metadata.id))

    override suspend fun updatePath(oldPath: String, newPath: String, nowMillis: Long) {
        val stored = notesByPath.remove(oldPath) ?: return
        notesByPath[newPath] = stored.copy(
            metadata = stored.metadata.copy(path = newPath),
            updatedAt = nowMillis,
        )
        emitChange()
    }

    override suspend fun deleteByPath(path: String) {
        val removed = notesByPath.remove(path) ?: return
        noteLabels.remove(removed.metadata.id)
        emitChange()
    }

    override suspend fun deleteByPathRecursively(path: String) {
        val prefix = "$path/"
        notesByPath.keys
            .filter { it == path || it.startsWith(prefix) }
            .toList()
            .forEach { deleteByPath(it) }
    }

    override suspend fun setLabelsForPath(path: String, labels: List<String>) {
        val note = notesByPath[path] ?: return
        noteLabels[note.metadata.id] = mutableSetOf()
        labels.distinct().forEach { labelName ->
            val label = labelsByName.getOrPut(labelName) {
                NoteLabel(id = nextLabelId++, name = labelName)
            }
            noteLabels.getOrPut(note.metadata.id) { mutableSetOf() }.add(label.id)
        }
        emitChange()
    }

    override suspend fun setColor(path: String, color: Int?) {
        val stored = notesByPath[path] ?: return
        notesByPath[path] = stored.copy(metadata = stored.metadata.copy(color = color))
        emitChange()
    }

    override suspend fun setArchived(path: String, archived: Boolean) {
        val stored = notesByPath[path] ?: return
        notesByPath[path] = stored.copy(metadata = stored.metadata.copy(isArchived = archived))
        emitChange()
    }

    override suspend fun togglePinned(path: String, nowMillis: Long) {
        val stored = notesByPath[path]
        if (stored == null) {
            upsertFromPath(path, nowMillis)
            val created = notesByPath[path] ?: return
            notesByPath[path] = created.copy(
                metadata = created.metadata.copy(pinned = true),
                updatedAt = nowMillis,
            )
        } else {
            notesByPath[path] = stored.copy(
                metadata = stored.metadata.copy(pinned = !stored.metadata.pinned),
                updatedAt = nowMillis,
            )
        }
        emitChange()
    }

    private fun labelsForNote(noteId: Long): List<NoteLabel> {
        val labelIds = noteLabels[noteId].orEmpty()
        return labelsByName.values.filter { it.id in labelIds }
    }

    private fun extractTitle(content: String, path: String): String {
        for (line in content.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val withoutHashes = trimmed.trimStart('#').trim()
            if (withoutHashes.isNotEmpty()) return withoutHashes
        }
        return path.substringAfterLast("/").substringBeforeLast(".")
    }

    private fun extractPreview(content: String): String? {
        if (content.isBlank()) return null
        val normalized = content.replace(Regex("\\s+"), " ").trim()
        if (normalized.isEmpty()) return null
        return normalized.substring(0, min(normalized.length, 180))
    }

    private fun extractFirstImage(content: String): String? {
        val match = Regex("!\\[.*?]\\((.*?)\\)").find(content)
        return match?.groupValues?.get(1)
    }
}