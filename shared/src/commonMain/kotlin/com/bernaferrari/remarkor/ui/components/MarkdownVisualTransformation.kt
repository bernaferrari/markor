package com.bernaferrari.remarkor.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.sp

class MarkdownVisualTransformation(
    private val colorScheme: androidx.compose.material3.ColorScheme,
    private val backgroundColor: Color = colorScheme.surface,
    private val editorFontSize: Int = 16,
    private val editorLineHeightMultiplier: Float = 1.45f,
    private val accentColorOverride: Color? = null
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            markdownToAnnotatedString(
                text.text,
                colorScheme,
                backgroundColor,
                editorFontSize,
                editorLineHeightMultiplier,
                accentColorOverride
            ),
            OffsetMapping.Identity
        )
    }
}

fun markdownToAnnotatedString(
    text: String,
    colorScheme: androidx.compose.material3.ColorScheme,
    backgroundColor: Color = colorScheme.surface,
    editorFontSize: Int = 16,
    editorLineHeightMultiplier: Float = 1.45f,
    accentColorOverride: Color? = null
): AnnotatedString {
    val builder = AnnotatedString.Builder(text)
    val palette = resolveMarkdownColorPalette(
        colorScheme = colorScheme,
        backgroundColor = backgroundColor,
        accentColorOverride = accentColorOverride
    )

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
        val headingLevel = match.groupValues[1].length
        val headingScale = when (headingLevel) {
            1 -> 1.75f
            2 -> 1.55f
            3 -> 1.35f
            4 -> 1.2f
            5 -> 1.1f
            6 -> 1.05f
            else -> 1f
        }
        val headingFontSize = kotlin.math.max(1, editorFontSize).sp * headingScale

            builder.addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = headingFontSize,
                    brush = SolidColor(palette.accent),
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
            builder.addStyle(
                style = ParagraphStyle(
                    lineHeight = headingFontSize * editorLineHeightMultiplier
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
    }

    // Links ([text](url))
    val linkRegex = Regex("\\[([^\\[\\]]+)\\]\\(([^\\)]+)\\)")
    linkRegex.findAll(text).forEach { match ->
        builder.addStyle(
            style = SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
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
                background = palette.codeBackground,
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
