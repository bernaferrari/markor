package com.bernaferrari.remarkor.ui.components

import com.bernaferrari.remarkor.ui.icons.MaterialSymbols

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.bold
import markor.shared.generated.resources.strikethrough_label
import markor.shared.generated.resources.quote_label
import markor.shared.generated.resources.link
import markor.shared.generated.resources.image_label
import markor.shared.generated.resources.heading
import markor.shared.generated.resources.code
import markor.shared.generated.resources.bullet_list_label
import markor.shared.generated.resources.horizontal_rule
import markor.shared.generated.resources.italic
import markor.shared.generated.resources.numbered_list_label
import markor.shared.generated.resources.task_list
import org.jetbrains.compose.resources.stringResource

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
    val bottomInset = WindowInsets.navigationBars
        .union(WindowInsets.ime)
        .asPaddingValues()
        .calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
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
                // Primary actions: the inline styles people reach for most, larger and morphing on press.
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveActionButton(
                        symbol = "B",
                        description = stringResource(Res.string.bold),
                        action = EditorAction.BOLD,
                        onAction = onAction,
                        fontWeight = FontWeight.ExtraBold
                    )
                    ExpressiveActionButton(
                        symbol = "I",
                        description = stringResource(Res.string.italic),
                        action = EditorAction.ITALIC,
                        onAction = onAction,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold
                    )
                    ExpressiveActionButton(
                        icon = MaterialSymbols.Filled.FormatStrikethrough,
                        description = stringResource(Res.string.strikethrough_label),
                        action = EditorAction.STRIKETHROUGH,
                        onAction = onAction
                    )
                    ExpressiveActionButton(
                        icon = MaterialSymbols.Filled.Link,
                        description = stringResource(Res.string.link),
                        action = EditorAction.LINK,
                        onAction = onAction
                    )
                }

                // Secondary actions: block-level formatting, compact.
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactActionButton(
                        MaterialSymbols.Filled.Title,
                        stringResource(Res.string.heading),
                        onAction,
                        EditorAction.HEADER
                    )
                    CompactActionButton(
                        MaterialSymbols.AutoMirrored.Filled.FormatListBulleted,
                        stringResource(Res.string.bullet_list_label),
                        onAction,
                        EditorAction.LIST_BULLET
                    )
                    CompactActionButton(
                        MaterialSymbols.Filled.FormatListNumbered,
                        stringResource(Res.string.numbered_list_label),
                        onAction,
                        EditorAction.LIST_NUMBERED
                    )
                    CompactActionButton(
                        MaterialSymbols.Filled.CheckBox,
                        stringResource(Res.string.task_list),
                        onAction,
                        EditorAction.LIST_TASK
                    )
                    CompactActionButton(
                        MaterialSymbols.Filled.FormatQuote,
                        stringResource(Res.string.quote_label),
                        onAction,
                        EditorAction.QUOTE
                    )
                    CompactActionButton(
                        MaterialSymbols.Filled.Code,
                        stringResource(Res.string.code),
                        onAction,
                        EditorAction.CODE
                    )
                    CompactActionButton(
                        MaterialSymbols.Filled.Image,
                        stringResource(Res.string.image_label),
                        onAction,
                        EditorAction.IMAGE
                    )
                    CompactActionButton(
                        MaterialSymbols.Filled.Remove,
                        stringResource(Res.string.horizontal_rule),
                        onAction,
                        EditorAction.HORIZONTAL_RULE
                    )
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
        shapes = IconButtonDefaults.shapes(),
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
        shapes = IconButtonDefaults.shapes(
            shape = MaterialTheme.shapes.small,
            pressedShape = MaterialTheme.shapes.extraSmall,
        ),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Icon(icon, description, modifier = Modifier.size(20.dp))
    }
}
