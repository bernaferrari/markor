package net.gsantner.markor.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import okio.Path
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppUtils {

    fun getFileExtension(file: File): String {
        return file.extension.lowercase()
    }

    fun getFileExtension(uri: Uri, context: Context): String {
        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
    }

    fun getFileName(uri: Uri, context: Context): String {
        var fileName = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    fileName = cursor.getString(index)
                }
            }
        }
        return fileName
    }

    fun getFileSize(uri: Uri, context: Context): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0) {
                    size = cursor.getLong(index)
                }
            }
        }
        return size
    }

    fun copyUriToFile(uri: Uri, destinationFile: File, context: Context): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareFile(context: Context, file: File, title: String = "Share") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType(file)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMimeType(file: File): String {
        val extension = getFileExtension(file)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }

    fun formatDate(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    fun getDeviceInfo(): String {
        return "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    }

    fun getFileExtension(path: Path): String {
        return path.name.substringAfterLast('.', "").lowercase()
    }

    fun shareFile(context: Context, path: Path, title: String = "Share") {
        shareFile(context, File(path.toString()), title)
    }
}
