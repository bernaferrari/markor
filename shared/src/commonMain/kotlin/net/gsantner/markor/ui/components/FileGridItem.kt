package net.gsantner.markor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.gsantner.markor.domain.repository.FileInfo
import net.gsantner.markor.ui.theme.MarkorTheme
import net.gsantner.markor.util.resolveImageUrl
import androidx.compose.material3.ripple
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.compose.LocalPlatformContext
import okio.Path

private val noteTextExtensions = setOf("md", "markdown", "txt", "org", "todo", "rst", "adoc")

private fun shouldShowExtensionPlaceholder(extension: String): Boolean {
    val normalizedExtension = extension.trim().lowercase()
    return normalizedExtension.isNotEmpty() && normalizedExtension !in noteTextExtensions
}

private fun resolveImagePreviewUrl(imagePreviewUrl: String?, filePath: Path): String? {
    val rawPath = imagePreviewUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (
        rawPath.startsWith("http://") ||
        rawPath.startsWith("https://") ||
        rawPath.startsWith("file://") ||
        rawPath.startsWith("content://") ||
        rawPath.startsWith("/")
    ) {
        return rawPath
    }
    return resolveImageUrl(rawPath, filePath) ?: rawPath
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    file: FileInfo,
    isSelected: Boolean,
    selectionMode: Boolean,
    isPinned: Boolean,
    color: Int? = null,
    imagePreviewUrl: String? = null,
    labels: List<String> = emptyList(),
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val noteSurfaceColor = color?.let {
        resolveNoteSurfaceColor(
            it,
            colorScheme,
            fallback = colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    }
    val containerColor = when {
        isSelected -> colorScheme.primaryContainer.copy(alpha = 0.4f)
        color != null -> colorScheme.surfaceContainerLow
        file.isDirectory -> colorScheme.surface
        else -> colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val contentSurfaceColor = if (isSelected) containerColor else noteSurfaceColor ?: containerColor
    val effectiveBackground = contentSurfaceColor.compositeOver(colorScheme.surface)
    val accentOverride = color?.let(::Color)
    val previewPalette = remember(effectiveBackground, colorScheme, accentOverride) {
        resolveMarkdownColorPalette(
            colorScheme = colorScheme,
            backgroundColor = effectiveBackground,
            accentColorOverride = accentOverride
        )
    }
    val resolvedImagePreviewUrl = remember(imagePreviewUrl, file.path) {
        resolveImagePreviewUrl(imagePreviewUrl, file.path)
    }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    // Haptic Feedback
    val hapticHelper = rememberHapticHelper()

    val card = @Composable {
        val cardShape = MaterialTheme.shapes.medium
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .combinedClickable(
                    onClick = {
                        hapticHelper.performLightClick()
                        onClick()
                    },
                    onLongClick = {
                        hapticHelper.performHeavyClick()
                        onLongClick()
                    },
                    interactionSource = interactionSource,
                    indication = ripple()
                ),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = MarkorTheme.elevation.level0),
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        ) {
            val isColorfulNote = !file.isDirectory && noteSurfaceColor != null
            val showMetaRow = !file.isDirectory && isPinned
            val accentColor = color?.let(::Color) ?: Color.Unspecified
            val contentPadding = if (isColorfulNote) {
                PaddingValues(
                    horizontal = MarkorTheme.spacing.medium,
                    vertical = MarkorTheme.spacing.small
                )
            } else {
                PaddingValues(MarkorTheme.spacing.medium)
            }

            val innerShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            )
            val contentModifier = if (isColorfulNote) {
                Modifier
                    .fillMaxWidth()
                    .background(accentColor)
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 0.dp)
                    .clip(innerShape)
                    .background(contentSurfaceColor)
                    .padding(contentPadding)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            }

            Column(modifier = contentModifier) {
                    if (showMetaRow) {
                        // Keep metadata minimal and focused.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (file.isDirectory) {
                        // Directory Layout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        // File Layout - Note Style
                        if (!resolvedImagePreviewUrl.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(MaterialTheme.shapes.large)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalPlatformContext.current)
                                        .data(resolvedImagePreviewUrl)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        SharedElementContainer(
                            key = SharedTransitionKeys.fileTitle(file.path.toString()),
                            isSource = true
                        ) {
                            Text(
                                text = file.name.substringBeforeLast("."),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = previewPalette.body
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        if (!file.preview.isNullOrEmpty()) {
                            val previewText = remember(file.preview, colorScheme, effectiveBackground, accentOverride) {
                                renderGridMarkdown(
                                    file.preview,
                                    colorScheme,
                                    backgroundColor = effectiveBackground,
                                    accentColorOverride = accentOverride
                                )
                            }
                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    letterSpacing = 0.1.sp
                                ),
                                color = previewPalette.body,
                                maxLines = 8,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        } else if (resolvedImagePreviewUrl.isNullOrEmpty() && shouldShowExtensionPlaceholder(file.extension)) {
                            // Non-text file placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = file.extension.uppercase(),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                )
                            }
                        }

                        // Labels row (Google Keep style)
                        if (labels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                labels.take(3).forEach { label ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.height(20.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (labels.size > 3) {
                                    Text(
                                        text = "+${labels.size - 3}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
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
