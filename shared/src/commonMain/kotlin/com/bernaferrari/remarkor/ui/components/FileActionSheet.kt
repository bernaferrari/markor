package com.bernaferrari.remarkor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.domain.repository.FileInfo
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.delete
import markor.shared.generated.resources.labels
import markor.shared.generated.resources.manage_images
import markor.shared.generated.resources.properties
import markor.shared.generated.resources.rename
import markor.shared.generated.resources.share
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileActionSheet(
    file: FileInfo,
    isPinned: Boolean,
    hasAssets: Boolean = false,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onInfo: () -> Unit,
    onTogglePin: () -> Unit,
    onEditLabels: () -> Unit,
    onManageAssets: (() -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            ActionItem(
                icon = Icons.Default.Edit,
                label = stringResource(Res.string.rename),
                onClick = {
                    onDismiss()
                    onRename()
                }
            )

            ActionItem(
                icon = Icons.Default.Share,
                label = stringResource(Res.string.share),
                onClick = {
                    onDismiss()
                    onShare()
                }
            )

            ActionItem(
                icon = Icons.Default.PushPin,
                label = if (isPinned) "Unpin" else "Pin",
                onClick = {
                    onDismiss()
                    onTogglePin()
                }
            )

            ActionItem(
                icon = Icons.AutoMirrored.Filled.Label,
                label = stringResource(Res.string.labels),
                onClick = {
                    onDismiss()
                    onEditLabels()
                }
            )

            // Manage Assets - only show if file has assets
            if (hasAssets && onManageAssets != null) {
                ActionItem(
                    icon = Icons.Default.Image,
                    label = stringResource(Res.string.manage_images),
                    onClick = {
                        onDismiss()
                        onManageAssets()
                    }
                )
            }

            ActionItem(
                icon = Icons.Default.Delete,
                label = stringResource(Res.string.delete),
                color = MaterialTheme.colorScheme.error,
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )

            ActionItem(
                icon = Icons.Default.Info,
                label = stringResource(Res.string.properties),
                onClick = {
                    onDismiss()
                    onInfo()
                }
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}
