package com.bernaferrari.remarkor.util

import okio.FileSystem
import okio.Path
import okio.SYSTEM

/**
 * Extracts the first image URL from markdown content.
 * Supports:
 * - Standard markdown: `![alt](url)`
 * - Wiki-style: `![[image.png]]`
 * - HTML: `<img src="url">`
 */
fun extractFirstImageUrl(content: String): String? {
    // Standard markdown image: ![alt](url)
    val mdImageRegex = """!\[[^]]*]\((.*?)\)""".toRegex()
    mdImageRegex.find(content)?.let { match ->
        return match.groupValues[1]
    }

    // Wiki-style image: ![[image.png]]
    val wikiImageRegex = """!\[\[(.*?)]\]""".toRegex()
    wikiImageRegex.find(content)?.let { match ->
        return match.groupValues[1]
    }

    // HTML img tag: <img src="url">
    val htmlImageRegex = """<img[^>]+src=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
    htmlImageRegex.find(content)?.let { match ->
        return match.groupValues[1]
    }

    return null
}

/**
 * Extracts the first image URL from a markdown file.
 * Returns the image URL or path relative to the file.
 */
fun extractFirstImageFromFile(
    filePath: Path,
    fileSystem: FileSystem = FileSystem.SYSTEM
): String? {
    return try {
        val content = fileSystem.read(filePath) { readUtf8() }
        extractFirstImageUrl(content)
    } catch (_: Exception) {
        null
    }
}

/**
 * Resolves an image URL relative to the markdown file's directory.
 * Handles relative paths like ./images/photo.jpg or images/photo.jpg
 */
fun resolveImageUrl(
    imageUrl: String,
    markdownFilePath: Path
): String? {
    // Already absolute path or URL
    if (imageUrl.startsWith("http://") ||
        imageUrl.startsWith("https://") ||
        imageUrl.startsWith("/")
    ) {
        return imageUrl
    }

    // Resolve relative to markdown file's directory
    val parentDir = markdownFilePath.parent ?: return null

    return try {
        val resolved = parentDir.resolve(imageUrl)
        resolved.toString()
    } catch (_: Exception) {
        null
    }
}

/**
 * Extracts a preview from markdown content for display in cards.
 * Removes markdown syntax and returns clean text.
 */
fun extractPreviewText(content: String, maxLength: Int = 150): String {
    var text = content

    // Remove images
    text = text.replace("""!\[[^]]*]\(.*?\)""".toRegex(), "")
    text = text.replace("""!\[\[.*?]]""".toRegex(), "")

    // Remove links but keep text
    text = text.replace("""\[(.*?)\]\(.*?\)""".toRegex(), "$1")

    // Remove headers
    text = text.replace("""^#{1,6}\s+""".toRegex(RegexOption.MULTILINE), "")

    // Remove bold/italic
    text = text.replace("""[*_]{1,3}(.*?)[*_]{1,3}""".toRegex(), "$1")

    // Remove code blocks
    text = text.replace("""```[\s\S]*?```""".toRegex(), "")
    text = text.replace("""`(.*?)`""".toRegex(), "$1")

    // Remove blockquotes
    text = text.replace("""^>\s+""".toRegex(RegexOption.MULTILINE), "")

    // Remove horizontal rules
    text = text.replace("""^[-*_]{3,}$""".toRegex(RegexOption.MULTILINE), "")

    // Remove list markers
    text = text.replace("""^[-*+]\s+""".toRegex(RegexOption.MULTILINE), "")
    text = text.replace("""^\d+\.\s+""".toRegex(RegexOption.MULTILINE), "")
    text = text.replace("""^[-*+]\s+\[[ xX]]\s+""".toRegex(RegexOption.MULTILINE), "")

    // Remove HTML tags
    text = text.replace("""<[^>]+>""".toRegex(), "")

    // Clean up whitespace
    text = text.trim()
    text = text.replace("""\n{3,}""".toRegex(), "\n\n")

    // Truncate
    if (text.length > maxLength) {
        text = text.substring(0, maxLength).trim() + "..."
    }

    return text
}
