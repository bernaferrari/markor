package com.bernaferrari.remarkor.ui.components

import com.bernaferrari.remarkor.ui.icons.MaterialSymbols

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bernaferrari.remarkor.domain.repository.FileInfo
import kotlinx.coroutines.delay
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.delete
import markor.shared.generated.resources.cancel
import markor.shared.generated.resources.close
import markor.shared.generated.resources.create
import markor.shared.generated.resources.delete_files_warning
import markor.shared.generated.resources.delete_items_question
import markor.shared.generated.resources.file
import markor.shared.generated.resources.file_name
import markor.shared.generated.resources.folder
import markor.shared.generated.resources.folder_name
import markor.shared.generated.resources.name
import markor.shared.generated.resources.new_folder
import markor.shared.generated.resources.new_note
import markor.shared.generated.resources.path_with_arg
import markor.shared.generated.resources.rename
import markor.shared.generated.resources.size_bytes_with_arg
import markor.shared.generated.resources.type_with_arg
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    PremiumInputDialog(
        title = stringResource(Res.string.new_folder),
        icon = MaterialSymbols.Filled.Folder,
        value = folderName,
        onValueChange = { folderName = it },
        label = stringResource(Res.string.folder_name),
        confirmText = stringResource(Res.string.create),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(folderName) }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    PremiumInputDialog(
        title = stringResource(Res.string.rename),
        icon = MaterialSymbols.Filled.Edit,
        value = newName,
        onValueChange = { newName = it },
        label = stringResource(Res.string.name),
        confirmText = stringResource(Res.string.rename),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(newName) }
    )
}

@Composable
fun DeleteDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val haptic = rememberHapticHelper()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(MaterialSymbols.Filled.Delete, contentDescription = null) },
        title = {
            Text(
                text = pluralStringResource(Res.plurals.delete_items_question, count, count),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                stringResource(Res.string.delete_files_warning),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHeavyClick()
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.performLightClick()
                onDismiss()
            }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun FilePropertiesDialog(
    file: FileInfo,
    onDismiss: () -> Unit,
) {
    val folderLabel = stringResource(Res.string.folder)
    val fileLabel = stringResource(Res.string.file)
    val fileType = if (file.isDirectory) folderLabel else file.extension.ifBlank { fileLabel }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(file.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.path_with_arg, file.path), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(
                        Res.string.type_with_arg,
                        fileType,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!file.isDirectory) {
                    Text(stringResource(Res.string.size_bytes_with_arg, file.size), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.close)) } },
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun PremiumInputDialog(
    title: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        // Dialog attach can be one frame late; delay avoids dropped focus/IME requests.
        delay(120)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm, enabled = value.isNotEmpty()) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    suggestedName: String = "new_note.md"
) {
    var fileName by remember { mutableStateOf(suggestedName) }

    PremiumInputDialog(
        title = stringResource(Res.string.new_note),
        icon = MaterialSymbols.Filled.Create,
        value = fileName,
        onValueChange = { fileName = it },
        label = stringResource(Res.string.file_name),
        confirmText = stringResource(Res.string.create),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(fileName) }
    )
}
