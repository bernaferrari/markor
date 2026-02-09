package net.gsantner.markor.ui.components

import android.content.Context
import android.print.PrintManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import net.gsantner.markor.ui.theme.MarkorTheme
import okio.Path
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onShareHtml: () -> Unit,
    onPrint: () -> Unit,
    onShareMarkdown: () -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    
    val exportOptions = remember {
        listOf(
            ExportOption(
                title = "Share as HTML",
                subtitle = "Send rendered HTML to other apps",
                icon = Icons.Default.Code,
                onClick = {
                    isExporting = true
                    exportToHtml(context, fileName, markdownContent) { htmlFile ->
                        isExporting = false
                        shareFile(context, htmlFile, "Share HTML")
                        onShareHtml()
                    }
                }
            ),
            ExportOption(
                title = "Print / Save as PDF",
                subtitle = "Create a PDF document",
                icon = Icons.Default.Print,
                onClick = {
                    printDocument(context, fileName, markdownContent)
                    onPrint()
                }
            ),
            ExportOption(
                title = "Share as Markdown",
                subtitle = "Share the raw markdown file",
                icon = Icons.Default.Description,
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Export",
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

private fun exportToHtml(
    context: Context,
    fileName: String,
    markdown: String,
    onComplete: (File) -> Unit
) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val htmlFileName = fileName.substringBeforeLast(".") + "_$timestamp.html"
    val htmlFile = File(context.cacheDir, htmlFileName)
    
    val htmlContent = generateStyledHtml(fileName, markdown)
    
    FileOutputStream(htmlFile).use { stream ->
        stream.write(htmlContent.toByteArray())
    }
    
    onComplete(htmlFile)
}

private fun generateStyledHtml(title: String, markdown: String): String {
    // Simple markdown to HTML conversion with beautiful styling
    val processedHtml = markdown
        // Headers
        .replace(Regex("^(#{1})\\s+(.+)$", RegexOption.MULTILINE)) { "<h1>${it.groupValues[2]}</h1>" }
        .replace(Regex("^(#{2})\\s+(.+)$", RegexOption.MULTILINE)) { "<h2>${it.groupValues[2]}</h2>" }
        .replace(Regex("^(#{3})\\s+(.+)$", RegexOption.MULTILINE)) { "<h3>${it.groupValues[2]}</h3>" }
        // Bold
        .replace(Regex("\\*\\*(.+?)\\*\\*")) { "<strong>${it.groupValues[1]}</strong>" }
        .replace(Regex("__(.+?)__")) { "<strong>${it.groupValues[1]}</strong>" }
        // Italic
        .replace(Regex("\\*(.+?)\\*")) { "<em>${it.groupValues[1]}</em>" }
        .replace(Regex("_(.+?)_")) { "<em>${it.groupValues[1]}</em>" }
        // Inline code
        .replace(Regex("`(.+?)`")) { "<code>${it.groupValues[1]}</code>" }
        // Code blocks
        .replace(Regex("```\\n([\\s\\S]+?)```")) { "<pre><code>${it.groupValues[1]}</code></pre>" }
        // Blockquotes
        .replace(Regex("^>\\s+(.+)$", RegexOption.MULTILINE)) { "<blockquote>${it.groupValues[1]}</blockquote>" }
        // Links
        .replace(Regex("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")) { "<a href=\"${it.groupValues[2]}\">${it.groupValues[1]}</a>" }
        // Unordered lists
        .replace(Regex("^[-*+]\\s+(.+)$", RegexOption.MULTILINE)) { "• ${it.groupValues[1]}" }
        // Ordered lists
        .replace(Regex("^(\\d+)\\.\\s+(.+)$", RegexOption.MULTILINE)) { "${it.groupValues[1]}. ${it.groupValues[2]}" }
        // Task lists
        .replace(Regex("^\\s?\\[([ xX])\\]\\s+(.+)$", RegexOption.MULTILINE)) { 
            val checked = if (it.groupValues[1].lowercase() == "x") "☑" else "☐"
            "$checked ${it.groupValues[2]}" 
        }
        // Horizontal rule
        .replace(Regex("^[-*_]{3,}$", RegexOption.MULTILINE)) { "<hr/>" }
        // Line breaks
        .replace("\n", "<br/>")
    
    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
            line-height: 1.7;
            color: #1a1a2e;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 40px 20px;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 32px;
            text-align: center;
        }
        .header h1 {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        .header .date {
            font-size: 14px;
            opacity: 0.8;
        }
        .content {
            padding: 32px;
            font-size: 16px;
        }
        h1, h2, h3 {
            color: #667eea;
            margin-top: 24px;
            margin-bottom: 12px;
        }
        h1 { font-size: 28px; border-bottom: 2px solid #667eea; padding-bottom: 8px; }
        h2 { font-size: 24px; }
        h3 { font-size: 20px; }
        p { margin-bottom: 16px; }
        strong { color: #764ba2; }
        em { color: #555; }
        code {
            background: #f4f4f9;
            padding: 2px 8px;
            border-radius: 4px;
            font-family: 'SF Mono', Monaco, monospace;
            font-size: 14px;
            color: #e91e63;
        }
        pre {
            background: #1a1a2e;
            color: #eee;
            padding: 20px;
            border-radius: 8px;
            overflow-x: auto;
            margin: 16px 0;
        }
        pre code {
            background: none;
            color: inherit;
            padding: 0;
        }
        blockquote {
            border-left: 4px solid #667eea;
            padding-left: 16px;
            margin: 16px 0;
            color: #666;
            font-style: italic;
        }
        a { color: #667eea; text-decoration: none; }
        a:hover { text-decoration: underline; }
        hr {
            border: none;
            border-top: 2px dashed #ddd;
            margin: 24px 0;
        }
        ul, ol { margin-left: 24px; margin-bottom: 16px; }
        li { margin-bottom: 8px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>$title</h1>
            <div class="date">Exported on ${SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())}</div>
        </div>
        <div class="content">
            $processedHtml
        </div>
    </div>
</body>
</html>
"""
}

private fun printDocument(context: Context, fileName: String, markdown: String) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    
    val htmlContent = generateSimpleHtml(fileName, markdown)
    
    val printAdapter = object : android.print.PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: android.print.PrintAttributes?,
            newAttributes: android.print.PrintAttributes,
            cancellationSignal: android.os.CancellationSignal?,
            callback: android.print.PrintDocumentAdapter.LayoutResultCallback,
            osBundle: android.os.Bundle?
        ) {
            val printInfo = android.print.PrintDocumentInfo.Builder("markor_print")
                .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            callback.onLayoutFinished(printInfo, true)
        }
        
        override fun onWrite(
            pages: Array<android.print.PageRange>,
            destination: android.os.ParcelFileDescriptor,
            cancellationSignal: android.os.CancellationSignal?,
            callback: android.print.PrintDocumentAdapter.WriteResultCallback
        ) {
            try {
                val outputStream = FileOutputStream(destination.fileDescriptor)
                outputStream.write(htmlContent.toByteArray())
                outputStream.close()
                callback.onWriteFinished(arrayOf(android.print.PageRange(0, 0)))
            } catch (e: Exception) {
                callback.onWriteFailed(e.message)
            }
        }
    }
    
    val printAttributes = android.print.PrintAttributes.Builder()
        .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
        .setResolution(android.print.PrintAttributes.Resolution("300dpi", "300 dpi", 300, 300))
        .setColorMode(android.print.PrintAttributes.COLOR_MODE_COLOR)
        .build()
    
    printManager.print("Markor Document", printAdapter, printAttributes)
}

private fun generateSimpleHtml(title: String, markdown: String): String {
    // Strip HTML tags for print-friendly version
    val processedHtml = markdown
        .replace(Regex("\\*\\*(.+?)\\*\\*")) { "<b>${it.groupValues[1]}</b>" }
        .replace(Regex("\\*(.+?)\\*")) { "<i>${it.groupValues[1]}</i>" }
        .replace(Regex("`(.+?)`")) { "<code>${it.groupValues[1]}</code>" }
        .replace(Regex("```\\n([\\s\\S]+?)```")) { "<pre>${it.groupValues[1]}</pre>" }
        .replace(Regex("^(#{1})\\s+(.+)$", RegexOption.MULTILINE)) { "<h1>${it.groupValues[2]}</h1>" }
        .replace(Regex("^(#{2})\\s+(.+)$", RegexOption.MULTILINE)) { "<h2>${it.groupValues[2]}</h2>" }
        .replace(Regex("^(#{3})\\s+(.+)$", RegexOption.MULTILINE)) { "<h3>${it.groupValues[2]}</h3>" }
        .replace(Regex("^>\\s+(.+)$", RegexOption.MULTILINE)) { "<blockquote>${it.groupValues[1]}</blockquote>" }
        .replace(Regex("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")) { "<u>${it.groupValues[1]}</u>" }
        .replace("\n", "<br/>")
    
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>$title</title>
    <style>
        body { font-family: sans-serif; line-height: 1.6; padding: 20px; }
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

private fun shareFile(context: Context, file: File, title: String) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, title))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
