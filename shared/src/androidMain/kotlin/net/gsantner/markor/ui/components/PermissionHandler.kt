package net.gsantner.markor.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun HandleStoragePermissions(
    onRequest: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    if (!onRequest) return

    val context = LocalContext.current
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { 
             // On return from settings, check if granted
             if (android.os.Environment.isExternalStorageManager()) {
                 onGranted()
             } else {
                 onDenied()
             }
        }
        
        LaunchedEffect(onRequest) {
            if (!android.os.Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", context.packageName))
                    launcher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    launcher.launch(intent)
                }
            } else {
                onGranted()
            }
        }
    } else {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                onGranted()
            } else {
                onDenied()
            }
        }

        LaunchedEffect(onRequest) {
            // Check if already granted
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
               onGranted()
            } else {
               launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
}
