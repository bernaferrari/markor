
package com.bernaferrari.remarkor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.bernaferrari.remarkor.ui.components.BackHandler
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.components.supportsSharedStorageMode
import com.bernaferrari.remarkor.ui.viewmodel.SettingsViewModel
import com.bernaferrari.remarkor.ui.viewmodel.ThemeModeOption
import com.bernaferrari.remarkor.ui.components.RenameDialog
import com.bernaferrari.remarkor.ui.theme.dynamicColorScheme
import com.bernaferrari.remarkor.ui.theme.MarkorTheme
import com.bernaferrari.remarkor.ui.theme.ThemePaletteOption
import androidx.compose.foundation.isSystemInDarkTheme
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import markor.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel

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
            SettingsSection(title = stringResource(Res.string.appearance)) {
                ThemeModeSettingItem(
                    selectedMode = themeMode,
                    onSelectMode = viewModel::setThemeMode
                )
                Spacer(modifier = Modifier.height(4.dp))
                ThemeColorSettingItem(
                    selectedPalette = themePalette,
                    onSelectPalette = viewModel::setThemePalette
                )
            }

            // Editor Section
            val editorItems = listOf<SegmentedSectionItem>(
                { index, itemCount ->
                    SwitchSettingItem(
                        title = stringResource(Res.string.line_numbers),
                        subtitle = stringResource(Res.string.display_line_numbers),
                        checked = showLineNumbers,
                        onCheckedChange = { viewModel.setShowLineNumbers(it) },
                        index = index,
                        itemCount = itemCount
                    )
                },
                { index, itemCount ->
                    SwitchSettingItem(
                        title = stringResource(Res.string.word_wrap),
                        subtitle = stringResource(Res.string.wrap_text_to_fit_screen),
                        checked = wordWrap,
                        onCheckedChange = { viewModel.setWordWrap(it) },
                        index = index,
                        itemCount = itemCount
                    )
                },
                { index, itemCount ->
                    SwitchSettingItem(
                        title = stringResource(Res.string.auto_format),
                        subtitle = stringResource(Res.string.format_while_typing),
                        checked = autoFormat,
                        onCheckedChange = { viewModel.setAutoFormat(it) },
                        index = index,
                        itemCount = itemCount
                    )
                },
                { index, itemCount ->
                    FontSizeSettingItem(
                        value = editorFontSize,
                        onValueChange = { viewModel.setEditorFontSize(it.toInt()) },
                        index = index,
                        itemCount = itemCount
                    )
                }
            )
            SegmentedSettingsSection(
                title = stringResource(Res.string.editor),
                items = editorItems
            )
            Spacer(modifier = Modifier.height(MarkorTheme.spacing.small))

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

            val storageItems = listOf<SegmentedSectionItem>(
                { index, itemCount ->
                    if (supportsSharedStorage) {
                        StorageModeSettingItem(
                            isExternalStorageEnabled = isExternalStorageEnabled,
                            onSwitchMode = { viewModel.switchStorageMode(it) },
                            index = index,
                            itemCount = itemCount
                        )
                    } else {
                        InfoSettingItem(
                            title = stringResource(Res.string.storage_mode),
                            value = "Private (iOS)",
                            index = index,
                            itemCount = itemCount
                        )
                    }
                },
                { index, itemCount ->
                    ClickableSettingItem(
                        title = stringResource(Res.string.notebook_directory),
                        subtitle = notebookDirectory.ifEmpty { "Default (Documents/Markor)" },
                        onClick = {
                            editingStorageKey = "notebook"
                            currentStorageValue = notebookDirectory
                        },
                        index = index,
                        itemCount = itemCount
                    )
                }
            )
            SegmentedSettingsSection(
                title = stringResource(Res.string.storage),
                items = storageItems
            )
            Spacer(modifier = Modifier.height(MarkorTheme.spacing.small))
             
            // About Section
            val aboutItems = listOf<SegmentedSectionItem>(
                { index, itemCount ->
                    InfoSettingItem(
                        title = stringResource(Res.string.version),
                        value = "2.15.2-Expressive",
                        index = index,
                        itemCount = itemCount
                    )
                },
                { index, itemCount ->
                    ClickableSettingItem(
                        title = stringResource(Res.string.project_license),
                        subtitle = "Apache License 2.0",
                        onClick = { showProjectLicenseDialog = true },
                        index = index,
                        itemCount = itemCount
                    )
                }
            )
            SegmentedSettingsSection(
                title = stringResource(Res.string.about),
                items = aboutItems
            )
            
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

private typealias SegmentedSectionItem = @Composable (Int, Int) -> Unit

@Composable
private fun SettingsSection(
    title: String,
    segmented: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                start = MarkorTheme.spacing.medium,
                bottom = MarkorTheme.spacing.medium
            )
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                if (segmented) ListItemDefaults.SegmentedGap else 0.dp
            )
        ) {
            content()
        }
    }
}

@Composable
private fun SegmentedSettingsSection(
    title: String,
    items: List<SegmentedSectionItem>
) {
    SettingsSection(title = title, segmented = true) {
        items.forEachIndexed { index, item ->
            item(index, items.size)
        }
    }
}

@Composable
private fun segmentedItemColors(): ListItemColors =
    ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

@Composable
private fun segmentedShapes(index: Int, itemCount: Int): ListItemShapes =
    ListItemDefaults.segmentedShapes(index = index, count = itemCount)

@Composable
private fun SegmentedSettingSurface(
    index: Int,
    itemCount: Int,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = segmentedShapes(index = index, itemCount = itemCount).shape

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
    index: Int,
    itemCount: Int
) {
    val hapticHelper = rememberHapticHelper()
    val onToggle: (Boolean) -> Unit = {
        hapticHelper.performLightClick()
        onCheckedChange(it)
    }

    SegmentedListItem(
        checked = checked,
        onCheckedChange = onToggle,
        shapes = segmentedShapes(index = index, itemCount = itemCount),
        colors = segmentedItemColors(),
        supportingContent = {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        content = {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        }
    )
}

@Composable
private fun InfoSettingItem(
    title: String,
    value: String,
    index: Int,
    itemCount: Int
) {
    SegmentedSettingSurface(index = index, itemCount = itemCount) {
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
    index: Int,
    itemCount: Int
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = segmentedShapes(index = index, itemCount = itemCount),
        colors = segmentedItemColors(),
        supportingContent = {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        content = {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        }
    )
}

@Composable
private fun FontSizeSettingItem(
    value: Int,
    onValueChange: (Float) -> Unit,
    index: Int,
    itemCount: Int
) {
    SegmentedSettingSurface(index = index, itemCount = itemCount) {
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
    index: Int,
    itemCount: Int
) {
    val hapticHelper = rememberHapticHelper()

    SegmentedSettingSurface(index = index, itemCount = itemCount) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(Res.string.storage_mode),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                ToggleButton(
                    checked = !isExternalStorageEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked && isExternalStorageEnabled) {
                            hapticHelper.performLightClick()
                            onSwitchMode(false)
                        }
                    },
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = if (isExternalStorageEnabled) Icons.Outlined.Lock else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(Res.string.private),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                ToggleButton(
                    checked = isExternalStorageEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked && !isExternalStorageEnabled) {
                            hapticHelper.performLightClick()
                            onSwitchMode(true)
                        }
                    },
                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = if (!isExternalStorageEnabled) Icons.Outlined.Folder else Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(Res.string.shared),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeSettingItem(
    selectedMode: ThemeModeOption,
    onSelectMode: (ThemeModeOption) -> Unit
) {
    val hapticHelper = rememberHapticHelper()

    val modeOptions = listOf(
        Triple(ThemeModeOption.AUTO, Res.string.auto, Icons.Default.BrightnessAuto),
        Triple(ThemeModeOption.LIGHT, Res.string.light, Icons.Default.LightMode),
        Triple(ThemeModeOption.DARK, Res.string.dark, Icons.Default.DarkMode)
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
            Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            modeOptions.forEachIndexed { modeIndex, (mode, label, icon) ->
                ToggleButton(
                    checked = mode == selectedMode,
                    onCheckedChange = { isChecked ->
                        if (isChecked && mode != selectedMode) {
                            hapticHelper.performLightClick()
                            onSelectMode(mode)
                        }
                    },
                    shapes = when (modeIndex) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        modeOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(label),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeColorSettingItem(
    selectedPalette: ThemePaletteOption,
    onSelectPalette: (ThemePaletteOption) -> Unit,
) {
    val hapticHelper = rememberHapticHelper()

    val paletteChoices = listOf(
        ThemePaletteOption.DYNAMIC,
        ThemePaletteOption.BLUE,
        ThemePaletteOption.PURPLE,
        ThemePaletteOption.INDIGO,
        ThemePaletteOption.TEAL,
        ThemePaletteOption.GREEN,
        ThemePaletteOption.LIME,
        ThemePaletteOption.AMBER,
        ThemePaletteOption.ORANGE,
        ThemePaletteOption.RED,
        ThemePaletteOption.PINK
    )
    val dynamicColor = dynamicColorScheme(isSystemInDarkTheme())?.primary
        ?: MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            paletteChoices.forEach { choice ->
                ThemeSwatch(
                    theme = choice.token,
                    seedColor = choice.seedColor,
                    isDynamic = choice == ThemePaletteOption.DYNAMIC,
                    isSelected = selectedPalette == choice,
                    isEnabled = true,
                    dynamicColor = if (choice == ThemePaletteOption.DYNAMIC) dynamicColor else MaterialTheme.colorScheme.primary,
                    onClick = {
                        if (selectedPalette != choice) {
                            hapticHelper.performLightClick()
                            onSelectPalette(choice)
                        }
                    },
                    contentDescription = stringResource(
                        when (choice) {
                            ThemePaletteOption.DYNAMIC -> Res.string.dynamic
                            ThemePaletteOption.BLUE -> Res.string.blue
                            ThemePaletteOption.RED -> Res.string.red
                            ThemePaletteOption.ORANGE -> Res.string.orange
                            ThemePaletteOption.PURPLE -> Res.string.purple
                            ThemePaletteOption.AMBER -> Res.string.amber
                            ThemePaletteOption.GREEN -> Res.string.green
                            ThemePaletteOption.TEAL -> Res.string.teal
                            ThemePaletteOption.PINK -> Res.string.pink
                            ThemePaletteOption.INDIGO -> Res.string.indigo
                            ThemePaletteOption.LIME -> Res.string.lime
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun ThemeSwatch(
    theme: String,
    seedColor: Color?,
    isDynamic: Boolean,
    isSelected: Boolean,
    isEnabled: Boolean,
    dynamicColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val baseColor = seedColor ?: dynamicColor
    val displayColor = if (isEnabled) baseColor else baseColor.copy(alpha = 0.42f)
    val tokenSize = 50.dp
    val glowSize = 56.dp
    val orbSize = 40.dp

    val orbCornerFraction by animateFloatAsState(
        targetValue = if (isSelected) 0.5f else 0.26f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 520f),
        label = "orbCorner_$theme"
    )
    val orbShape = RoundedCornerShape(percent = (orbCornerFraction * 100).roundToInt())

    val orbScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 0.86f,
        animationSpec = spring(dampingRatio = 0.56f, stiffness = 600f),
        label = "orbScale_$theme"
    )
    val orbRotation by animateFloatAsState(
        targetValue = if (isSelected) 8f else 0f,
        animationSpec = spring(dampingRatio = 0.66f, stiffness = 420f),
        label = "orbRotation_$theme"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "glow_$theme"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (isSelected || isDynamic) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "icon_$theme"
    )
    val overallAlpha = if (isEnabled) 1f else 0.52f

    Box(
        modifier = Modifier
            .size(tokenSize)
            .graphicsLayer { alpha = overallAlpha }
            .clickable(
                enabled = isEnabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.RadioButton
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(glowSize)
                .graphicsLayer {
                    alpha = glowAlpha
                }
                .drawBehind {
                    drawCircle(
                        color = displayColor.copy(alpha = 0.44f),
                        radius = size.minDimension * 0.5f
                    )
                }
        )

        Box(
            modifier = Modifier
                .size(orbSize)
                .graphicsLayer {
                    scaleX = orbScale
                    scaleY = orbScale
                    rotationZ = orbRotation
                }
                .clip(orbShape)
                .background(displayColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                            start = Offset.Zero,
                            end = Offset(60f, 60f),
                        ),
                    ),
            )

            if (isSelected || isDynamic) {
                Icon(
                    contentDescription = null,
                    modifier = Modifier
                        .size(if (isSelected) 18.dp else 16.dp)
                        .graphicsLayer {
                            alpha = iconAlpha
                            rotationZ = -orbRotation
                        },
                    imageVector = when {
                        isSelected -> Icons.Default.Check
                        isDynamic -> Icons.Default.Palette
                        else -> Icons.Default.Check
                    },
                    tint = Color.White.copy(alpha = 1f),
                )
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
