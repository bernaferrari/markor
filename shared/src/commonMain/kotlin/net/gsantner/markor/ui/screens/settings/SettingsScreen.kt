package net.gsantner.markor.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.gsantner.markor.ui.components.BackHandler
import net.gsantner.markor.ui.components.supportsSharedStorageMode
import net.gsantner.markor.ui.viewmodel.SettingsViewModel
import net.gsantner.markor.ui.viewmodel.ThemeModeOption
import net.gsantner.markor.ui.viewmodel.ThemePaletteOption
import net.gsantner.markor.ui.components.RenameDialog
import net.gsantner.markor.ui.theme.MarkorTheme
import org.jetbrains.compose.resources.stringResource
import net.gsantner.markor.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val showLineNumbers by viewModel.showLineNumbers.collectAsState()
    val wordWrap by viewModel.wordWrap.collectAsState()
    val autoFormat by viewModel.autoFormat.collectAsState()
    val editorFontSize by viewModel.editorFontSize.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val themePalette by viewModel.themePalette.collectAsState()

    val notebookDirectory by viewModel.notebookDirectory.collectAsState()
    val isExternalStorageEnabled by viewModel.isExternalStorageEnabled.collectAsState()
    val supportsSharedStorage = remember { supportsSharedStorageMode() }

    LaunchedEffect(supportsSharedStorage, isExternalStorageEnabled) {
        if (!supportsSharedStorage && isExternalStorageEnabled) {
            viewModel.switchStorageMode(useSharedStorage = false)
        }
    }

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.settings),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    }
                )
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(
                    start = MarkorTheme.spacing.large,
                    end = MarkorTheme.spacing.large,
                    top = MarkorTheme.spacing.large
                ),
            verticalArrangement = Arrangement.spacedBy(MarkorTheme.spacing.medium)
        ) {
            SettingsSection(title = stringResource(Res.string.appearance), icon = Icons.Default.Palette) {
                ThemeModeSettingItem(
                    selectedMode = themeMode,
                    onSelectMode = viewModel::setThemeMode,
                    position = SettingItemPosition.First
                )
                ThemeColorSettingItem(
                    selectedPalette = themePalette,
                    onSelectPalette = viewModel::setThemePalette,
                    position = SettingItemPosition.Last
                )
            }

            // Editor Section
            SettingsSection(title = stringResource(Res.string.editor), icon = Icons.Default.Edit) {
                SwitchSettingItem(
                    title = stringResource(Res.string.line_numbers),
                    subtitle = stringResource(Res.string.display_line_numbers),
                    checked = showLineNumbers,
                    onCheckedChange = { viewModel.setShowLineNumbers(it) },
                    position = SettingItemPosition.First
                )
                SwitchSettingItem(
                    title = stringResource(Res.string.word_wrap),
                    subtitle = stringResource(Res.string.wrap_text_to_fit_screen),
                    checked = wordWrap,
                    onCheckedChange = { viewModel.setWordWrap(it) },
                    position = SettingItemPosition.Middle
                )
                SwitchSettingItem(
                    title = stringResource(Res.string.auto_format),
                    subtitle = stringResource(Res.string.format_while_typing),
                    checked = autoFormat,
                    onCheckedChange = { viewModel.setAutoFormat(it) },
                    position = SettingItemPosition.Middle
                )
                FontSizeSettingItem(
                    value = editorFontSize,
                    onValueChange = { viewModel.setEditorFontSize(it.toInt()) },
                    position = SettingItemPosition.Last
                )
            }

            // Storage Section
            var editingStorageKey by remember { mutableStateOf<String?>(null) }
            var currentStorageValue by remember { mutableStateOf("") }
            var showProjectLicenseDialog by remember { mutableStateOf(false) }

            if (editingStorageKey != null) {
                RenameDialog(
                    currentName = currentStorageValue,
                    onDismiss = { editingStorageKey = null },
                    onConfirm = { newValue ->
                        when(editingStorageKey) {
                            "notebook" -> viewModel.setNotebookDirectory(newValue)
                        }
                        editingStorageKey = null
                    }
                )
            }

            if (showProjectLicenseDialog) {
                LicenseDialog(
                    title = stringResource(Res.string.project_license),
                    body = PROJECT_LICENSE_TEXT,
                    onDismiss = { showProjectLicenseDialog = false }
                )
            }

            SettingsSection(title = stringResource(Res.string.storage), icon = Icons.Default.SdStorage) {
                if (supportsSharedStorage) {
                    StorageModeSettingItem(
                        isExternalStorageEnabled = isExternalStorageEnabled,
                        onSwitchMode = { viewModel.switchStorageMode(it) },
                        position = SettingItemPosition.First
                    )
                } else {
                    InfoSettingItem(
                        title = stringResource(Res.string.storage_mode),
                        value = "Private (iOS)",
                        position = SettingItemPosition.First
                    )
                }

                ClickableSettingItem(
                    title = stringResource(Res.string.notebook_directory),
                    subtitle = notebookDirectory.ifEmpty { "Default (Documents/Markor)" },
                    onClick = { 
                        editingStorageKey = "notebook"
                        currentStorageValue = notebookDirectory
                    },
                    position = SettingItemPosition.Last
                )
            }
             
            // About Section
            SettingsSection(title = stringResource(Res.string.about), icon = Icons.Default.Info) {
                InfoSettingItem(
                    title = stringResource(Res.string.version),
                    value = "2.15.2-Expressive",
                    position = SettingItemPosition.First
                )
                ClickableSettingItem(
                    title = stringResource(Res.string.project_license),
                    subtitle = "Apache License 2.0",
                    onClick = { showProjectLicenseDialog = true },
                    position = SettingItemPosition.Last
                )
            }
            
            // Keep final items above gesture/navigation area while preserving edge-to-edge.
            Spacer(modifier = Modifier.height(MarkorTheme.spacing.small))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

private const val PROJECT_LICENSE_TEXT = """
Markor Compose Port

Copyright 2017-2025 Gregor Santner

Licensed under the Apache License, Version 2.0.
You may obtain a copy of the License at:
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied.

See LICENSE.txt in the project root for the full license text.
"""

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = MarkorTheme.spacing.medium, bottom = MarkorTheme.spacing.medium)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(MarkorTheme.spacing.medium))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}

private enum class SettingItemPosition {
    Single, First, Middle, Last
}

private fun settingItemShape(position: SettingItemPosition): Shape = when (position) {
    SettingItemPosition.Single -> RoundedCornerShape(22.dp)
    SettingItemPosition.First -> RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    SettingItemPosition.Middle -> RoundedCornerShape(8.dp)
    SettingItemPosition.Last -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
}

@Composable
private fun SettingsItemContainer(
    position: SettingItemPosition,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = settingItemShape(position)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    } else {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    position: SettingItemPosition
) {
    SettingsItemContainer(position = position) {
        ListItem(
            headlineContent = {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            },
            supportingContent = {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun InfoSettingItem(
    title: String,
    value: String,
    position: SettingItemPosition
) {
    SettingsItemContainer(position = position) {
        ListItem(
            headlineContent = {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            },
            supportingContent = {
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun ClickableSettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    position: SettingItemPosition
) {
    SettingsItemContainer(
        position = position,
        onClick = onClick
    ) {
        ListItem(
            headlineContent = {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            },
            supportingContent = {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun FontSizeSettingItem(
    value: Int,
    onValueChange: (Float) -> Unit,
    position: SettingItemPosition
) {
    SettingsItemContainer(position = position) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.font_size),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${value}sp",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = value.toFloat(),
                onValueChange = onValueChange,
                valueRange = 12f..24f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun StorageModeSettingItem(
    isExternalStorageEnabled: Boolean,
    onSwitchMode: (Boolean) -> Unit,
    position: SettingItemPosition
) {
    SettingsItemContainer(position = position) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(Res.string.storage_mode),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isExternalStorageEnabled,
                    onClick = { onSwitchMode(false) },
                    label = { Text(stringResource(Res.string.private)) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (!isExternalStorageEnabled) Icons.Filled.Lock else Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
                FilterChip(
                    selected = isExternalStorageEnabled,
                    onClick = { onSwitchMode(true) },
                    label = { Text(stringResource(Res.string.shared)) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isExternalStorageEnabled) Icons.Filled.FolderShared else Icons.Outlined.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeModeSettingItem(
    selectedMode: ThemeModeOption,
    onSelectMode: (ThemeModeOption) -> Unit,
    position: SettingItemPosition
) {
    SettingsItemContainer(position = position) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(Res.string.theme_mode),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedMode == ThemeModeOption.AUTO,
                    onClick = { onSelectMode(ThemeModeOption.AUTO) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.auto), textAlign = TextAlign.Center)
                }
                SegmentedButton(
                    selected = selectedMode == ThemeModeOption.LIGHT,
                    onClick = { onSelectMode(ThemeModeOption.LIGHT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.light), textAlign = TextAlign.Center)
                }
                SegmentedButton(
                    selected = selectedMode == ThemeModeOption.DARK,
                    onClick = { onSelectMode(ThemeModeOption.DARK) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.dark), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ThemeColorSettingItem(
    selectedPalette: ThemePaletteOption,
    onSelectPalette: (ThemePaletteOption) -> Unit,
    position: SettingItemPosition
) {
    val paletteOptions = listOf(
        Triple(ThemePaletteOption.MARKOR, stringResource(Res.string.markor), Color(0xFF4A6FA5)),
        Triple(ThemePaletteOption.RED, stringResource(Res.string.red), Color(0xFFB3261E)),
        Triple(ThemePaletteOption.ORANGE, stringResource(Res.string.orange), Color(0xFFB35A00)),
        Triple(ThemePaletteOption.GREEN, stringResource(Res.string.green), Color(0xFF2E7D32)),
        Triple(ThemePaletteOption.TEAL, stringResource(Res.string.teal), Color(0xFF006A6A))
    )

    SettingsItemContainer(position = position) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(Res.string.theme_color),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paletteOptions.forEach { (option, label, dotColor) ->
                    FilterChip(
                        selected = selectedPalette == option,
                        onClick = { onSelectPalette(option) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(label)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LicenseDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = body.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close))
            }
        }
    )
}
