package com.bernaferrari.remarkor.domain.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents the type of a content block in the editor.
 */
enum class BlockType {
    PARAGRAPH,
    HEADING1,
    HEADING2,
    HEADING3,
    HEADING4,
    HEADING5,
    HEADING6,
    BULLET_LIST,
    NUMBERED_LIST,
    TASK_LIST,
    QUOTE,
    CODE_BLOCK,
    DIVIDER,
    IMAGE
}

/**
 * Represents a single content block in the block-based editor.
 * Each block can be independently edited, styled, and reordered.
 */
@OptIn(ExperimentalUuidApi::class)
data class Block(
    val id: String = Uuid.random().toString(),
    val type: BlockType = BlockType.PARAGRAPH,
    val content: String = "",
    val indent: Int = 0, // For nested lists
    val checked: Boolean = false, // For task lists
    val language: String = "" // For code blocks
) {
    /**
     * Convert this block to its markdown representation.
     */
    fun toMarkdown(): String {
        val indentStr = "  ".repeat(indent)
        return when (type) {
            BlockType.PARAGRAPH -> content
            BlockType.HEADING1 -> "# $content"
            BlockType.HEADING2 -> "## $content"
            BlockType.HEADING3 -> "### $content"
            BlockType.HEADING4 -> "#### $content"
            BlockType.HEADING5 -> "##### $content"
            BlockType.HEADING6 -> "###### $content"
            BlockType.BULLET_LIST -> "$indentStr- $content"
            BlockType.NUMBERED_LIST -> "${indentStr}1. $content"
            BlockType.TASK_LIST -> if (checked) "$indentStr- [x] $content" else "$indentStr- [ ] $content"
            BlockType.QUOTE -> "> $content"
            BlockType.CODE_BLOCK -> if (language.isNotEmpty()) "```$language\n$content\n```" else "```\n$content\n```"
            BlockType.DIVIDER -> "---"
            BlockType.IMAGE -> "![$content]()"
        }
    }
    
    /**
     * Get a placeholder hint for this block type.
     */
    fun getPlaceholder(): String = when (type) {
        BlockType.PARAGRAPH -> "Type something..."
        BlockType.HEADING1 -> "Heading 1"
        BlockType.HEADING2 -> "Heading 2"
        BlockType.HEADING3 -> "Heading 3"
        BlockType.HEADING4 -> "Heading 4"
        BlockType.HEADING5 -> "Heading 5"
        BlockType.HEADING6 -> "Heading 6"
        BlockType.BULLET_LIST -> "List item"
        BlockType.NUMBERED_LIST -> "Numbered item"
        BlockType.TASK_LIST -> "Task"
        BlockType.QUOTE -> "Quote..."
        BlockType.CODE_BLOCK -> "// code"
        BlockType.DIVIDER -> ""
        BlockType.IMAGE -> "Image description"
    }
}

/**
 * Represents the state of a block-based document.
 */
data class BlockDocument(
    val blocks: List<Block> = listOf(Block()),
    val focusedBlockId: String? = null
) {
    /**
     * Convert all blocks to a single markdown string.
     */
    fun toMarkdown(): String {
        return blocks.joinToString("\n\n") { it.toMarkdown() }
    }
    
    /**
     * Get block by ID.
     */
    fun getBlock(id: String): Block? = blocks.find { it.id == id }
    
    /**
     * Get index of block by ID.
     */
    fun getBlockIndex(id: String): Int = blocks.indexOfFirst { it.id == id }
    
    /**
     * Update a specific block.
     */
    fun updateBlock(id: String, transform: (Block) -> Block): BlockDocument {
        return copy(blocks = blocks.map { if (it.id == id) transform(it) else it })
    }
    
    /**
     * Insert a new block after the specified block.
     */
    fun insertAfter(afterId: String, newBlock: Block = Block()): BlockDocument {
        val index = blocks.indexOfFirst { it.id == afterId }
        if (index == -1) return copy(blocks = blocks + newBlock)
        val newBlocks = blocks.toMutableList()
        newBlocks.add(index + 1, newBlock)
        return copy(blocks = newBlocks, focusedBlockId = newBlock.id)
    }
    
    /**
     * Delete a block by ID.
     */
    fun deleteBlock(id: String): BlockDocument {
        val index = blocks.indexOfFirst { it.id == id }
        if (index == -1 || blocks.size <= 1) return this // Don't delete last block
        val newBlocks = blocks.toMutableList()
        newBlocks.removeAt(index)
        // Focus previous block or first block
        val newFocusId = if (index > 0) blocks[index - 1].id else newBlocks.firstOrNull()?.id
        return copy(blocks = newBlocks, focusedBlockId = newFocusId)
    }
    
    /**
     * Move a block to a new position.
     */
    fun moveBlock(fromId: String, toIndex: Int): BlockDocument {
        val fromIndex = blocks.indexOfFirst { it.id == fromId }
        if (fromIndex == -1 || toIndex < 0 || toIndex >= blocks.size) return this
        val newBlocks = blocks.toMutableList()
        val block = newBlocks.removeAt(fromIndex)
        newBlocks.add(toIndex.coerceIn(0, newBlocks.size), block)
        return copy(blocks = newBlocks)
    }
    
    /**
     * Move a block from one index to another (by index).
     */
    fun moveBlockByIndex(fromIndex: Int, toIndex: Int): BlockDocument {
        if (fromIndex < 0 || fromIndex >= blocks.size || toIndex < 0 || toIndex >= blocks.size) return this
        val newBlocks = blocks.toMutableList()
        val block = newBlocks.removeAt(fromIndex)
        newBlocks.add(toIndex.coerceIn(0, newBlocks.size), block)
        return copy(blocks = newBlocks)
    }
}
