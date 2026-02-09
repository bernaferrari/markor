package net.gsantner.markor.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Represents a heading/section in the document outline.
 */
data class OutlineItem(
    val level: Int, // 1-6 for h1-h6
    val text: String,
    val lineIndex: Int, // Line number in the document
    val charOffset: Int // Character offset from start
)

/**
 * Parses markdown text to extract heading structure.
 */
fun parseOutline(text: String): List<OutlineItem> {
    val items = mutableListOf<OutlineItem>()
    val lines = text.split("\n")
    var charOffset = 0
    
    lines.forEachIndexed { lineIndex, line ->
        val headerMatch = Regex("^(#{1,6})\\s+(.+)$").find(line.trim())
        if (headerMatch != null) {
            val level = headerMatch.groupValues[1].length
            val headerText = headerMatch.groupValues[2].trim()
            items.add(OutlineItem(level, headerText, lineIndex, charOffset))
        }
        charOffset += line.length + 1 // +1 for newline
    }
    
    return items
}

/**
 * A side panel showing the document outline (table of contents).
 * Allows quick navigation to sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinePanel(
    items: List<OutlineItem>,
    currentCharOffset: Int, // Current cursor position to highlight active section
    onItemClick: (OutlineItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Find current section based on cursor position
    val currentSection = remember(currentCharOffset, items) {
        items.lastOrNull { it.charOffset <= currentCharOffset }
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Toc,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Document Outline",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${items.size} sections",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            if (items.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No headings found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Add headings with # to create outline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        val isActive = item == currentSection
                        val hapticHelper = rememberHapticHelper()
                        
                        // Staggered animation
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 30L)
                            visible = true
                        }
                        
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInHorizontally { -it / 3 }
                        ) {
                            OutlineItemRow(
                                item = item,
                                isActive = isActive,
                                onClick = {
                                    hapticHelper.performLightClick()
                                    scope.launch { sheetState.hide() }.invokeOnCompletion { 
                                        if (!sheetState.isVisible) {
                                            onDismiss()
                                        }
                                    }
                                    onItemClick(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlineItemRow(
    item: OutlineItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
        else 
            Color.Transparent,
        label = "outlineBg"
    )
    
    val contentColor = if (isActive) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.onSurface
    
    // Indentation based on heading level
    val indentation = (item.level - 1) * 16
    
    // Font size based on level
    val fontSize = when (item.level) {
        1 -> 16.sp
        2 -> 15.sp
        3 -> 14.sp
        else -> 13.sp
    }
    
    val fontWeight = when (item.level) {
        1, 2 -> FontWeight.SemiBold
        else -> FontWeight.Normal
    }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = (8 + indentation).dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
        ) {
            // Level indicator dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
            )
            
            Spacer(Modifier.width(12.dp))
            
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    letterSpacing = 0.sp
                ),
                color = contentColor,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
