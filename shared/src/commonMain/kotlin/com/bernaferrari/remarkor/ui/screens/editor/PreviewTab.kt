package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.bernaferrari.remarkor.ui.components.SharedElementContainer
import com.bernaferrari.remarkor.ui.components.SharedTransitionKeys
import com.bernaferrari.remarkor.ui.components.resolveMarkdownColorPalette
import com.bernaferrari.remarkor.ui.theme.MarkorTheme
import com.bernaferrari.remarkor.util.resolveImageUrl
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.nothing_to_preview
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreviewTab(
    filePath: String,
    title: String,
    content: String,
    backgroundColor: Color,
    noteAccentColor: Color?,
    onTapToEdit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val markdownPalette = remember(colorScheme, backgroundColor, noteAccentColor) {
        resolveMarkdownColorPalette(
            colorScheme = colorScheme,
            backgroundColor = backgroundColor,
            accentColorOverride = noteAccentColor
        )
    }
    val previewBlocks = remember(content, filePath) {
        buildPreviewBlocks(content, filePath)
    }
    val emptyMessage = stringResource(Res.string.nothing_to_preview)
    val styledText =
        remember<AnnotatedString>(content, colorScheme, backgroundColor, noteAccentColor) {
            com.bernaferrari.remarkor.ui.components.renderCleanMarkdown(
                if (content.isEmpty()) emptyMessage else content,
                colorScheme,
                backgroundColor,
                accentColorOverride = noteAccentColor
            )
        }

    SharedElementContainer(
        key = SharedTransitionKeys.fileCard(filePath),
        isSource = false,
        useSharedBounds = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MarkorTheme.spacing.medium)
                .background(backgroundColor, MaterialTheme.shapes.large)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onTapToEdit() })
                }
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (title.isNotBlank()) {
                SharedElementContainer(
                    key = SharedTransitionKeys.fileTitle(filePath),
                    isSource = false
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = markdownPalette.body
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (previewBlocks.none { it is PreviewBlock.Image || it is PreviewBlock.HorizontalRule }) {
                Text(
                    text = styledText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 32.sp,
                        color = if (content.isEmpty()) markdownPalette.subtle else markdownPalette.body
                    )
                )
            } else {
                val context = LocalPlatformContext.current
                previewBlocks.forEach { block ->
                    when (block) {
                        is PreviewBlock.Text -> {
                            if (block.content.isBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                val lineText =
                                    remember(
                                        block.content,
                                        colorScheme,
                                        backgroundColor,
                                        noteAccentColor
                                    ) {
                                        com.bernaferrari.remarkor.ui.components.renderCleanMarkdown(
                                            block.content,
                                            colorScheme,
                                            backgroundColor,
                                            accentColorOverride = noteAccentColor
                                        )
                                    }
                                Text(
                                    text = lineText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 30.sp,
                                        color = markdownPalette.body
                                    )
                                )
                            }
                        }

                        is PreviewBlock.Image -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(block.source)
                                    .build(),
                                contentDescription = block.altText,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 320.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        is PreviewBlock.HorizontalRule -> {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                color = markdownPalette.accent.copy(alpha = 0.42f)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal sealed interface PreviewBlock {
    data class Text(val content: String) : PreviewBlock
    data class Image(val source: String, val altText: String?) : PreviewBlock
    data object HorizontalRule : PreviewBlock
}

internal val markdownImageRegex = Regex("!\\[([^\\]]*)\\]\\(([^)]+)\\)")
internal val markdownHorizontalRuleRegex =
    Regex("^[ \\t]{0,3}(?:(?:\\*[ \\t]*){3,}|(?:-[ \\t]*){3,}|(?:_[ \\t]*){3,})$")

internal fun buildPreviewBlocks(content: String, filePath: String): List<PreviewBlock> {
    if (content.isEmpty()) return emptyList()

    val blocks = mutableListOf<PreviewBlock>()
    content.lineSequence().forEach { line ->
        if (markdownHorizontalRuleRegex.matches(line)) {
            blocks.add(PreviewBlock.HorizontalRule)
            return@forEach
        }

        val matches = markdownImageRegex.findAll(line).toList()
        if (matches.isEmpty()) {
            blocks.add(PreviewBlock.Text(line))
            return@forEach
        }

        var cursor = 0
        matches.forEach { match ->
            val before = line.substring(cursor, match.range.first)
            if (before.isNotEmpty()) {
                blocks.add(PreviewBlock.Text(before))
            }

            val altText = match.groupValues[1].ifBlank { null }
            val rawPath = match.groupValues[2].trim()
            val resolvedPath = resolvePreviewImageSource(rawPath, filePath)
            if (!resolvedPath.isNullOrEmpty()) {
                blocks.add(PreviewBlock.Image(source = resolvedPath, altText = altText))
            }
            cursor = match.range.last + 1
        }

        val after = line.substring(cursor)
        if (after.isNotEmpty()) {
            blocks.add(PreviewBlock.Text(after))
        }
    }
    return blocks
}

internal fun resolvePreviewImageSource(imagePath: String, filePath: String): String? {
    val source =
        imagePath.trim().removeSurrounding("<", ">").takeIf { it.isNotEmpty() } ?: return null
    if (
        source.startsWith("http://") ||
        source.startsWith("https://") ||
        source.startsWith("file://") ||
        source.startsWith("content://") ||
        source.startsWith("/")
    ) {
        return source
    }

    return runCatching {
        resolveImageUrl(source, filePath.toPath())
    }.getOrNull() ?: source
}
