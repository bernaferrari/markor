package com.bernaferrari.remarkor.ui.screens.main

import com.bernaferrari.remarkor.ui.icons.MaterialSymbols

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.ui.components.BackHandler
import com.bernaferrari.remarkor.ui.components.CreateFileDialog
import com.bernaferrari.remarkor.ui.components.EmptyState
import com.bernaferrari.remarkor.ui.components.rememberAdaptiveLayoutInfo
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import com.bernaferrari.remarkor.ui.viewmodel.MainViewModel
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.all
import markor.shared.generated.resources.add_to_favorites
import markor.shared.generated.resources.archive
import markor.shared.generated.resources.back
import markor.shared.generated.resources.choose_file_to_view_edit
import markor.shared.generated.resources.clear_query
import markor.shared.generated.resources.clear_selection
import markor.shared.generated.resources.create_note_to_start_searching
import markor.shared.generated.resources.delete
import markor.shared.generated.resources.favorites
import markor.shared.generated.resources.name
import markor.shared.generated.resources.no_matches
import markor.shared.generated.resources.no_notes_yet
import markor.shared.generated.resources.note_color
import markor.shared.generated.resources.notebook
import markor.shared.generated.resources.oldest_first
import markor.shared.generated.resources.recent_first
import markor.shared.generated.resources.remove_from_favorites
import markor.shared.generated.resources.search_notes
import markor.shared.generated.resources.search_notes_label
import markor.shared.generated.resources.select_all
import markor.shared.generated.resources.select_file
import markor.shared.generated.resources.selected
import markor.shared.generated.resources.settings
import markor.shared.generated.resources.sort
import markor.shared.generated.resources.switch_to_grid_view
import markor.shared.generated.resources.switch_to_list_view
import markor.shared.generated.resources.trash
import markor.shared.generated.resources.try_different_search_term
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsTopBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                stringResource(Res.string.settings),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    MaterialSymbols.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@Composable
internal fun SelectionTopBar(
    selectedCount: Int,
    currentFilterMode: FileFilterMode = FileFilterMode.ALL,
    onFilterModeChange: (FileFilterMode) -> Unit = {},
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    allSelectedAreFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onSetColor: () -> Unit,
    onDelete: () -> Unit,
    showFilterChips: Boolean = false
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = "$selectedCount ${stringResource(Res.string.selected)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        MaterialSymbols.Filled.Close,
                        contentDescription = stringResource(Res.string.clear_selection)
                    )
                }
            },
            actions = {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        MaterialSymbols.Filled.SelectAll,
                        contentDescription = stringResource(Res.string.select_all)
                    )
                }
                if (currentFilterMode != FileFilterMode.TRASH) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (allSelectedAreFavorite) {
                                MaterialSymbols.Filled.Star
                            } else {
                                MaterialSymbols.Outlined.Star
                            },
                            contentDescription = stringResource(
                                if (allSelectedAreFavorite) {
                                    Res.string.remove_from_favorites
                                } else {
                                    Res.string.add_to_favorites
                                }
                            )
                        )
                    }
                    IconButton(onClick = onSetColor) {
                        Icon(
                            MaterialSymbols.Filled.Palette,
                            contentDescription = stringResource(Res.string.note_color)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        MaterialSymbols.Filled.Delete,
                        contentDescription = stringResource(Res.string.delete)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        if (showFilterChips) {
            FilterTabRow(
                currentFilterMode = currentFilterMode,
                onFilterModeChange = onFilterModeChange
            )
        }
    }
}

@Composable
internal fun FilterTabRow(
    currentFilterMode: FileFilterMode,
    onFilterModeChange: (FileFilterMode) -> Unit
) {
    val hapticHelper = rememberHapticHelper()

    val filters = listOf(
        Triple(
            FileFilterMode.ALL,
            Res.string.all,
            MaterialSymbols.AutoMirrored.Outlined.List to MaterialSymbols.AutoMirrored.Filled.List
        ),
        Triple(
            FileFilterMode.FAVORITES,
            Res.string.favorites,
            MaterialSymbols.Outlined.Star to MaterialSymbols.Filled.Star
        ),
        Triple(
            FileFilterMode.ARCHIVE,
            Res.string.archive,
            MaterialSymbols.Outlined.Archive to MaterialSymbols.Filled.Archive
        ),
        Triple(
            FileFilterMode.TRASH,
            Res.string.trash,
            MaterialSymbols.Outlined.Delete to MaterialSymbols.Filled.Delete
        )
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filters.forEachIndexed { index, (mode, labelRes, icons) ->
                val isSelected = mode == currentFilterMode
                ToggleButton(
                    modifier = Modifier.weight(1f),
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        if (checked && mode != currentFilterMode) {
                            hapticHelper.performLightClick()
                            onFilterModeChange(mode)
                        }
                    },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = if (isSelected) icons.second else icons.first,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
internal fun StandardTopBar(
    isGridView: Boolean,
    currentFilterMode: FileFilterMode,
    currentSortOrder: String,
    onFilterModeChange: (FileFilterMode) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onToggleGridView: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
                Surface(
                    onClick = onOpenSearch,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.search_notes_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onToggleGridView) {
                    Icon(
                        if (isGridView) MaterialSymbols.AutoMirrored.Filled.List else MaterialSymbols.Filled.GridView,
                        contentDescription = if (isGridView) stringResource(Res.string.switch_to_list_view) else stringResource(
                            Res.string.switch_to_grid_view
                        )
                    )
                }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            MaterialSymbols.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(Res.string.sort)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.recent_first)) },
                            leadingIcon = {
                                if (currentSortOrder == "date") Icon(
                                    MaterialSymbols.Filled.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onSortOrderChange("date")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.oldest_first)) },
                            leadingIcon = {
                                if (currentSortOrder == "oldest") Icon(
                                    MaterialSymbols.Filled.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onSortOrderChange("oldest")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.name)) },
                            leadingIcon = {
                                if (currentSortOrder == "name") Icon(
                                    MaterialSymbols.Filled.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onSortOrderChange("name")
                                showSortMenu = false
                            }
                        )
                    }
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        MaterialSymbols.Filled.Settings,
                        contentDescription = stringResource(Res.string.settings)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        FilterTabRow(
            currentFilterMode = currentFilterMode,
            onFilterModeChange = onFilterModeChange
        )
    }
}
