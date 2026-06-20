package com.bernaferrari.remarkor.util

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

private val demoFileSystem: FakeFileSystem = FakeFileSystem().apply {
    val notebook = "/Notebook".toPath()
    createDirectories(notebook)
    write(notebook / "Welcome.md") {
        writeUtf8(
            """
            # Welcome to Markor Web

            This is a browser demo of **Re-Markor** — local-first Markdown notes built with Kotlin Multiplatform and Compose Multiplatform.

            ## Try it

            - Create a new note from the + button
            - Edit Markdown with live preview
            - Pin, archive, and organize notes

            Files in this demo live in an in-memory notebook for the session.
            """.trimIndent(),
        )
    }
}

actual val platformFileSystem: FileSystem = demoFileSystem