package com.bernaferrari.remarkor.ui.components

import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Google Keep-style search bar that's always prominent at the top.
 */
@Composable
fun KeepStyleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search notes...",
    leadingIcon: @Composable () -> Unit = { Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.search)) },
    trailingIcon: @Composable () -> Unit = {},
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                leadingIcon()
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Search input
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Using a simplified approach - BasicTextField could be used for actual input
                // For now, this integrates with existing SearchBar API
            }
            
            // Clear button
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(Res.string.clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            trailingIcon()
        }
    }
}

/**
 * Color picker for notes (Google Keep style).
 */
@Composable
fun NoteColorPicker(
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        null to "Default",
        0xFFF28B82.toInt() to "Coral",
        0xFFFCBC03.toInt() to "Yellow",
        0xFFFFF7C5.toInt() to "Cream",
        0xFFCBF0F8.toInt() to "Cyan",
        0xFFAECBFA.toInt() to "Blue",
        0xFFD7AEFB.toInt() to "Purple",
        0xFFFDCFE8.toInt() to "Pink",
        0xFFE6C9A8.toInt() to "Brown",
        0xFFE8EAED.toInt() to "Gray"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        colors.forEach { (color, _) ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color?.let { Color(it) } 
                            ?: MaterialTheme.colorScheme.surfaceVariant
                    )
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                CircleShape
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(Res.string.selected),
                        tint = if (color != null) {
                            val c = Color(color)
                            // Simple contrast check - if color is light, use dark icon
                            val red = (color shr 16) and 0xFF
                            val green = (color shr 8) and 0xFF
                            val blue = color and 0xFF
                            val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
                            if (luminance > 0.5) Color.Black else Color.White
                        } else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Quick actions FAB (Google Keep style).
 */
@Composable
fun QuickCreateFab(
    onNewNote: () -> Unit,
    onNewFolder: () -> Unit,
    onNewList: () -> Unit = onNewNote,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticHelper()
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Expanded options
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
            exit = fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // New List option
                SmallFloatingActionButton(
                    onClick = {
                        haptic.performLightClick()
                        expanded = false
                        onNewList()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, "New List")
                }
                
                // New Folder option
                SmallFloatingActionButton(
                    onClick = {
                        haptic.performLightClick()
                        expanded = false
                        onNewFolder()
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Default.Folder, "New Folder")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Main FAB
        LargeFloatingActionButton(
            onClick = {
                haptic.performHeavyClick()
                if (expanded) {
                    expanded = false
                    onNewNote()
                } else {
                    expanded = true
                }
            },
            modifier = Modifier.size(64.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(Res.string.create_new),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
