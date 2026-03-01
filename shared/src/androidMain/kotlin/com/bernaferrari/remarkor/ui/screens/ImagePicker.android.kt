package com.bernaferrari.remarkor.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bernaferrari.remarkor.domain.service.PickedImage

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            onImagePicked(null)
            return@rememberLauncherForActivityResult
        }

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val data = inputStream?.readBytes()
            inputStream?.close()

            if (data == null) {
                onImagePicked(null)
                return@rememberLauncherForActivityResult
            }

            // Get filename
            val fileName =
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex =
                        cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                } ?: "image_${System.currentTimeMillis()}.jpg"

            // Get mime type
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

            onImagePicked(PickedImage(data, fileName, mimeType))
        } catch (e: Exception) {
            onImagePicked(null)
        }
    }

    return remember {
        {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}
