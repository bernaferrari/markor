package net.gsantner.markor.domain.parser

import net.gsantner.markor.domain.model.Block
import net.gsantner.markor.domain.model.BlockDocument
import net.gsantner.markor.domain.model.BlockType

/**
 * Parses markdown text into a BlockDocument for block-based editing.
 */
object BlockParser {
    
    /**
     * Parse markdown text into a BlockDocument.
     */
    fun parse(markdown: String): BlockDocument {
        if (markdown.isBlank()) {
            return BlockDocument(blocks = listOf(Block()))
        }
        
        val blocks = mutableListOf<Block>()
        val lines = markdown.split("\n")
        var i = 0
        
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()
            
            when {
                // Code block (fenced)
                trimmed.startsWith("```") -> {
                    val language = trimmed.removePrefix("```").trim()
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    blocks.add(Block(
                        type = BlockType.CODE_BLOCK,
                        content = codeLines.joinToString("\n"),
                        language = language
                    ))
                }
                
                // Divider
                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    blocks.add(Block(type = BlockType.DIVIDER))
                }
                
                // Headings
                trimmed.startsWith("######") -> {
                    blocks.add(Block(
                        type = BlockType.HEADING6,
                        content = trimmed.removePrefix("######").trim()
                    ))
                }
                trimmed.startsWith("#####") -> {
                    blocks.add(Block(
                        type = BlockType.HEADING5,
                        content = trimmed.removePrefix("#####").trim()
                    ))
                }
                trimmed.startsWith("####") -> {
                    blocks.add(Block(
                        type = BlockType.HEADING4,
                        content = trimmed.removePrefix("####").trim()
                    ))
                }
                trimmed.startsWith("###") -> {
                    blocks.add(Block(
                        type = BlockType.HEADING3,
                        content = trimmed.removePrefix("###").trim()
                    ))
                }
                trimmed.startsWith("##") -> {
                    blocks.add(Block(
                        type = BlockType.HEADING2,
                        content = trimmed.removePrefix("##").trim()
                    ))
                }
                trimmed.startsWith("#") -> {
                    blocks.add(Block(
                        type = BlockType.HEADING1,
                        content = trimmed.removePrefix("#").trim()
                    ))
                }
                
                // Task list
                Regex("^(\\s*)- \\[([ xX])\\] (.*)$").find(trimmed)?.let { match ->
                    val indent = (line.length - line.trimStart().length) / 2
                    val checked = match.groupValues[2].lowercase() == "x"
                    val content = match.groupValues[3]
                    blocks.add(Block(
                        type = BlockType.TASK_LIST,
                        content = content,
                        indent = indent,
                        checked = checked
                    ))
                    null // Return null to continue
                } != null -> { /* Handled above */ }
                
                // Bullet list
                Regex("^(\\s*)[-*+] (.*)$").find(trimmed)?.let { match ->
                    val indent = (line.length - line.trimStart().length) / 2
                    val content = match.groupValues[2]
                    blocks.add(Block(
                        type = BlockType.BULLET_LIST,
                        content = content,
                        indent = indent
                    ))
                    null
                } != null -> { /* Handled above */ }
                
                // Numbered list
                Regex("^(\\s*)\\d+\\. (.*)$").find(trimmed)?.let { match ->
                    val indent = (line.length - line.trimStart().length) / 2
                    val content = match.groupValues[2]
                    blocks.add(Block(
                        type = BlockType.NUMBERED_LIST,
                        content = content,
                        indent = indent
                    ))
                    null
                } != null -> { /* Handled above */ }
                
                // Quote
                trimmed.startsWith(">") -> {
                    blocks.add(Block(
                        type = BlockType.QUOTE,
                        content = trimmed.removePrefix(">").trim()
                    ))
                }
                
                // Image
                Regex("^!\\[(.*)\\]\\((.*)\\)$").find(trimmed)?.let { match ->
                    blocks.add(Block(
                        type = BlockType.IMAGE,
                        content = match.groupValues[1] // Alt text
                    ))
                    null
                } != null -> { /* Handled above */ }
                
                // Empty line - skip or merge with previous
                trimmed.isEmpty() -> {
                    // Skip empty lines between blocks
                }
                
                // Paragraph (default)
                else -> {
                    // Collect consecutive lines as one paragraph
                    val paragraphLines = mutableListOf(line)
                    while (i + 1 < lines.size) {
                        val nextLine = lines[i + 1]
                        val nextTrimmed = nextLine.trim()
                        // Stop if next line starts a new block type
                        if (nextTrimmed.isEmpty() ||
                            nextTrimmed.startsWith("#") ||
                            nextTrimmed.startsWith("-") ||
                            nextTrimmed.startsWith("*") ||
                            nextTrimmed.startsWith(">") ||
                            nextTrimmed.startsWith("```") ||
                            nextTrimmed.matches(Regex("^\\d+\\. .*"))) {
                            break
                        }
                        paragraphLines.add(nextLine)
                        i++
                    }
                    blocks.add(Block(
                        type = BlockType.PARAGRAPH,
                        content = paragraphLines.joinToString("\n")
                    ))
                }
            }
            i++
        }
        
        // Ensure at least one block
        if (blocks.isEmpty()) {
            blocks.add(Block())
        }
        
        return BlockDocument(blocks = blocks, focusedBlockId = blocks.firstOrNull()?.id)
    }
    
    /**
     * Convert BlockDocument back to markdown string.
     */
    fun toMarkdown(document: BlockDocument): String {
        return document.toMarkdown()
    }
}
