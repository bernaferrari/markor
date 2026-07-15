package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.remarkor.ui.components.FocusModeOverlay
import com.bernaferrari.remarkor.ui.components.MarkdownVisualTransformation
import com.bernaferrari.remarkor.ui.components.calculateParagraphAlpha
import com.bernaferrari.remarkor.ui.components.getCurrentParagraphIndex
import com.bernaferrari.remarkor.ui.components.SharedElementContainer
import com.bernaferrari.remarkor.ui.components.SharedTransitionKeys
import com.bernaferrari.remarkor.ui.components.resolveMarkdownColorPalette
import com.bernaferrari.remarkor.ui.theme.MarkorTheme

@Composable
internal fun EditorTab(
    filePath: String,
    title: String,
    content: TextFieldValue,
    showLineNumbers: Boolean,
    editorFontSize: Int,
    wordWrap: Boolean,
    surfaceColor: Color,
    noteAccentColor: Color?,
    isFocusMode: Boolean = false,
    focusRequestNonce: Int = 0,
    enableSharedElements: Boolean = true,
    autoFocusOnStart: Boolean = false,
    onAutoFocusConsumed: () -> Unit = {},
    onTitleChange: (String) -> Unit,
    onTitleCommit: () -> Unit,
    onContentChange: (TextFieldValue) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val editorLineHeightMultiplier = 1.55f
    val markdownPalette = remember(colorScheme, surfaceColor, noteAccentColor) {
        resolveMarkdownColorPalette(
            colorScheme = colorScheme,
            backgroundColor = surfaceColor,
            accentColorOverride = noteAccentColor
        )
    }
    val editorScrollState = rememberScrollState()
    val focusRequester = remember(filePath) { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var didAutoFocus by remember(filePath) { mutableStateOf(false) }
    val editorTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = editorFontSize.sp,
        lineHeight = (editorFontSize * editorLineHeightMultiplier).sp,
        color = markdownPalette.body,
        letterSpacing = 0.sp
    )
    val titleTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val lineNumberGutterWidth = 44.dp
    val editorLineHeight = (editorFontSize * editorLineHeightMultiplier).sp
    val currentLineIndex = remember(content.text, content.selection.start, isFocusMode) {
        if (isFocusMode) getCurrentParagraphIndex(content.text, content.selection.start) else 0
    }
    val markdownTransform = remember(
        colorScheme,
        surfaceColor,
        editorFontSize,
        noteAccentColor,
        isFocusMode,
        currentLineIndex,
    ) {
        MarkdownVisualTransformation(
            colorScheme = colorScheme,
            backgroundColor = surfaceColor,
            editorFontSize = editorFontSize,
            editorLineHeightMultiplier = editorLineHeightMultiplier,
            accentColorOverride = noteAccentColor,
            isFocusMode = isFocusMode,
            currentLineIndex = currentLineIndex,
        )
    }
    var lineNumberMetadata by remember(showLineNumbers, content.text) {
        mutableStateOf(
            if (showLineNumbers) {
                buildLineNumberMetadata(content.text)
            } else {
                emptyList()
            }
        )
    }

    LaunchedEffect(autoFocusOnStart, didAutoFocus) {
        if (autoFocusOnStart && !didAutoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
            didAutoFocus = true
            onAutoFocusConsumed()
        }
    }

    LaunchedEffect(focusRequestNonce) {
        if (focusRequestNonce > 0) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    FocusModeOverlay(enabled = isFocusMode) {
    SharedElementContainer(
        key = SharedTransitionKeys.fileCard(filePath),
        isSource = false,
        useSharedBounds = true,
        enabled = enableSharedElements,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = MarkorTheme.spacing.large,
                    end = MarkorTheme.spacing.large,
                    bottom = MarkorTheme.spacing.large
                )
                .background(
                    surfaceColor,
                    MaterialTheme.shapes.large
                )
                .padding(
                    start = if (showLineNumbers) 0.dp else MarkorTheme.spacing.large,
                    end = MarkorTheme.spacing.medium,
                    top = MarkorTheme.spacing.small,
                    bottom = MarkorTheme.spacing.medium
                )
        ) {
            val bodyMinHeight = (maxHeight - 88.dp).coerceAtLeast(220.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(editorScrollState)
            ) {
                SharedElementContainer(
                    key = SharedTransitionKeys.fileTitle(filePath),
                    isSource = false,
                    enabled = enableSharedElements,
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        singleLine = true,
                        textStyle = titleTextStyle.copy(color = markdownPalette.body),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onTitleCommit()
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (showLineNumbers) lineNumberGutterWidth + MarkorTheme.spacing.medium else 0.dp,
                                top = MarkorTheme.spacing.small,
                                bottom = MarkorTheme.spacing.small
                            )
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown &&
                                    (event.key == Key.Enter || event.key == Key.NumPadEnter)
                                ) {
                                    onTitleCommit()
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                    true
                                } else {
                                    false
                                }
                            },
                        decorationBox = { innerTextField ->
                            Box {
                                if (title.isBlank()) {
                                    Text(
                                        text = "Title",
                                        style = titleTextStyle,
                                        color = markdownPalette.subtle
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(
                        start = if (showLineNumbers) lineNumberGutterWidth + MarkorTheme.spacing.medium else 0.dp,
                        end = MarkorTheme.spacing.extraSmall,
                        top = MarkorTheme.spacing.extraSmall,
                        bottom = MarkorTheme.spacing.medium
                    ),
                    color = markdownPalette.accent.copy(alpha = 0.36f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    if (showLineNumbers) {
                        val lineNumberBackground = darkenColor(surfaceColor, 0.90f)
                        val density = LocalDensity.current
                        val editorLineHeightPx = with(density) { editorLineHeight.toPx() }
                        Column(
                            modifier = Modifier
                                .width(lineNumberGutterWidth)
                                .fillMaxHeight()
                                .heightIn(min = bodyMinHeight)
                                .background(
                                    lineNumberBackground,
                                    RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 12.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 12.dp
                                    )
                                )
                                .padding(end = MarkorTheme.spacing.small)
                        ) {
                            lineNumberMetadata.forEachIndexed { index, line ->
                                val gutterLineHeightPx = if (line.lineHeightPx > 0f) {
                                    line.lineHeightPx
                                } else {
                                    editorLineHeightPx
                                }
                                val gutterLineHeightDp = with(density) { gutterLineHeightPx.toDp() }
                                val lineAlpha = if (isFocusMode && line.number != null) {
                                    calculateParagraphAlpha(line.number - 1, currentLineIndex, true)
                                } else {
                                    1f
                                }
                                val numberLineStyle = editorTextStyle.copy(
                                    fontSize = editorFontSize.sp,
                                    color = if (line.number == null) {
                                        Color.Transparent
                                    } else {
                                        markdownPalette.subtle.copy(alpha = lineAlpha)
                                    },
                                    textAlign = TextAlign.End
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(gutterLineHeightDp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = line.number?.toString().orEmpty(),
                                        style = numberLineStyle,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                    BasicTextField(
                        value = content,
                        onValueChange = onContentChange,
                        textStyle = editorTextStyle,
                        cursorBrush = SolidColor(markdownPalette.accent),
                        visualTransformation = markdownTransform,
                        onTextLayout = { layoutResult ->
                            if (showLineNumbers) {
                                val updated = buildWrappedLineNumberMetadata(
                                    text = content.text,
                                    layoutResult = layoutResult
                                )
                                if (updated != lineNumberMetadata) {
                                    lineNumberMetadata = updated
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(start = if (showLineNumbers) MarkorTheme.spacing.medium else 0.dp)
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .heightIn(min = bodyMinHeight)
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                                    if (handleSmartEnter(content, onContentChange)) {
                                        return@onPreviewKeyEvent true
                                    }
                                }
                                false
                            },
                        singleLine = !wordWrap,
                    )
                }
            }
        }
    }
    }
}


internal fun handleSmartEnter(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
): Boolean {
    val text = value.text
    val selection = value.selection
    if (!selection.collapsed) return false

    // Find the bounds of the current line
    var lineStart = selection.start
    while (lineStart > 0 && text[lineStart - 1] != '\n') {
        lineStart--
    }
    val currentLine = text.substring(lineStart, selection.start)

    // Regex patterns for lists
    val taskRegex = """^(\s*)([-*+])\s*\[([ xX]?)\]\s*(.*)$""".toRegex()
    val bulletRegex = """^(\s*)([-*+])\s+(.*)$""".toRegex()
    val numberedRegex = """^(\s*)(\d+)\.\s+(.*)$""".toRegex()

    val taskMatch = taskRegex.find(currentLine)
    val bulletMatch = bulletRegex.find(currentLine)
    val numberedMatch = numberedRegex.find(currentLine)

    return when {
        taskMatch != null -> {
            val whitespace = taskMatch.groupValues[1]
            val marker = taskMatch.groupValues[2]
            val content = taskMatch.groupValues[4]
            if (content.isEmpty()) {
                // Exit list: Clear the marker and insert newline
                val newText = text.removeRange(lineStart, selection.start) + "\n"
                onValueChange(value.copy(text = newText, selection = TextRange(lineStart + 1)))
            } else {
                // Continue list
                val insert = "\n$whitespace$marker [ ] "
                val newText = text.replaceRange(selection.start, selection.start, insert)
                onValueChange(
                    value.copy(
                        text = newText,
                        selection = TextRange(selection.start + insert.length)
                    )
                )
            }
            true
        }

        bulletMatch != null -> {
            val whitespace = bulletMatch.groupValues[1]
            val marker = bulletMatch.groupValues[2]
            val content = bulletMatch.groupValues[3]
            if (content.isEmpty()) {
                val newText = text.removeRange(lineStart, selection.start) + "\n"
                onValueChange(value.copy(text = newText, selection = TextRange(lineStart + 1)))
            } else {
                val insert = "\n$whitespace$marker "
                val newText = text.replaceRange(selection.start, selection.start, insert)
                onValueChange(
                    value.copy(
                        text = newText,
                        selection = TextRange(selection.start + insert.length)
                    )
                )
            }
            true
        }

        numberedMatch != null -> {
            val whitespace = numberedMatch.groupValues[1]
            val number = numberedMatch.groupValues[2].toInt()
            val content = numberedMatch.groupValues[3]
            if (content.isEmpty()) {
                val newText = text.removeRange(lineStart, selection.start) + "\n"
                onValueChange(value.copy(text = newText, selection = TextRange(lineStart + 1)))
            } else {
                val insert = "\n$whitespace${number + 1}. "
                val newText = text.replaceRange(selection.start, selection.start, insert)
                onValueChange(
                    value.copy(
                        text = newText,
                        selection = TextRange(selection.start + insert.length)
                    )
                )
            }
            true
        }

        else -> false
    }
}
