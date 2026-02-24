package net.gsantner.markor.ui.components

import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * Represents a slash command that can format text.
 */
data class SlashCommand(
    val name: StringResource,
    val shortcut: String, // What user types after "/"
    val icon: ImageVector,
    val description: StringResource,
    val insertText: String, // Text to insert (replaces /command)
    val cursorOffset: Int = 0 // How many chars from end to place cursor
)

/**
 * Standard slash commands available in the editor.
 */
val slashCommands = listOf(
    // Headers
    SlashCommand(Res.string.heading_1_label, "h1", Icons.Default.Title, Res.string.heading_1_description, "# ", 0),
    SlashCommand(Res.string.heading_2_label, "h2", Icons.Default.Title, Res.string.heading_2_description, "## ", 0),
    SlashCommand(Res.string.heading_3_label, "h3", Icons.Default.Title, Res.string.heading_3_description, "### ", 0),
    
    // Lists
    SlashCommand(Res.string.bullet_list, "bullet", Icons.AutoMirrored.Filled.FormatListBulleted, Res.string.bullet_list_description_label, "- ", 0),
    SlashCommand(Res.string.numbered_list, "num", Icons.Default.FormatListNumbered, Res.string.numbered_list_description_label, "1. ", 0),
    SlashCommand(Res.string.task_list_label, "task", Icons.Default.CheckBox, Res.string.task_list_description, "- [ ] ", 0),
    
    // Blocks
    SlashCommand(Res.string.quote_label, "quote", Icons.Default.FormatQuote, Res.string.quote_description, "> ", 0),
    SlashCommand(Res.string.code_block_label, "code", Icons.Default.Code, Res.string.code_block_description, "```\n\n```", 4),
    SlashCommand(Res.string.inline_code_label, "inline", Icons.Default.Code, Res.string.inline_code_description, "`code`", 1),
    
    // Structure
    SlashCommand(Res.string.divider_label, "hr", Icons.Default.Remove, Res.string.divider_description, "\n---\n", 0),
    SlashCommand(Res.string.link, "link", Icons.Default.Link, Res.string.link_description, "[text](url)", 4),
    SlashCommand(Res.string.image_label, "img", Icons.Default.Image, Res.string.image_description, "![alt](url)", 4),
    
    // Text formatting
    SlashCommand(Res.string.bold_label, "bold", Icons.Default.FormatBold, Res.string.bold_description, "**text**", 2),
    SlashCommand(Res.string.italic_label, "italic", Icons.Default.FormatItalic, Res.string.italic_description, "*text*", 1),
    SlashCommand(Res.string.strikethrough_label, "strike", Icons.Default.FormatStrikethrough, Res.string.strikethrough_description, "~~text~~", 2),
)

/**
 * A floating popup menu showing available slash commands.
 * Appears when user types "/" at the beginning of a line.
 */
@Composable
fun SlashCommandMenu(
    visible: Boolean,
    query: String, // Text typed after "/"
    onSelect: (SlashCommand) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticHelper = rememberHapticHelper()
    
    // Filter commands based on query
    val filteredCommands = remember(query) {
        if (query.isEmpty()) {
            slashCommands
        } else {
            slashCommands.filter { 
                it.shortcut.contains(query, ignoreCase = true)
            }
        }
    }
    
    AnimatedVisibility(
        visible = visible && filteredCommands.isNotEmpty(),
        enter = fadeIn(tween(150)) + slideInVertically { -it / 4 },
        exit = fadeOut(tween(100)) + slideOutVertically { -it / 4 }
    ) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = false)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = modifier
                    .widthIn(min = 200.dp, max = 300.dp)
                    .heightIn(max = 300.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Section header
                    item {
                        Text(
                            text = stringResource(Res.string.commands_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(filteredCommands) { command ->
                        SlashCommandItem(
                            command = command,
                            query = query,
                            onClick = {
                                hapticHelper.performLightClick()
                                onSelect(command)
                            }
                        )
                    }
                    
                    // Empty state
                    if (filteredCommands.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(Res.string.no_commands_match, query),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlashCommandItem(
    command: SlashCommand,
    query: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Icon container
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = command.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(command.name),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/${command.shortcut}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Process slash command input and return updated TextFieldValue.
 * Call this when user selects a command from the menu.
 */
fun applySlashCommand(
    value: TextFieldValue,
    command: SlashCommand,
    slashStartIndex: Int // Index where "/" was typed
): TextFieldValue {
    val text = value.text
    val currentPosition = value.selection.start
    
    // Find the end of the slash command (current cursor position)
    val beforeSlash = text.substring(0, slashStartIndex)
    val afterCursor = text.substring(currentPosition)
    
    // Build new text
    val newText = beforeSlash + command.insertText + afterCursor
    
    // Calculate new cursor position
    val insertEnd = slashStartIndex + command.insertText.length
    val newCursor = insertEnd - command.cursorOffset
    
    return value.copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}

/**
 * Detects if user is typing a slash command.
 * Returns the index of "/" if active, or -1 if not in slash mode.
 */
fun detectSlashCommand(value: TextFieldValue): Pair<Int, String>? {
    val text = value.text
    val cursorPos = value.selection.start
    
    if (cursorPos == 0 || text.isEmpty()) return null
    
    // Look backwards from cursor for "/"
    var slashIndex = -1
    for (i in (cursorPos - 1) downTo 0) {
        val char = text[i]
        if (char == '/') {
            // Check if it's at line start or after whitespace
            val isLineStart = i == 0 || text[i - 1] == '\n'
            if (isLineStart) {
                slashIndex = i
                break
            } else {
                return null // Slash not at line start
            }
        } else if (char == '\n' || char == ' ') {
            return null // Hit newline or space before finding /
        }
    }
    
    if (slashIndex == -1) return null
    
    // Extract query (text after /)
    val query = text.substring(slashIndex + 1, cursorPos)
    
    // Only match if query is reasonable length and alphanumeric
    if (query.length > 20 || !query.all { it.isLetterOrDigit() }) {
        return null
    }
    
    return slashIndex to query
}
