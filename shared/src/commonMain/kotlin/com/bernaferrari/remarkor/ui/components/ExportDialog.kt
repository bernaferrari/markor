package com.bernaferrari.remarkor.ui.components

import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.StringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.bernaferrari.remarkor.domain.service.ShareService
import com.bernaferrari.remarkor.ui.theme.MarkorTheme
import com.bernaferrari.remarkor.util.nowMillis
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

data class ExportOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ExportDialog(
    filePath: String,
    fileName: String,
    markdownContent: String,
    onDismiss: () -> Unit,
    onShareHtml: () -> Unit = {},
    onPrint: () -> Unit = {},
    onShareMarkdown: () -> Unit = {},
    shareService: ShareService = koinInject()
) {
    var isExporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val exportOptions = remember {
        listOf(
            ExportOption(
                title = "Share as Markdown",
                subtitle = "Share the raw markdown file",
                icon = Icons.Default.Description,
                onClick = {
                    scope.launch {
                        isExporting = true
                        try {
                            shareService.shareFile(
                                fileName = fileName,
                                content = markdownContent.encodeToByteArray(),
                                title = "Share as Markdown",
                                mimeType = "text/markdown"
                            )
                            onShareMarkdown()
                        } finally {
                            isExporting = false
                        }
                    }
                }
            ),
            ExportOption(
                title = "Share as Text",
                subtitle = "Share as plain text",
                icon = Icons.AutoMirrored.Filled.TextSnippet,
                onClick = {
                    scope.launch {
                        isExporting = true
                        try {
                            shareService.shareText(markdownContent, "Share Text")
                            onShareMarkdown()
                        } finally {
                            isExporting = false
                        }
                    }
                }
            ),
            ExportOption(
                title = "Copy to Clipboard",
                subtitle = "Copy content to clipboard",
                icon = Icons.Default.ContentCopy,
                onClick = {
                    onShareMarkdown()
                }
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = stringResource(Res.string.export),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(Res.string.export),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Export options
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exportOptions) { option ->
                        ExportOptionItem(
                            option = option,
                            isExporting = isExporting
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun ExportOptionItem(
    option: ExportOption,
    isExporting: Boolean
) {
    Card(
        onClick = { if (!isExporting) option.onClick() },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

/**
 * Generate a simple HTML document from markdown content.
 * Multiplatform-compatible version.
 */
fun generateSimpleHtml(title: String, markdown: String): String {
    val processedHtml = markdown
        .replace(Regex("\\*\\*(.+?)\\*\\*")) { "<b>${it.groupValues[1]}</b>" }
        .replace(Regex("\\*(.+?)\\*")) { "<i>${it.groupValues[1]}</i>" }
        .replace(Regex("`(.+?)`")) { "<code>${it.groupValues[1]}</code>" }
        .replace(Regex("```\\n([\\s\\S]+?)```")) { "<pre>${it.groupValues[1]}</pre>" }
        .replace(Regex("^(#{1})\\s+(.+)$", RegexOption.MULTILINE)) { "<h1>${it.groupValues[2]}</h1>" }
        .replace(Regex("^(#{2})\\s+(.+)$", RegexOption.MULTILINE)) { "<h2>${it.groupValues[2]}</h2>" }
        .replace(Regex("^(#{3})\\s+(.+)$", RegexOption.MULTILINE)) { "<h3>${it.groupValues[2]}</h3>" }
        .replace(Regex("^>\\s+(.+)$", RegexOption.MULTILINE)) { "<blockquote>${it.groupValues[1]}</blockquote>" }
        .replace(Regex("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")) { "<a href=\"${it.groupValues[2]}\">${it.groupValues[1]}</a>" }
        .replace("\n", "<br/>")
    
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>$title</title>
    <style>
        body { font-family: sans-serif; line-height: 1.6; padding: 20px; max-width: 800px; margin: 0 auto; }
        h1 { color: #333; border-bottom: 1px solid #ddd; }
        h2 { color: #444; }
        h3 { color: #555; }
        code { background: #f4f4f4; padding: 2px 6px; border-radius: 3px; }
        pre { background: #f4f4f4; padding: 15px; overflow-x: auto; }
        blockquote { border-left: 3px solid #667eea; margin: 0; padding-left: 15px; color: #666; }
    </style>
</head>
<body>
    <h1>$title</h1>
    $processedHtml
</body>
</html>
"""
}

/**
 * Format timestamp for display (multiplatform-compatible)
 */
fun formatExportTimestamp(): String {
    val now = Instant.fromEpochMilliseconds(nowMillis())
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.month.name.take(3)} ${now.day}, ${now.year} at ${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
}
