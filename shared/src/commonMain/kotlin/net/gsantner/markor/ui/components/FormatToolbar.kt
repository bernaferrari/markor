@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package net.gsantner.markor.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class EditorAction {
    BOLD, ITALIC, STRIKETHROUGH, CODE,
    LINK, IMAGE, HEADER,
    LIST_BULLET, LIST_NUMBERED, LIST_TASK,
    QUOTE, HORIZONTAL_RULE,
    UNDO, REDO, SEARCH
}

@Composable
fun FormatToolbar(
    onAction: (EditorAction) -> Unit
) {
    val cookieShape = MaterialShapes.ClamShell.toShape()
    val bottomInset = WindowInsets.navigationBars
        .union(WindowInsets.ime)
        .asPaddingValues()
        .calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            shape = RectangleShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 0.dp,
                    bottom = 8.dp + bottomInset,
                    start = 6.dp,
                    end = 6.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary actions: taller and expressive.
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveActionButton(
                        symbol = "B",
                        description = "Bold",
                        action = EditorAction.BOLD,
                        onAction = onAction,
                        shape = cookieShape,
                        fontWeight = FontWeight.ExtraBold
                    )
                    ExpressiveActionButton(
                        symbol = "I",
                        description = "Italic",
                        action = EditorAction.ITALIC,
                        onAction = onAction,
                        shape = cookieShape,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold
                    )
                    ExpressiveActionButton(
                        symbol = "—",
                        description = "Horizontal rule",
                        action = EditorAction.HORIZONTAL_RULE,
                        onAction = onAction,
                        shape = cookieShape
                    )
                    ExpressiveActionButton(
                        icon = Icons.Default.Link,
                        description = "Link",
                        action = EditorAction.LINK,
                        onAction = onAction,
                        shape = cookieShape
                    )
                }

                // Secondary actions: compact and quick.
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactActionButton(Icons.Default.FormatStrikethrough, "Strike", onAction, EditorAction.STRIKETHROUGH)
                    CompactActionButton(Icons.Default.Code, "Code", onAction, EditorAction.CODE)
                    CompactActionButton(Icons.Default.Title, "Header", onAction, EditorAction.HEADER)
                    CompactActionButton(Icons.Default.FormatQuote, "Quote", onAction, EditorAction.QUOTE)
                    CompactActionButton(Icons.Default.Image, "Image", onAction, EditorAction.IMAGE)
                    CompactActionButton(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet list", onAction, EditorAction.LIST_BULLET)
                    CompactActionButton(Icons.Default.FormatListNumbered, "Numbered list", onAction, EditorAction.LIST_NUMBERED)
                    CompactActionButton(Icons.Default.CheckBox, "Task list", onAction, EditorAction.LIST_TASK)
                }
            }
        }
    }
}

@Composable
private fun ExpressiveActionButton(
    description: String,
    action: EditorAction,
    onAction: (EditorAction) -> Unit,
    shape: Shape,
    symbol: String? = null,
    icon: ImageVector? = null,
    fontWeight: FontWeight = FontWeight.Medium,
    fontStyle: FontStyle = FontStyle.Normal
) {
    val hapticHelper = rememberHapticHelper()

    FilledTonalIconButton(
        onClick = {
            hapticHelper.performLightClick()
            onAction(action)
        },
        shape = shape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .widthIn(min = 58.dp)
            .height(50.dp)
    ) {
        when {
            symbol != null -> Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                )
            )
            icon != null -> Icon(icon, description, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    description: String,
    onAction: (EditorAction) -> Unit,
    action: EditorAction
) {
    val hapticHelper = rememberHapticHelper()
    
    FilledTonalIconButton(
        onClick = {
            hapticHelper.performLightClick()
            onAction(action)
        },
        shape = MaterialTheme.shapes.small,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Icon(icon, description, modifier = Modifier.size(20.dp))
    }
}
