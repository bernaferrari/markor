package net.gsantner.markor.domain.model

enum class DocumentFormat(val extension: String, val displayName: String) {
    MARKDOWN(".md", "Markdown"),
    TODOTXT(".txt", "Todo.txt"),
    CSV(".csv", "CSV"),
    WIKITEXT(".wiki", "Wikitext"),
    ORGMODE(".org", "Org-mode"),
    ASCIIDOC(".adoc", "AsciiDoc"),
    PLAINTEXT(".txt", "Plaintext"),
    KEYVALUE(".keyvalue", "Key-Value"),
    EPUP(".epub", "EPUB");

    companion object {
        fun fromExtension(ext: String): DocumentFormat {
            val normalizedExt = if (ext.startsWith(".")) ext.lowercase() else ".${ext.lowercase()}"
            return entries.find { it.extension.equals(normalizedExt, ignoreCase = true) } ?: PLAINTEXT
        }
    }
}
