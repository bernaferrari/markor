package net.gsantner.markor.ui.components

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
    val name: String,
    val shortcut: String, // What user types after "/"
    val icon: ImageVector,
    val description: String,
    val insertText: String, // Text to insert (replaces /command)
    val cursorOffset: Int = 0 // How many chars from end to place cursor
)

/**
 * Standard slash commands available in the editor.
 */
val slashCommands = listOf(
    // Headers
    SlashCommand("Heading 1", "h1", Icons.Default.Title, "Large heading", "# ", 0),
    SlashCommand("Heading 2", "h2", Icons.Default.Title, "Medium heading", "## ", 0),
    SlashCommand("Heading 3", "h3", Icons.Default.Title, "Small heading", "### ", 0),
    
    // Lists
    SlashCommand("Bullet List", "bullet", Icons.AutoMirrored.Filled.FormatListBulleted, "Unordered list item", "- ", 0),
    SlashCommand("Numbered List", "num", Icons.Default.FormatListNumbered, "Ordered list item", "1. ", 0),
    SlashCommand("Task List", "task", Icons.Default.CheckBox, "Checkbox item", "- [ ] ", 0),
    
    // Blocks
    SlashCommand("Quote", "quote", Icons.Default.FormatQuote, "Block quote", "> ", 0),
    SlashCommand("Code Block", "code", Icons.Default.Code, "Code snippet", "```\n\n```", 4),
    SlashCommand("Inline Code", "inline", Icons.Default.Code, "Inline code", "`code`", 1),
    
    // Structure
    SlashCommand("Divider", "hr", Icons.Default.Remove, "Horizontal line", "\n---\n", 0),
    SlashCommand("Link", "link", Icons.Default.Link, "Add a link", "[text](url)", 4),
    SlashCommand("Image", "img", Icons.Default.Image, "Embed image", "![alt](url)", 4),
    
    // Text formatting
    SlashCommand("Bold", "bold", Icons.Default.FormatBold, "Bold text", "**text**", 2),
    SlashCommand("Italic", "italic", Icons.Default.FormatItalic, "Italic text", "*text*", 1),
    SlashCommand("Strikethrough", "strike", Icons.Default.FormatStrikethrough, "Strikethrough", "~~text~~", 2),
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
                it.shortcut.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true)
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
                            text = "Commands",
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
                                text = "No commands match \"$query\"",
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
                    text = command.name,
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
