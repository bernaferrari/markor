package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.bernaferrari.remarkor.domain.service.PickedImage
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit
): () -> Unit {
    return remember {
        {
            GlobalScope.launch(Dispatchers.IO) {
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
                    } catch (e: Exception) {
                        onImagePicked(null)
                    }
                } else {
                    onImagePicked(null)
                }
            }
        }
    }
}
