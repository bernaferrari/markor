package com.bernaferrari.remarkor

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import com.bernaferrari.remarkor.ui.components.ConfigureSystemBars
import com.bernaferrari.remarkor.ui.components.UserMessageHandler
import com.bernaferrari.remarkor.ui.components.UserMessageManager
import com.bernaferrari.remarkor.ui.navigation.MarkorNavigator
import com.bernaferrari.remarkor.ui.components.LocalSharedTransitionScope
import com.bernaferrari.remarkor.ui.navigation.MarkorNavDisplay
import com.bernaferrari.remarkor.ui.navigation.Screen
import com.bernaferrari.remarkor.ui.theme.MarkorTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppContent(
    systemInternalFilesDir: String,
    onExit: () -> Unit,
    initialFilePath: String? = null,
    introViewModel: com.bernaferrari.remarkor.ui.viewmodel.IntroViewModel = org.koin.compose.viewmodel.koinViewModel()
) {
    val isFirstRun by produceState<Boolean?>(initialValue = null, introViewModel) {
        introViewModel.isFirstRun.collect { value = it }
    }
    val settingsRepository: ISettingsRepository = koinInject()
    val userMessageManager: UserMessageManager = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }

    MarkorTheme(appTheme = settingsRepository.getAppTheme) {
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
                    com.bernaferrari.remarkor.ui.screens.IntroScreen(
                        onIntroFinished = { introViewModel.markIntroSeen() },
                        systemInternalFilesDir = systemInternalFilesDir
                    )
                }

                false -> {
                    val backstack = remember(initialFilePath) {
                        mutableStateListOf<Screen>(
                            initialFilePath?.let { Screen.Editor(it) } ?: Screen.Notebook
                        )
                    }
                    val navigator = remember(backstack) {
                        MarkorNavigator(backstack, onExitWhenEmpty = onExit)
                    }

                    UserMessageHandler(
                        messageManager = userMessageManager,
                        snackbarHostState = snackbarHostState,
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        SharedTransitionLayout(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CompositionLocalProvider(
                                LocalSharedTransitionScope provides this
                            ) {
                                MarkorNavDisplay(
                                    backstack = backstack,
                                    onNavigate = navigator::navigate,
                                    onPopBackStack = navigator::pop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
