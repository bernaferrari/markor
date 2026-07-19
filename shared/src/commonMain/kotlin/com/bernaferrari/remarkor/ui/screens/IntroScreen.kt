package com.bernaferrari.remarkor.ui.screens

import com.bernaferrari.remarkor.ui.icons.MaterialSymbols

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.ui.components.supportsSharedStorageMode
import com.bernaferrari.remarkor.ui.viewmodel.IntroViewModel
import kotlinx.coroutines.launch
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.back
import markor.shared.generated.resources.choose_storage_location
import markor.shared.generated.resources.get_started
import markor.shared.generated.resources.intro_description
import markor.shared.generated.resources.intro_local_first
import markor.shared.generated.resources.private_storage
import markor.shared.generated.resources.recommended
import markor.shared.generated.resources.select_storage_location
import markor.shared.generated.resources.shared_storage
import markor.shared.generated.resources.storage_external_description
import markor.shared.generated.resources.storage_internal_description
import markor.shared.generated.resources.storage_permission_denied
import markor.shared.generated.resources.welcome_to_markor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private enum class IntroStep {
    Welcome,
    Storage,
}

@Composable
fun IntroScreen(
    onIntroFinished: () -> Unit,
    systemInternalFilesDir: String,
    viewModel: IntroViewModel = koinViewModel(),
) {
    val supportsSharedStorage = remember { supportsSharedStorageMode() }
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(IntroStep.Welcome) }
    var requestPermissions by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var isFinishing by remember { mutableStateOf(false) }

    fun finishWithPrivateStorage() {
        if (isFinishing) return
        isFinishing = true
        scope.launch {
            viewModel.setStorageMode(isExternal = false, internalPath = systemInternalFilesDir)
            onIntroFinished()
        }
    }

    com.bernaferrari.remarkor.ui.components.HandleStoragePermissions(
        onRequest = requestPermissions,
        onGranted = {
            if (!isFinishing) {
                isFinishing = true
                scope.launch {
                    viewModel.setStorageMode(isExternal = true, internalPath = systemInternalFilesDir)
                    onIntroFinished()
                    requestPermissions = false
                }
            }
        },
        onDenied = {
            requestPermissions = false
            permissionDenied = true
        },
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (step) {
                IntroStep.Welcome -> WelcomePage(
                    onContinue = {
                        if (supportsSharedStorage) step = IntroStep.Storage
                        else finishWithPrivateStorage()
                    },
                    enabled = !isFinishing,
                )

                IntroStep.Storage -> StorageSelectionPage(
                    onBack = {
                        permissionDenied = false
                        step = IntroStep.Welcome
                    },
                    onPrivateSelected = ::finishWithPrivateStorage,
                    onSharedSelected = {
                        if (!isFinishing) {
                            permissionDenied = false
                            requestPermissions = true
                        }
                    },
                    permissionDenied = permissionDenied,
                    enabled = !isFinishing && !requestPermissions,
                )
            }
        }
    }
}

@Composable
private fun WelcomePage(
    onContinue: () -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NotesPreview()

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(Res.string.welcome_to_markor),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(Res.string.intro_description),
                    modifier = Modifier.widthIn(max = 440.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onContinue,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = stringResource(Res.string.get_started),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Icon(
                    imageVector = MaterialSymbols.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = MaterialSymbols.Filled.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.intro_local_first),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NotesPreview() {
    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    Row(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .height(228.dp)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier.weight(1.08f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PreviewNote(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                accent = MaterialTheme.colorScheme.secondaryContainer,
                border = outline,
                lineFractions = listOf(0.86f, 0.62f, 0.74f),
                showCheck = true,
                showPin = false,
            )
            PreviewNote(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f),
                accent = MaterialTheme.colorScheme.primaryContainer,
                border = outline,
                lineFractions = listOf(0.7f, 0.48f),
                showCheck = false,
                showPin = false,
            )
        }
        Column(
            modifier = Modifier.weight(0.92f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PreviewNote(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.76f),
                accent = MaterialTheme.colorScheme.tertiaryContainer,
                border = outline,
                lineFractions = listOf(0.78f, 0.52f),
                showCheck = false,
                showPin = true,
            )
            PreviewNote(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.24f),
                accent = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = outline,
                lineFractions = listOf(0.9f, 0.66f, 0.8f),
                showCheck = false,
                showPin = false,
            )
        }
    }
}

@Composable
private fun PreviewNote(
    modifier: Modifier,
    accent: Color,
    border: Color,
    lineFractions: List<Float>,
    showCheck: Boolean,
    showPin: Boolean,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.32f).compositeOver(MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, border),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(9.dp)
                        .background(accent, CircleShape),
                )
                if (showPin) {
                    Icon(
                        imageVector = MaterialSymbols.Filled.PushPin,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    )
                }
            }
            lineFractions.forEachIndexed { index, fraction ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    if (showCheck) {
                        Surface(
                            modifier = Modifier.size(14.dp),
                            shape = CircleShape,
                            color = if (index == 0) accent else Color.Transparent,
                            border = BorderStroke(1.dp, border),
                        ) {
                            if (index == 0) {
                                Icon(
                                    imageVector = MaterialSymbols.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.padding(2.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(6.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageSelectionPage(
    onBack: () -> Unit,
    onPrivateSelected: () -> Unit,
    onSharedSelected: () -> Unit,
    permissionDenied: Boolean,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = MaterialSymbols.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.back),
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(Res.string.choose_storage_location),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(Res.string.select_storage_location),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(36.dp))

        StorageOption(
            title = stringResource(Res.string.private_storage),
            description = stringResource(Res.string.storage_internal_description),
            icon = MaterialSymbols.Filled.Lock,
            recommended = true,
            enabled = enabled,
            onClick = onPrivateSelected,
        )

        Spacer(modifier = Modifier.height(12.dp))

        StorageOption(
            title = stringResource(Res.string.shared_storage),
            description = stringResource(Res.string.storage_external_description),
            icon = MaterialSymbols.Filled.FolderOpen,
            recommended = false,
            enabled = enabled,
            onClick = onSharedSelected,
        )

        if (permissionDenied) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.storage_permission_denied),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StorageOption(
    title: String,
    description: String,
    icon: ImageVector,
    recommended: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (recommended) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (recommended) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (recommended) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (recommended) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (recommended) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = stringResource(Res.string.recommended),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = MaterialSymbols.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
