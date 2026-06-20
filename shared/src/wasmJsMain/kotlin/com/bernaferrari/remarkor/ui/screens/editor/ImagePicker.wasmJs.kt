package com.bernaferrari.remarkor.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bernaferrari.remarkor.domain.service.PickedImage

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit,
): () -> Unit = remember(onImagePicked) {
    { onImagePicked(null) }
}