package net.gsantner.markor.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Render markdown for Grid/List preview.
 * Uses the same rendering as view mode for consistency.
 */
fun renderGridMarkdown(text: String, colorScheme: ColorScheme): AnnotatedString {
    return renderMarkdown(text, colorScheme, isGridPreview = true)
}

/**
 * Render clean markdown for View Mode (editor preview).
 */
fun renderCleanMarkdown(text: String, colorScheme: ColorScheme): AnnotatedString {
    return renderMarkdown(text, colorScheme, isGridPreview = false)
}

/**
 * Unified markdown renderer that properly strips markers.
 */
private fun renderMarkdown(text: String, colorScheme: ColorScheme, isGridPreview: Boolean): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val lines = text.split("\n")
    
    lines.forEachIndexed { index, line ->
        if (index > 0) builder.append("\n")
        
        var content = line
        var lineStyle: SpanStyle? = null
        
        // Headers - strip # and apply sizing
        val headerMatch = Regex("^(#{1,6})[ \\t]+(.+)$").find(content)
        if (headerMatch != null) {
            val level = headerMatch.groupValues[1].length
            content = headerMatch.groupValues[2]
            val fontSize = when (level) {
                1 -> if (isGridPreview) 14.sp else 24.sp
                2 -> if (isGridPreview) 13.sp else 22.sp
                3 -> if (isGridPreview) 12.sp else 20.sp
                else -> if (isGridPreview) 11.sp else 18.sp
            }
            lineStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize, color = colorScheme.primary)
        }
        
        // Blockquotes - strip >
        val quoteMatch = Regex("^>\\s*(.*)$").find(content)
        if (quoteMatch != null) {
            content = quoteMatch.groupValues[1]
            lineStyle = SpanStyle(fontStyle = FontStyle.Italic, color = colorScheme.onSurfaceVariant)
        }
        
        // Lists - replace markers with bullet
        val listMatch = Regex("^[-*+]\\s+(.+)$").find(content)
        if (listMatch != null) {
            content = "• " + listMatch.groupValues[1]
        }
        
        // Numbered lists
        val numberedMatch = Regex("^(\\d+)\\.\\s+(.+)$").find(content)
        if (numberedMatch != null) {
            content = numberedMatch.groupValues[1] + ". " + numberedMatch.groupValues[2]
        }
        
        // Checkboxes
        val taskMatch = Regex("^-?\\s?\\[([ xX])\\]\\s+(.+)$").find(content)
        if (taskMatch != null) {
            val isChecked = taskMatch.groupValues[1].lowercase() == "x"
            val checkbox = if (isChecked) "☑ " else "☐ "
            content = checkbox + taskMatch.groupValues[2]
        }
        
        // Skip empty lines
        if (content.isEmpty()) return@forEachIndexed
        
        // Process inline styles and build clean string
        val cleanResult = processInlineStyles(content, colorScheme)
        
        val start = builder.length
        builder.append(cleanResult.text)
        
        // Apply line-level style first
        if (lineStyle != null) {
            builder.addStyle(lineStyle, start, builder.length)
        }
        
        // Apply inline styles with adjusted offsets
        cleanResult.styles.forEach { (style, range) ->
            builder.addStyle(style, start + range.first, start + range.last + 1)
        }
    }
    
    return builder.toAnnotatedString()
}

private data class InlineResult(
    val text: String,
    val styles: List<Pair<SpanStyle, IntRange>>
)

/**
 * Process inline markdown and return clean text with style ranges.
 * This properly removes markers instead of making them transparent.
 */
private fun processInlineStyles(text: String, colorScheme: ColorScheme): InlineResult {
    val styles = mutableListOf<Pair<SpanStyle, IntRange>>()
    var result = text
    var offset = 0
    
    // Process in order: strikethrough, bold, italic, code
    // Each pass updates 'result' and tracks style positions
    
    // Strikethrough ~~text~~
    Regex("~~(.+?)~~").findAll(result).toList().reversed().forEach { match ->
        val content = match.groupValues[1]
        val startPos = match.range.first
        val endPos = startPos + content.length - 1
        
        // Replace ~~content~~ with content
        result = result.substring(0, match.range.first) + content + result.substring(match.range.last + 1)
        
        styles.add(SpanStyle(textDecoration = TextDecoration.LineThrough) to (startPos..endPos))
    }
    
    // Bold **text** or __text__
    Regex("(\\*\\*|__)(.+?)\\1").findAll(result).toList().reversed().forEach { match ->
        val content = match.groupValues[2]
        val startPos = match.range.first
        val endPos = startPos + content.length - 1
        
        result = result.substring(0, match.range.first) + content + result.substring(match.range.last + 1)
        
        styles.add(SpanStyle(fontWeight = FontWeight.Bold) to (startPos..endPos))
    }
    
    // Italic *text* or _text_ (avoiding ** and __)
    Regex("(?<!\\*)(\\*)(?!\\*)(.+?)(?<!\\*)(\\*)(?!\\*)|(?<!_)(_)(?!_)(.+?)(?<!_)(_)(?!_)").findAll(result).toList().reversed().forEach { match ->
        val content = if (match.groupValues[2].isNotEmpty()) match.groupValues[2] else match.groupValues[5]
        val startPos = match.range.first
        val endPos = startPos + content.length - 1
        
        result = result.substring(0, match.range.first) + content + result.substring(match.range.last + 1)
        
        styles.add(SpanStyle(fontStyle = FontStyle.Italic) to (startPos..endPos))
    }
    
    // Inline code `text`
    Regex("`([^`]+)`").findAll(result).toList().reversed().forEach { match ->
        val content = match.groupValues[1]
        val startPos = match.range.first
        val endPos = startPos + content.length - 1
        
        result = result.substring(0, match.range.first) + content + result.substring(match.range.last + 1)
        
        styles.add(SpanStyle(
            fontFamily = FontFamily.Monospace,
            background = colorScheme.surfaceVariant.copy(alpha = 0.5f),
            color = colorScheme.onSurfaceVariant
        ) to (startPos..endPos))
    }
    
    return InlineResult(result, styles)
}

