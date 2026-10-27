package com.bernaferrari.remarkor.storecapture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bernaferrari.remarkor.AppContent
import com.bernaferrari.remarkor.domain.repository.IFavoritesRepository
import com.bernaferrari.remarkor.domain.repository.INoteMetadataRepository
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import com.bernaferrari.remarkor.domain.usecase.InitializeNotebookUseCase
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.io.File

/**
 * Store-capture entry point (storeCapture build type only, never in production builds).
 *
 * Seeds a private notebook with fictional notes the first time it runs, then shows the real app.
 *
 * Extras:
 *  - `seed` (boolean, default true): false leaves a fresh install on the real intro screens.
 *  - `theme` (light | dark) and `palette` (an app palette token such as `indigo`).
 *  - `open`: a note path relative to the notebook, opened straight in the editor.
 */
class StoreCaptureActivity : ComponentActivity() {
    private val settings: ISettingsRepository by inject()
    private val metadata: INoteMetadataRepository by inject()
    private val favorites: IFavoritesRepository by inject()
    private val initializeNotebook: InitializeNotebookUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notebook = File(filesDir, "Notebook")
        runBlocking {
            if (intent.getBooleanExtra("seed", true) && !File(filesDir, MARKER).exists()) seed(notebook)
            val theme = intent.getStringExtra("theme") ?: "light"
            val palette = intent.getStringExtra("palette") ?: "amber"
            settings.setAppTheme("$palette $theme")
        }
        val open = intent.getStringExtra("open")?.let { File(notebook, it).absolutePath }

        setContent {
            AppContent(
                systemInternalFilesDir = filesDir.absolutePath,
                onExit = { finish() },
                initialFilePath = open,
            )
        }
    }

    private suspend fun seed(notebook: File) {
        initializeNotebook(isExternal = false, internalFilesDir = filesDir.absolutePath)
        settings.setFirstRun(false)

        // Newest first: each note is a minute older than the one before it.
        val now = System.currentTimeMillis()
        StoreFixtures.notes.forEachIndexed { index, note ->
            val file = File(notebook, note.path)
            file.parentFile?.mkdirs()
            file.writeText(note.content)
            file.setLastModified(now - index * 60_000L * 37)
            val path = file.absolutePath
            metadata.upsertFromContent(path, note.content, now)
            note.color?.let { metadata.setColor(path, it) }
            if (note.labels.isNotEmpty()) metadata.setLabelsForPath(path, note.labels)
            if (note.pinned) metadata.togglePinned(path, now)
            if (note.favorite) favorites.addFavorite(path) else favorites.removeFavorite(path)
        }
        File(filesDir, MARKER).writeText("seeded")
    }

    private companion object {
        const val MARKER = "store-capture-seeded"
    }
}
