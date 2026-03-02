package com.bernaferrari.remarkor.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.bernaferrari.remarkor.domain.service.PickedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit
): () -> Unit {
    val scope = rememberCoroutineScope()

    return remember(scope, onImagePicked) {
        {
            scope.launch(Dispatchers.IO) {
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
                        val pickedImage = PickedImage(
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
                        onImagePicked(pickedImage)
                    } catch (_: Exception) {
                        onImagePicked(null)
                    }
                } else {
                    onImagePicked(null)
                }
            }
        }
    }
}
