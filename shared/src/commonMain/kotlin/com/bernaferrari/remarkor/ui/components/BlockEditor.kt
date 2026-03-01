package com.bernaferrari.remarkor.ui.components

import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.remarkor.domain.model.Block
import com.bernaferrari.remarkor.domain.model.BlockDocument
import com.bernaferrari.remarkor.domain.model.BlockType
import kotlin.math.roundToInt

/**
 * A complete block-based editor with drag-to-reorder functionality.
 * Each block can be independently edited, dragged to reorder, and converted to different types.
 */
@Composable
fun BlockEditor(
    document: BlockDocument,
    onDocumentChange: (BlockDocument) -> Unit,
    isFocusMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current
    
    // Drag state
    var draggedBlockIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(IntOffset.Zero) }
    var dragStarted by remember { mutableStateOf(false) }
    
    // Calculate scroll to keep dragged item visible
    LaunchedEffect(draggedBlockIndex, dragOffset) {
        if (draggedBlockIndex != null) {
            val itemIndex = draggedBlockIndex!!
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val itemInfo = visibleItems.find { it.index == itemIndex }
            
            if (itemInfo != null) {
                // Auto-scroll if dragging near edges
                val viewportCenter = listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize.height / 2
                val itemTop = itemInfo.offset
                val itemBottom = itemTop + itemInfo.size
                
                when {
                    itemTop < listState.layoutInfo.viewportStartOffset + 100 -> {
                        listState.animateScrollToItem(maxOf(0, itemIndex - 1))
                    }
                    itemBottom > listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize.height - 100 -> {
                        listState.animateScrollToItem(minOf(document.blocks.size - 1, itemIndex + 1))
                    }
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(document.blocks, key = { index, block -> block.id }) { index, block ->
            val isFocused = document.focusedBlockId == block.id
            val isDragging = draggedBlockIndex == index
            
            BlockItem(
                block = block,
                index = index,
                isFocused = isFocused,
                isFocusMode = isFocusMode,
                isDragging = isDragging,
                dragOffset = if (isDragging) dragOffset else IntOffset.Zero,
                onFocused = {
                    onDocumentChange(document.copy(focusedBlockId = block.id))
                },
                onContentChange = { newContent ->
                    onDocumentChange(document.updateBlock(block.id) { it.copy(content = newContent) })
                },
                onEnterPressed = {
                    onDocumentChange(document.insertAfter(block.id))
                },
                onBackspaceAtStart = {
                    if (block.content.isEmpty() && document.blocks.size > 1) {
                        onDocumentChange(document.deleteBlock(block.id))
                    }
                },
                onTypeChange = { newType ->
                    onDocumentChange(document.updateBlock(block.id) { it.copy(type = newType) })
                },
                onCheckToggle = {
                    onDocumentChange(document.updateBlock(block.id) { it.copy(checked = !it.checked) })
                },
                onDragStart = {
                    draggedBlockIndex = index
                    dragStarted = true
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                },
                onDrag = { offset ->
                    dragOffset = offset
                },
                onDragEnd = {
                    if (draggedBlockIndex != null) {
                        val fromIndex = draggedBlockIndex!!
                        val itemHeight = 72 // Approximate item height with padding
                        val deltaInItems = (dragOffset.y.toFloat() / itemHeight).roundToInt()
                        val toIndex = (fromIndex + deltaInItems).coerceIn(0, document.blocks.size - 1)
                        
                        if (fromIndex != toIndex) {
                            onDocumentChange(document.moveBlockByIndex(fromIndex, toIndex))
                        }
                    }
                    draggedBlockIndex = null
                    dragOffset = IntOffset.Zero
                    dragStarted = false
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                },
                onDragCancel = {
                    draggedBlockIndex = null
                    dragOffset = IntOffset.Zero
                    dragStarted = false
                },
                onDelete = {
                    onDocumentChange(document.deleteBlock(block.id))
                },
                onMoveUp = {
                    if (index > 0) {
                        onDocumentChange(document.moveBlockByIndex(index, index - 1))
                    }
                },
                onMoveDown = {
                    if (index < document.blocks.size - 1) {
                        onDocumentChange(document.moveBlockByIndex(index, index + 1))
                    }
                }
            )
        }
        
        // Add block button at the end
        item {
            AddBlockButton(
                onClick = {
                    val lastBlockId = document.blocks.lastOrNull()?.id
                    if (lastBlockId != null) {
                        onDocumentChange(document.insertAfter(lastBlockId))
                    } else {
                        onDocumentChange(BlockDocument())
                    }
                }
            )
        }
    }
}

@Composable
private fun BlockItem(
    block: Block,
    index: Int,
    isFocused: Boolean,
    isFocusMode: Boolean,
    isDragging: Boolean,
    dragOffset: IntOffset,
    onFocused: () -> Unit,
    onContentChange: (String) -> Unit,
    onEnterPressed: () -> Unit,
    onBackspaceAtStart: () -> Unit,
    onTypeChange: (BlockType) -> Unit,
    onCheckToggle: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (IntOffset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current
    var showTypeMenu by remember { mutableStateOf(false) }
    var showBlockMenu by remember { mutableStateOf(false) }
    
    // Request focus when this block should be focused
    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }
    
    // Animation for drag state
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else if (isFocused) 4.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isDragging -> MaterialTheme.colorScheme.primary
            isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "border"
    )
    
    // Focus mode dimming
    val alpha by animateFloatAsState(
        targetValue = if (!isFocusMode || isFocused) 1f else 0.4f,
        label = "alpha"
    )
    
    // Drag handle rotation animation
    val handleRotation by animateFloatAsState(
        targetValue = if (isDragging) 90f else 0f,
        label = "handleRotation"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDragging) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = elevation,
        shadowElevation = elevation,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (isDragging) dragOffset.y.toFloat() else 0f
                this.alpha = alpha
            }
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(12.dp)
        ) {
            // Enhanced drag handle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { 
                                hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onDragStart() 
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(IntOffset(dragOffset.x + dragAmount.x.roundToInt(), dragOffset.y + dragAmount.y.roundToInt()))
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragCancel() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Drag to reorder",
                    tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(handleRotation)
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Block type selector
            Box {
                Surface(
                    onClick = { showTypeMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = getBlockTypeIcon(block.type),
                            contentDescription = "Change block type",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    BlockType.entries.filter { it != BlockType.DIVIDER && it != BlockType.IMAGE }.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(getBlockTypeName(type)) },
                            leadingIcon = { Icon(getBlockTypeIcon(type), null, Modifier.size(18.dp)) },
                            onClick = {
                                hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onTypeChange(type)
                                showTypeMenu = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Move Up") },
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, null, Modifier.size(18.dp)) },
                        onClick = {
                            onMoveUp()
                            showTypeMenu = false
                        },
                        enabled = index > 0
                    )
                    DropdownMenuItem(
                        text = { Text("Move Down") },
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, null, Modifier.size(18.dp)) },
                        onClick = {
                            onMoveDown()
                            showTypeMenu = false
                        },
                        enabled = index < Int.MAX_VALUE
                    )
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Task checkbox
            if (block.type == BlockType.TASK_LIST) {
                Checkbox(
                    checked = block.checked,
                    onCheckedChange = {
                        hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onCheckToggle()
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            
            // Content field
            Column(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = block.content,
                    onValueChange = onContentChange,
                    textStyle = getBlockTextStyle(block.type).copy(
                        color = if (block.type == BlockType.TASK_LIST && block.checked)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = block.type != BlockType.CODE_BLOCK && block.type != BlockType.PARAGRAPH,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (it.isFocused) onFocused() }
                        .onKeyEvent { event ->
                            when {
                                event.key == Key.Enter && event.type == KeyEventType.KeyDown -> {
                                    onEnterPressed()
                                    true
                                }
                                event.key == Key.Backspace && event.type == KeyEventType.KeyDown && block.content.isEmpty() -> {
                                    onBackspaceAtStart()
                                    true
                                }
                                event.key == Key.D && event.isCtrlPressed && event.type == KeyEventType.KeyDown -> {
                                    onDelete()
                                    true
                                }
                                else -> false
                            }
                        },
                    decorationBox = { innerTextField ->
                        Box {
                            if (block.content.isEmpty()) {
                                Text(
                                    text = block.getPlaceholder(),
                                    style = getBlockTextStyle(block.type),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            
            // Actions menu
            Box {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isFocused,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = { showBlockMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showBlockMenu,
                    onDismissRequest = { showBlockMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.delete)) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, Modifier.size(18.dp)) },
                        onClick = {
                            onDelete()
                            showBlockMenu = false
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)) },
                        onClick = { /* TODO: Copy to clipboard */ showBlockMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Cut") },
                        leadingIcon = { Icon(Icons.Default.ContentCut, null, Modifier.size(18.dp)) },
                        onClick = { /* TODO: Cut to clipboard */ showBlockMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddBlockButton(onClick: () -> Unit) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.15f else 0f,
        label = "bgAlpha"
    )
    
    Surface(
        onClick = { 
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f + backgroundAlpha),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.tap_to_add_block),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getBlockTypeIcon(type: BlockType): ImageVector = when (type) {
    BlockType.PARAGRAPH -> Icons.AutoMirrored.Filled.Notes
    BlockType.HEADING1 -> Icons.Default.Title
    BlockType.HEADING2 -> Icons.Default.Title
    BlockType.HEADING3 -> Icons.Default.Title
    BlockType.HEADING4 -> Icons.Default.Title
    BlockType.HEADING5 -> Icons.Default.Title
    BlockType.HEADING6 -> Icons.Default.Title
    BlockType.BULLET_LIST -> Icons.AutoMirrored.Filled.FormatListBulleted
    BlockType.NUMBERED_LIST -> Icons.Default.FormatListNumbered
    BlockType.TASK_LIST -> Icons.Default.CheckBox
    BlockType.QUOTE -> Icons.Default.FormatQuote
    BlockType.CODE_BLOCK -> Icons.Default.Code
    BlockType.DIVIDER -> Icons.Default.Remove
    BlockType.IMAGE -> Icons.Default.Image
}

private fun getBlockTypeName(type: BlockType): String = when (type) {
    BlockType.PARAGRAPH -> "Paragraph"
    BlockType.HEADING1 -> "Heading 1"
    BlockType.HEADING2 -> "Heading 2"
    BlockType.HEADING3 -> "Heading 3"
    BlockType.HEADING4 -> "Heading 4"
    BlockType.HEADING5 -> "Heading 5"
    BlockType.HEADING6 -> "Heading 6"
    BlockType.BULLET_LIST -> "Bullet List"
    BlockType.NUMBERED_LIST -> "Numbered List"
    BlockType.TASK_LIST -> "Task"
    BlockType.QUOTE -> "Quote"
    BlockType.CODE_BLOCK -> "Code Block"
    BlockType.DIVIDER -> "Divider"
    BlockType.IMAGE -> "Image"
}

@Composable
private fun getBlockTextStyle(type: BlockType): TextStyle = when (type) {
    BlockType.HEADING1 -> MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
    BlockType.HEADING2 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
    BlockType.HEADING3 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
    BlockType.HEADING4 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
    BlockType.HEADING5 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    BlockType.HEADING6 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
    BlockType.CODE_BLOCK -> MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    BlockType.QUOTE -> MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
    else -> MaterialTheme.typography.bodyLarge
}
