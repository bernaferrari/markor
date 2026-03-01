package com.bernaferrari.remarkor.ui.components

import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.launch
import com.bernaferrari.remarkor.domain.service.AssetInfo
import com.bernaferrari.remarkor.domain.service.ImageAssetManager
import okio.Path

@Composable
fun AssetManagerSheet(
    filePath: Path,
    content: String,
    assetManager: ImageAssetManager,
    onDismiss: () -> Unit,
    onContentChanged: (String) -> Unit = {},
    onAssetsDeleted: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    var allAssets by remember { mutableStateOf<List<AssetInfo>>(emptyList()) }
    var orphanedAssets by remember { mutableStateOf<List<AssetInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var selectedAssets by remember { mutableStateOf<Set<Path>>(emptySet()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    // Load assets
    LaunchedEffect(filePath, content) {
        isLoading = true
        loadError = null
        try {
            allAssets = assetManager.listAssets(filePath)
            orphanedAssets = assetManager.findOrphanedAssets(filePath, content)
        } catch (e: Exception) {
            loadError = "Failed to load images: ${e.message}"
        }
        isLoading = false
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { 
                Text(
                    "Delete ${if (selectedAssets.size == orphanedAssets.size && orphanedAssets.isNotEmpty()) "all orphaned" else "selected"} images?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                ) 
            },
            text = { 
                Text(
                    "This will permanently delete ${selectedAssets.size} image${if (selectedAssets.size != 1) "s" else ""}. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            var deleted = 0
                            selectedAssets.forEach { path ->
                                if (assetManager.deleteAsset(path)) {
                                    deleted++
                                }
                            }
                            // Refresh lists
                            allAssets = assetManager.listAssets(filePath)
                            orphanedAssets = assetManager.findOrphanedAssets(filePath, content)
                            selectedAssets = emptySet()
                            showDeleteConfirm = false
                            onAssetsDeleted()
                            
                            // Clean up empty folder
                            assetManager.cleanupEmptyAssetsFolder(filePath)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .animateContentSize()
        ) {
            // Header
            Text(
                stringResource(Res.string.manage_images),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (loadError != null) {
                // Error state
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Error loading images",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        loadError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            } else if (allAssets.isEmpty()) {
                // No images
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ImageNotSupported,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No images attached",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Images you add to this note will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Stats
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip(
                        icon = Icons.Default.Image,
                        label = "${allAssets.size} image${if (allAssets.size != 1) "s" else ""}"
                    )
                    if (orphanedAssets.isNotEmpty()) {
                        StatChip(
                            icon = Icons.Default.DeleteOutline,
                            label = "${orphanedAssets.size} unused",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Action buttons
                if (orphanedAssets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Select all orphans
                        OutlinedButton(
                            onClick = {
                                selectedAssets = if (selectedAssets == orphanedAssets.map { it.path }.toSet()) {
                                    emptySet()
                                } else {
                                    orphanedAssets.map { it.path }.toSet()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (selectedAssets.size == orphanedAssets.size) Icons.Default.Close else Icons.Default.SelectAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (selectedAssets.size == orphanedAssets.size) "Deselect all" else "Select unused")
                        }
                        
                        // Delete selected
                        Button(
                            onClick = { showDeleteConfirm = true },
                            enabled = selectedAssets.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAssets.isNotEmpty()) MaterialTheme.colorScheme.error 
                                    else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.delete))
                        }
                    }
                }
                
                // Asset list
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(allAssets) { asset ->
                        val isOrphaned = orphanedAssets.any { it.path == asset.path }
                        val isSelected = selectedAssets.contains(asset.path)
                        
                        AssetItem(
                            asset = asset,
                            isOrphaned = isOrphaned,
                            isSelected = isSelected,
                            canSelect = isOrphaned,
                            onSelect = {
                                if (isOrphaned) {
                                    selectedAssets = if (isSelected) {
                                        selectedAssets - asset.path
                                    } else {
                                        selectedAssets + asset.path
                                    }
                                }
                            },
                            onDelete = {
                                selectedAssets = setOf(asset.path)
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
private fun AssetItem(
    asset: AssetInfo,
    isOrphaned: Boolean,
    isSelected: Boolean,
    canSelect: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
               else if (isOrphaned) MaterialTheme.colorScheme.surfaceContainerHighest
               else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = canSelect) { onSelect() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image preview with actual thumbnail
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(asset.path.toString())
                    .size(96) // Thumbnail size
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        asset.formatSize(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isOrphaned) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "• unused",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Selection indicator or status
            if (canSelect) {
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(4.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ) {
                        Box(modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                // Used image - show checkmark
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(Res.string.in_use),
                        modifier = Modifier.padding(4.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
