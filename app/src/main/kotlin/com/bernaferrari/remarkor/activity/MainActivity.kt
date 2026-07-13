package com.bernaferrari.remarkor.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.bernaferrari.remarkor.AppContent
import com.bernaferrari.remarkor.domain.repository.IDocumentRepository
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class MainActivity : ComponentActivity() {
    private val settingsRepository: ISettingsRepository by inject()
    private val documentRepository: IDocumentRepository by inject()
    private val defaultNotebookPath: String by inject(named("default_notebook_path"))

    private var incomingFilePath by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppContent(
                systemInternalFilesDir = filesDir.absolutePath,
                onExit = { finish() },
                initialFilePath = incomingFilePath,
            )
        }

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        lifecycleScope.launch {
            runCatching {
                IncomingDocumentImporter(
                    contentResolver = contentResolver,
                    settingsRepository = settingsRepository,
                    documentRepository = documentRepository,
                    defaultNotebookPath = defaultNotebookPath,
                ).import(intent)
            }.getOrNull()?.let { incomingFilePath = it }
        }
    }
}
