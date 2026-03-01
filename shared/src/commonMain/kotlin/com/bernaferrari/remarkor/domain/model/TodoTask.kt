package com.bernaferrari.remarkor.domain.model

data class TodoTask(
    val rawText: String,
    val priority: Char = ' ',
    val completionDate: String? = null,
    val creationDate: String? = null,
    val dueDate: String? = null,
    val contexts: List<String> = emptyList(),
    val projects: List<String> = emptyList(),
    val description: String = "",
    val isCompleted: Boolean = false
) {
    val textForDisplay: String
        get() = if (isCompleted) description.replaceFirst(
            Regex(
                "^\\s*x\\s*",
                RegexOption.IGNORE_CASE
            ), ""
        ) else description

    val hasDueDate: Boolean get() = !dueDate.isNullOrBlank()

    companion object {
        private val TODO_TXT_PATTERN = Regex(
            """^(x\s+)?(\([A-Z]\)\s+)?(\d{4}-\d{2}-\d{2}\s+)?(.*)$""",
            RegexOption.IGNORE_CASE
        )

        private val DATE_PATTERN = Regex("""\d{4}-\d{2}-\d{2}""")

        fun parse(text: String): TodoTask {
            val trimmed = text.trim()
            val match = TODO_TXT_PATTERN.matchEntire(trimmed)

            val isCompleted: Boolean
            val priority: Char
            val dateString: String?
            val description: String

            if (match != null) {
                isCompleted = match.groups[1] != null
                priority = match.groups[2]?.value?.trim()?.get(1) ?: ' '
                dateString = match.groups[3]?.value?.trim()
                description = match.groups[4]?.value?.trim() ?: ""
            } else {
                isCompleted = false
                priority = ' '
                dateString = null
                description = trimmed
            }

            val contexts = Regex("""@\w+""").findAll(description)
                .map { it.value.drop(1) }
                .toList()

            val projects = Regex("""\+\w+""").findAll(description)
                .map { it.value.drop(1) }
                .toList()

            val creationDate: String?
            val completionDate: String?

            if (isCompleted) {
                completionDate = dateString
                creationDate = null // Simplified for now to match the regex provided
            } else {
                creationDate = dateString
                completionDate = null
            }

            val dueDateMatch =
                Regex("""due:\d{4}-\d{2}-\d{2}""", RegexOption.IGNORE_CASE).find(description)
            val dueDate = dueDateMatch?.value?.substringAfter(":")

            return TodoTask(
                rawText = text,
                priority = priority,
                completionDate = completionDate,
                creationDate = creationDate,
                dueDate = dueDate,
                contexts = contexts,
                projects = projects,
                description = description,
                isCompleted = isCompleted
            )
        }
    }
}
