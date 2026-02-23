package net.gsantner.markor.domain.service

/**
 * iOS stub implementation of ImagePicker.
 * Actual implementation would use PHPickerViewController.
 */
class IosImagePicker : ImagePicker {
    override suspend fun pickImage(): PickedImage? {
        // TODO: Implement with PHPickerViewController
        return null
    }
}
