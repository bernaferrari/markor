package com.bernaferrari.remarkor.domain.service

import okio.Path

/**
 * Platform-specific image picker interface.
 * Each platform implements this to handle image selection.
 */
interface ImagePicker {
    /**
     * Whether this platform supports picking images.
     */
    val isSupported: Boolean get() = true
    
    /**
     * Pick an image from gallery/file system.
     * @param onResult Callback with the picked image or null if cancelled.
     * Note: This is designed to be called from UI and the result comes async.
     */
    suspend fun pickImage(): PickedImage?
}

/**
 * Callback-based image picker for UI integration.
 * Use this when you need to trigger picking from a Composable.
 */
interface ImagePickerLauncher {
    /**
     * Launch the image picker.
     * @param onResult Callback with the picked image or null if cancelled.
     */
    fun launch(onResult: (PickedImage?) -> Unit)
    
    /**
     * Whether this platform supports picking images.
     */
    val isSupported: Boolean get() = true
}

/**
 * Result from picking an image.
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
