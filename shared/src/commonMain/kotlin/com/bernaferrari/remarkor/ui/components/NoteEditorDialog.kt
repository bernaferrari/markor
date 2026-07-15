package com.bernaferrari.remarkor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bernaferrari.remarkor.ui.screens.EditorScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Keep-style note editor overlay for large screens.
 * Simple fade via parent [androidx.compose.animation.AnimatedVisibility]
 * (sharedBounds was too CPU-heavy on web).
 */
@Composable
fun NoteEditorDialog(
    filePath: String,
    openKeyboardOnStart: Boolean = false,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    // Closing outside the card must not bypass the editor's save/rename commit.
                    onClick = {},
                ),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val maxDialogHeight = maxHeight * 0.92f

            Surface(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .heightIn(max = maxDialogHeight)
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
            ) {
                EditorScreen(
                    filePath = filePath,
                    openKeyboardOnStart = openKeyboardOnStart,
                    onNavigateBack = onDismiss,
                    embeddedInDialog = true,
                    viewModel = koinViewModel(key = filePath),
                )
            }
        }
    }
}
