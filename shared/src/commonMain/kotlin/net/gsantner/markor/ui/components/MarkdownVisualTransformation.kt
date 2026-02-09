package net.gsantner.markor.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily

class MarkdownVisualTransformation(
    private val colorScheme: androidx.compose.material3.ColorScheme
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(markdownToAnnotatedString(text.text, colorScheme), OffsetMapping.Identity)
    }
}

fun markdownToAnnotatedString(text: String, colorScheme: androidx.compose.material3.ColorScheme): AnnotatedString {
    val builder = AnnotatedString.Builder(text)

    // Bold (**text** or __text__)
    val boldRegex = Regex("(\\*\\*|__)(.*?)\\1")
    boldRegex.findAll(text).forEach { match ->
        builder.addStyle(
            style = SpanStyle(fontWeight = FontWeight.Bold),
            start = match.range.first,
            end = match.range.last + 1
        )
    }

    // Italic (*text* or _text_)
    val italicRegex = Regex("(?<!\\*)(\\*)(?![\\*\\s])(.+?)(?<![\\*\\s])(\\*)|(?<!_)(_)(?![_\\s])(.+?)(?<![_\\s])(_)")
    italicRegex.findAll(text).forEach { match ->
        builder.addStyle(
            style = SpanStyle(fontStyle = FontStyle.Italic),
            start = match.range.first,
            end = match.range.last + 1
        )
    }

    // Headers (# text)
    val headerRegex = Regex("^(#{1,6})[ \\t]+(.+)$", RegexOption.MULTILINE)
    headerRegex.findAll(text).forEach { match ->
        val hashes = match.groupValues[1]
        val fontSize = when (hashes.length) {
            1 -> 24.sp
            2 -> 22.sp
            3 -> 20.sp
            else -> 18.sp
        }
        builder.addStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = colorScheme.primary
            ),
            start = match.range.first,
            end = match.range.last + 1
        )
    }

    // Links ([text](url))
    val linkRegex = Regex("\\[([^\\[\\]]+)\\]\\(([^\\)]+)\\)")
    linkRegex.findAll(text).forEach { match ->
        builder.addStyle(
            style = SpanStyle(color = colorScheme.primary, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
            start = match.range.first,
            end = match.range.last + 1
        )
    }

    // Code code
    val codeRegex = Regex("`([^`]+)`")
    codeRegex.findAll(text).forEach { match ->
        builder.addStyle(
            style = SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                color = colorScheme.onSurfaceVariant
            ),
            start = match.range.first,
            end = match.range.last + 1
        )
    }
    
    // Blockquote (> text)
    val quoteRegex = Regex("^>\\s+(.+)$", RegexOption.MULTILINE)
    quoteRegex.findAll(text).forEach { match ->
        builder.addStyle(
            style = SpanStyle(
                fontStyle = FontStyle.Italic,
                color = colorScheme.onSurfaceVariant
            ),
            start = match.range.first,
            end = match.range.last + 1
        )
    }
    
    // Strikethrough (~~text~~)
    val strikeRegex = Regex("~~(.+?)~~")
    strikeRegex.findAll(text).forEach { match ->
            builder.addStyle(
            style = SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
            start = match.range.first,
            end = match.range.last + 1
        )
    }

    return builder.toAnnotatedString()
}
