package com.bernaferrari.remarkor.platform

import com.bernaferrari.remarkor.domain.service.PickedImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSUUID
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
object IosImagePicker {
    fun launch(onResult: (PickedImage?) -> Unit) {
        val presenter = IosPlatformHolder.rootViewController ?: run {
            onResult(null)
            return
        }

        val pickerDelegate = PickerDelegate(onResult)

        val picker = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            allowsEditing = false
            delegate = pickerDelegate
        }

        presenter.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class PickerDelegate(
    private val onResult: (PickedImage?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onResult(extractPickedImage(didFinishPickingMediaWithInfo))
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onResult(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun extractPickedImage(info: Map<Any?, *>): PickedImage? {
    val image = info[UIImagePickerControllerOriginalImage] as? UIImage
        ?: return null
    val jpegData = UIImageJPEGRepresentation(image, 0.92) ?: return null
    val fileName = "image_${NSUUID().UUIDString}.jpg"
    return PickedImage(jpegData.toByteArray(), fileName, "image/jpeg")
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
}