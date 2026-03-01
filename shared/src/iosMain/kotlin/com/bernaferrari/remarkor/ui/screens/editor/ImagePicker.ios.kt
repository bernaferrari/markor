package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bernaferrari.remarkor.domain.service.PickedImage

/**
 * iOS image picker placeholder.
 * 
 * Full PHPickerViewController implementation requires:
 * 1. Creating a Swift wrapper class that implements PHPickerViewControllerDelegate
 * 2. Exposing it to Kotlin via cinterop or a Kotlin/Swift bridge
 * 3. Handling the async image loading from NSItemProvider
 * 
 * For production, consider:
 * - Using a KMP-compatible image picker library
 * - Creating a Swift-based ImagePickerCoordinator exposed to Kotlin
 * 
 * Current implementation returns null to allow compilation.
 */
@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (PickedImage?) -> Unit
): () -> Unit {
    return remember {
        {
            // TODO: Implement PHPickerViewController integration
            // This requires Swift interop or a third-party KMP library
            // 
            // For now, images can be added by:
            // 1. Manually copying images to the _assets folder
            // 2. Typing the markdown image syntax directly
            // 3. Using the share sheet to import images
            onImagePicked(null)
        }
    }
}
