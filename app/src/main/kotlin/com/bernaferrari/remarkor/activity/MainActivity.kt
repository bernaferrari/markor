package com.bernaferrari.remarkor.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bernaferrari.remarkor.AppContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppContent(
                systemInternalFilesDir = filesDir.absolutePath,
                onExit = { finish() }
            )
        }
    }
}
