package net.gsantner.markor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import net.gsantner.markor.ui.components.ConfigureSystemBars
import net.gsantner.markor.ui.components.LocalSharedTransitionScope
import net.gsantner.markor.ui.navigation.MarkorNavDisplay
import net.gsantner.markor.ui.navigation.Screen
import net.gsantner.markor.ui.theme.MarkorTheme
import org.koin.compose.koinInject
import net.gsantner.markor.data.local.AppSettings

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppContent(
    systemInternalFilesDir: String,
    onExit: () -> Unit,
    introViewModel: net.gsantner.markor.ui.viewmodel.IntroViewModel = org.koin.compose.viewmodel.koinViewModel()
) {
    val isFirstRun by produceState<Boolean?>(initialValue = null, introViewModel) {
        introViewModel.isFirstRun.collect { value = it }
    }
    val appSettings: AppSettings = koinInject()

    MarkorTheme(appTheme = appSettings.getAppTheme) {
        ConfigureSystemBars(
            useDarkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (isFirstRun) {
                null -> {
                    // Keep a stable background while preferences load to avoid intro/main flashing.
                    Box(modifier = Modifier.fillMaxSize())
                }
                true -> {
                net.gsantner.markor.ui.screens.intro.IntroScreen(
                    onIntroFinished = { introViewModel.markIntroSeen() },
                    systemInternalFilesDir = systemInternalFilesDir
                )
                }
                false -> {
                // Simple backstack management for now, matching previous logic
                val backstack = remember { mutableStateListOf<Screen>(Screen.Notebook) }
                
                SharedTransitionLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
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
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            }
        }
    }
}
