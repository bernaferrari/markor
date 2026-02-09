package net.gsantner.markor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import net.gsantner.markor.ui.components.LocalSharedTransitionScope
import net.gsantner.markor.ui.navigation.MarkorNavDisplay
import net.gsantner.markor.ui.navigation.Screen
import net.gsantner.markor.ui.theme.MarkorTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppContent(
    systemInternalFilesDir: String,
    onExit: () -> Unit,
    introViewModel: net.gsantner.markor.ui.viewmodel.IntroViewModel = org.koin.compose.viewmodel.koinViewModel()
) {
    val isFirstRun by introViewModel.isFirstRun.collectAsState(initial = true)

    MarkorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isFirstRun) {
                net.gsantner.markor.ui.screens.intro.IntroScreen(
                    onIntroFinished = { introViewModel.markIntroSeen() },
                    systemInternalFilesDir = systemInternalFilesDir
                )
            } else {
                // Simple backstack management for now, matching previous logic
                val backstack = remember { mutableStateListOf<Screen>(Screen.Notebook) }
                
                SharedTransitionLayout {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this
                    ) {
                        MarkorNavDisplay(
                            backstack = backstack,
                            onNavigate = { screen ->
                                backstack.add(screen)
                            },
                            onPopBackStack = {
                                if (backstack.size > 1) {
                                    backstack.removeAt(backstack.size - 1)
                                } else {
                                    onExit()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
