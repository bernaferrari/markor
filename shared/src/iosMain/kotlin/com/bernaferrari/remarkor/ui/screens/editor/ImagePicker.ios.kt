package com.bernaferrari.remarkor.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bernaferrari.remarkor.domain.service.PickedImage
import com.bernaferrari.remarkor.platform.IosImagePicker

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit
): () -> Unit = remember(onImagePicked) {
    { IosImagePicker.launch(onImagePicked) }
}