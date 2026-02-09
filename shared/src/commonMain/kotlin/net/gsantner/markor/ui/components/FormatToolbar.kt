package net.gsantner.markor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import net.gsantner.markor.ui.theme.MarkorTheme
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class EditorAction {
    BOLD, ITALIC, STRIKETHROUGH, CODE,
    LINK, HEADER,
    LIST_BULLET, LIST_NUMBERED, LIST_TASK,
    QUOTE, HORIZONTAL_RULE,
    UNDO, REDO, SEARCH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatToolbar(
    onAction: (EditorAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = MarkorTheme.spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = MarkorTheme.elevation.level2,
            shadowElevation = MarkorTheme.elevation.level2,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(vertical = MarkorTheme.spacing.small, horizontal = MarkorTheme.spacing.extraSmall),
                verticalArrangement = Arrangement.spacedBy(MarkorTheme.spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: Text Styling & Structure
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = MarkorTheme.spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(MarkorTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarIconButton(Icons.Default.FormatBold, "Bold", onAction, EditorAction.BOLD)
                    ToolbarIconButton(Icons.Default.FormatItalic, "Italic", onAction, EditorAction.ITALIC)
                    ToolbarIconButton(Icons.Default.FormatStrikethrough, "Strike", onAction, EditorAction.STRIKETHROUGH)
                    ToolbarIconButton(Icons.Default.Code, "Code", onAction, EditorAction.CODE)
                    
                    VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    ToolbarIconButton(Icons.Default.Title, "Header", onAction, EditorAction.HEADER)
                    ToolbarIconButton(Icons.Default.FormatQuote, "Quote", onAction, EditorAction.QUOTE)
                    ToolbarIconButton(Icons.Default.Link, "Link", onAction, EditorAction.LINK)
                }
                
                // Row 2: Lists & Structure
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = MarkorTheme.spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(MarkorTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarIconButton(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet", onAction, EditorAction.LIST_BULLET)
                    ToolbarIconButton(Icons.Default.FormatListNumbered, "Numbered", onAction, EditorAction.LIST_NUMBERED)
                    ToolbarIconButton(Icons.Default.CheckBox, "Task", onAction, EditorAction.LIST_TASK)
                    ToolbarIconButton(Icons.Default.Remove, "Horizontal Rule", onAction, EditorAction.HORIZONTAL_RULE)
                }
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
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
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.size(38.dp) // Slightly smaller to fit better
    ) {
        Icon(icon, description, modifier = Modifier.size(20.dp))
    }
}

