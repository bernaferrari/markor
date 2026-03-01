package com.bernaferrari.remarkor.domain.service

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Android implementation of ImagePicker.
 * Note: This requires an ActivityResultLauncher to be provided from the Activity/Fragment.
 * The actual image picking is handled by the UI layer, this class just processes the result.
 */
class AndroidImagePicker(
    private val context: Context
) : ImagePicker {
    
    // This will be set by the UI when an image is picked
    private var pendingImageCallback: ((PickedImage?) -> Unit)? = null
    
    /**
     * Request to pick an image. This should trigger the ActivityResultLauncher.
     * The actual launch needs to be done from the Activity/Fragment level.
     */
    override suspend fun pickImage(): PickedImage? {
        // This is a placeholder - actual implementation needs to work with ActivityResultContracts
        // The UI component will need to handle the actual picking
        return null
    }
    
    /**
     * Process the result from the image picker ActivityResultContract.
     * Call this when the ActivityResult returns.
     */
    fun processImageUri(uri: Uri?): PickedImage? {
        if (uri == null) return null
        
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val data = inputStream?.readBytes()
            inputStream?.close()
            
            if (data == null) return null
            
            // Get filename from content resolver
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "image_${System.currentTimeMillis()}.jpg"
            
            // Get mime type
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            
            PickedImage(
                data = data,
                fileName = fileName,
                mimeType = mimeType
            )
        } catch (e: Exception) {
            null
        }
    }
}
