package net.gsantner.markor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LabelsDialog(
    initialLabels: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val initialText = initialLabels.joinToString(", ")
    val textState = remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Labels", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    text = "Separate labels with commas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val labels = textState.value
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    onConfirm(labels)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
