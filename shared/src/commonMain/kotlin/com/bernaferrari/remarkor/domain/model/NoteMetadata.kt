package com.bernaferrari.remarkor.domain.model

data class NoteLabel(
    val id: Long,
    val name: String,
)

data class NoteMetadata(
    val id: Long,
    val path: String,
    val title: String,
    val pinned: Boolean,
    val isArchived: Boolean,
    val color: Int?,
    val preview: String?,
    val imagePreviewUrl: String? = null,
    val labels: List<NoteLabel>,
)