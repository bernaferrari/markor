package com.bernaferrari.remarkor.domain.model

import okio.Path

data class Document(
    val path: Path,
    val title: String = "",
    val content: String = "",
    val format: DocumentFormat = DocumentFormat.PLAINTEXT,
    val lastModified: Long = 0L,
    val isFavorite: Boolean = false
) {
    val name: String get() = path.name
    val extension: String get() = name.substringAfterLast(".", "").lowercase()
    val absolutePath: String get() = path.toString()

    companion object {
        fun createNew(path: Path, format: DocumentFormat = DocumentFormat.PLAINTEXT): Document {
            return Document(
                path = path,
                title = path.name.substringBeforeLast("."),
                format = format,
                lastModified = 0L // Will be set on save
            )
        }

        fun fromPath(path: Path): Document {
            return Document(
                path = path,
                title = path.name.substringBeforeLast("."),
                format = DocumentFormat.fromExtension(path.name.substringAfterLast(".", "")),
                lastModified = 0L // Needs to be loaded from filesystem
            )
        }
    }
}
