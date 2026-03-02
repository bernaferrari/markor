package com.bernaferrari.remarkor.domain.service

/**
 * Image data returned by the platform picker UI.
 */
data class PickedImage(
    val data: ByteArray,
    val fileName: String,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedImage) return false
        return data.contentEquals(other.data) && fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}
