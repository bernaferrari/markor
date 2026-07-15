package com.bernaferrari.remarkor.data.local.db

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.Junction
import androidx.room3.PrimaryKey
import androidx.room3.Relation

@Entity(
    tableName = "notes",
    indices = [Index(value = ["path"], unique = true)]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val title: String,
    val noteType: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    val color: Int?,
    val preview: String?,
    @ColumnInfo(name = "image_preview_url") val imagePreviewUrl: String? = null,
    val wordCount: Int?,
    val charCount: Int?
)

@Entity(
    tableName = "labels",
    indices = [Index(value = ["name"], unique = true)]
)
data class LabelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "note_labels",
    primaryKeys = ["noteId", "labelId"],
    indices = [Index(value = ["labelId"])]
)
data class NoteLabelCrossRef(
    val noteId: Long,
    val labelId: Long
)

data class NoteWithLabels(
    @Embedded
    val note: NoteEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            value = NoteLabelCrossRef::class,
            parentColumns = ["noteId"],
            entityColumns = ["labelId"]
        )
    )
    val labels: List<LabelEntity>
)
