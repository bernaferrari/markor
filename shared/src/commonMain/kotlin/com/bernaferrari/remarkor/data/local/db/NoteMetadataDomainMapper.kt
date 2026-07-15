package com.bernaferrari.remarkor.data.local.db

import com.bernaferrari.remarkor.domain.model.NoteLabel
import com.bernaferrari.remarkor.domain.model.NoteMetadata

internal fun NoteWithLabels.toDomain(): NoteMetadata = NoteMetadata(
    id = note.id,
    path = note.path,
    title = note.title,
    pinned = note.pinned,
    isArchived = note.isArchived,
    color = note.color,
    preview = note.preview,
    imagePreviewUrl = note.imagePreviewUrl,
    labels = labels.map { NoteLabel(id = it.id, name = it.name) },
)

internal fun LabelEntity.toDomain(): NoteLabel = NoteLabel(id = id, name = name)

internal fun NoteEntity.toDomain(labels: List<NoteLabel> = emptyList()): NoteMetadata = NoteMetadata(
    id = id,
    path = path,
    title = title,
    pinned = pinned,
    isArchived = isArchived,
    color = color,
    preview = preview,
    imagePreviewUrl = imagePreviewUrl,
    labels = labels,
)