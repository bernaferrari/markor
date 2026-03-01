package com.bernaferrari.remarkor.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TodoTaskTest {
    @Test
    fun testParseSimpleTask() {
        val task = TodoTask.parse("Walk the dog")
        assertEquals("Walk the dog", task.description)
        assertFalse(task.isCompleted)
    }

    @Test
    fun testParseCompletedTask() {
        val task = TodoTask.parse("x 2026-01-04 Walk the dog")
        assertTrue(task.isCompleted)
        assertEquals("2026-01-04", task.completionDate)
        assertEquals("Walk the dog", task.description)
    }

    @Test
    fun testParseTaskWithPriority() {
        val task = TodoTask.parse("(A) Important task")
        assertEquals('A', task.priority)
        assertEquals("Important task", task.description)
    }

    @Test
    fun testParseTaskWithContextsAndProjects() {
        val task = TodoTask.parse("Email @boss about +projectX")
        assertTrue(task.contexts.contains("boss"))
        assertTrue(task.projects.contains("projectX"))
    }
}
