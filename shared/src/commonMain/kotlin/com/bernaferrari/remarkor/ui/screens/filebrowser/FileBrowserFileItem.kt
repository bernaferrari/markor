package com.bernaferrari.remarkor.ui.screens.filebrowser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.domain.repository.IAssetRepository
import com.bernaferrari.remarkor.ui.components.AssetManagerSheet
import com.bernaferrari.remarkor.ui.components.BackHandler
import com.bernaferrari.remarkor.ui.components.CreateFolderDialog
import com.bernaferrari.remarkor.ui.components.DeleteDialog
import com.bernaferrari.remarkor.ui.components.EmptyState
import com.bernaferrari.remarkor.ui.components.FavoriteIndicator
import com.bernaferrari.remarkor.ui.components.FileActionSheet
import com.bernaferrari.remarkor.ui.components.FileGridItem
import com.bernaferrari.remarkor.ui.components.HapticHelper
import com.bernaferrari.remarkor.ui.components.LabelsDialog
import com.bernaferrari.remarkor.ui.components.RenameDialog
import com.bernaferrari.remarkor.ui.components.ShareDialog
import com.bernaferrari.remarkor.ui.components.SharedElementContainer
import com.bernaferrari.remarkor.ui.components.SharedTransitionKeys
import com.bernaferrari.remarkor.ui.components.SwipeableFileCard
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.components.renderGridMarkdown
import com.bernaferrari.remarkor.ui.components.resolveMarkdownColorPalette
import com.bernaferrari.remarkor.ui.components.resolveNoteSurfaceColor
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.back_to_with_arg
import markor.shared.generated.resources.create_new
import markor.shared.generated.resources.delete_permanently
import markor.shared.generated.resources.favorite
import markor.shared.generated.resources.folder
import markor.shared.generated.resources.more
import markor.shared.generated.resources.more_create_options
import markor.shared.generated.resources.no_favorites_yet
import markor.shared.generated.resources.no_favorites_yet_description
import markor.shared.generated.resources.notebook_is_empty
import markor.shared.generated.resources.notebook_is_empty_description
import markor.shared.generated.resources.restore
import markor.shared.generated.resources.trash_is_empty
import markor.shared.generated.resources.trash_is_empty_description
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun EmptyListState() {
    EmptyState(
        title = stringResource(Res.string.notebook_is_empty),
        subtitle = stringResource(Res.string.notebook_is_empty_description),
        icon = Icons.Default.FolderOpen
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FileItem(
    file: FileInfo,
    isSelected: Boolean,
    selectionMode: Boolean,
    isFavorite: Boolean = false,
    noteColor: Int? = null,
    preview: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    haptic: HapticHelper = rememberHapticHelper(),
    isTrashMode: Boolean = false,
    onRestore: (() -> Unit)? = null,
    onDeletePermanently: (() -> Unit)? = null,
    isKeyboardSelected: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = when {
        isSelected -> colorScheme.primaryContainer
        isKeyboardSelected -> colorScheme.secondaryContainer
        isTrashMode -> colorScheme.errorContainer.copy(alpha = 0.3f)
        file.isDirectory -> colorScheme.surface
        noteColor != null -> resolveNoteSurfaceColor(
            noteColor,
            colorScheme,
            fallback = colorScheme.surfaceContainerLow
        )

        else -> colorScheme.surfaceContainerLow
    }
    val effectiveBackground = containerColor.compositeOver(colorScheme.surface)
    val accentOverride = noteColor?.let(::Color)
    val previewPalette = remember(effectiveBackground, colorScheme, accentOverride) {
        resolveMarkdownColorPalette(
            colorScheme = colorScheme,
            backgroundColor = effectiveBackground,
            accentColorOverride = accentOverride
        )
    }
    val iconContainerColor = remember(file.isDirectory, colorScheme, accentOverride) {
        when {
            file.isDirectory -> colorScheme.primaryContainer
            accentOverride != null -> {
                val alpha = if (colorScheme.surface.luminance() < 0.5f) 0.36f else 0.22f
                accentOverride.copy(alpha = alpha).compositeOver(colorScheme.surfaceContainerHigh)
            }

            else -> colorScheme.secondaryContainer
        }
    }
    val iconTintColor =
        remember(file.isDirectory, colorScheme, iconContainerColor, accentOverride) {
            when {
                file.isDirectory -> colorScheme.primary
                accentOverride != null -> {
                    resolveMarkdownColorPalette(
                        colorScheme = colorScheme,
                        backgroundColor = iconContainerColor,
                        accentColorOverride = accentOverride
                    ).accent
                }

                else -> colorScheme.secondary
            }
        }

    val interactionSource =
        remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    val card = @Composable {
        val cardShape = MaterialTheme.shapes.medium
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .combinedClickable(
                    onClick = {
                        haptic.performHeavyClick()
                        onClick()
                    },
                    onLongClick = {
                        haptic.performHeavyClick()
                        onLongClick()
                    },
                    interactionSource = interactionSource,
                    indication = ripple()
                ),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = when {
                isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                isKeyboardSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                else -> null
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    // Show filename with extension in reduced opacity
                    if (file.isDirectory) {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(Res.string.folder),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        val baseName = file.name.substringBeforeLast(".")
                        val ext = if (file.name.contains(".")) ".${file.extension}" else ""
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SharedElementContainer(
                                key = SharedTransitionKeys.fileTitle(file.path.toString()),
                                isSource = true
                            ) {
                                Text(
                                    text = baseName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = ext,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }

                        val effectivePreview = preview?.takeIf { it.isNotBlank() }
                            ?: file.preview?.takeIf { it.isNotBlank() }
                        if (!effectivePreview.isNullOrEmpty()) {
                            val previewText = remember(
                                effectivePreview,
                                colorScheme,
                                effectiveBackground,
                                accentOverride
                            ) {
                                renderGridMarkdown(
                                    effectivePreview,
                                    colorScheme,
                                    backgroundColor = effectiveBackground,
                                    accentColorOverride = accentOverride
                                )
                            }
                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.bodySmall,
                                color = previewPalette.body,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            haptic.performLightClick()
                            onClick()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                } else if (isTrashMode) {
                    // Trash mode: show restore and delete permanently actions
                    Row {
                        IconButton(
                            onClick = {
                                haptic.performSuccess()
                                onRestore?.invoke()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = stringResource(Res.string.restore),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                haptic.performHeavyClick()
                                onDeletePermanently?.invoke()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = stringResource(Res.string.delete_permanently),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Row {
                        // Favorite star indicator
                        if (isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = stringResource(Res.string.favorite),
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        haptic.performHeavyClick()
                                        // Toggle favorite handled by parent
                                    }
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        IconButton(
                            onClick = {
                                haptic.performHeavyClick()
                                onMoreClick()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(Res.string.more),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (!file.isDirectory) {
        SharedElementContainer(
            key = SharedTransitionKeys.fileCard(file.path.toString()),
            isSource = true,
            useSharedBounds = true
        ) {
            card()
        }
    } else {
        card()
    }
}
