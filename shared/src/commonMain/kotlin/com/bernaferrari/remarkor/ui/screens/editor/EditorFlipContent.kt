package com.bernaferrari.remarkor.ui.screens.editor

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.TextFieldValue

@Composable
internal fun EditorFlipContent(
    isPreviewMode: Boolean,
    activeFilePath: String,
    titleInput: String,
    content: TextFieldValue,
    showLineNumbers: Boolean,
    editorFontSize: Int,
    wordWrap: Boolean,
    surfaceColor: Color,
    noteAccentColor: Color?,
    editorFocusNonce: Int,
    autoFocusOnStart: Boolean,
    onAutoFocusConsumed: () -> Unit,
    onTitleChange: (String) -> Unit,
    onTitleCommit: () -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onTapToEdit: () -> Unit,
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPreviewMode) {
        rotation.animateTo(
            targetValue = if (isPreviewMode) 180f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val isBack = rotation.value > 90f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 12f * density
                    if (isBack) {
                        rotationY = rotation.value - 180f
                    }
                },
        ) {
            if (isBack) {
                PreviewTab(
                    filePath = activeFilePath,
                    title = titleInput,
                    content = content.text,
                    backgroundColor = surfaceColor,
                    noteAccentColor = noteAccentColor,
                    onTapToEdit = onTapToEdit,
                )
            } else {
                EditorTab(
                    filePath = activeFilePath,
                    title = titleInput,
                    content = content,
                    showLineNumbers = showLineNumbers,
                    editorFontSize = editorFontSize,
                    wordWrap = wordWrap,
                    surfaceColor = surfaceColor,
                    noteAccentColor = noteAccentColor,
                    focusRequestNonce = editorFocusNonce,
                    autoFocusOnStart = autoFocusOnStart,
                    onAutoFocusConsumed = onAutoFocusConsumed,
                    onTitleChange = onTitleChange,
                    onTitleCommit = onTitleCommit,
                    onContentChange = onContentChange,
                )
            }
        }
    }
}