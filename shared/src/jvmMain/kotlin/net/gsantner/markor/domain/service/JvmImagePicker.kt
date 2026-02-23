package net.gsantner.markor.domain.service

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JVM implementation of ImagePicker using JFileChooser.
 */
class JvmImagePicker : ImagePicker {
    override suspend fun pickImage(): PickedImage? = withContext(Dispatchers.IO) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Select Image"
            fileFilter = FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.webp)",
                "jpg", "jpeg", "png", "gif", "webp"
            )
        }
        
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            try {
                PickedImage(
                    data = file.readBytes(),
                    fileName = file.name,
                    mimeType = when (file.extension.lowercase()) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "gif" -> "image/gif"
                        "webp" -> "image/webp"
                        else -> "image/jpeg"
                    }
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}
