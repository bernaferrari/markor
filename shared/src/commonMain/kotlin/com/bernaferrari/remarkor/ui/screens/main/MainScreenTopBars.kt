package com.bernaferrari.remarkor.ui.screens.main

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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarOutline
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
                    Icons.AutoMirrored.Filled.ArrowBack,
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
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.clear_selection)
                    )
                }
            },
            actions = {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        Icons.Default.SelectAll,
                        contentDescription = stringResource(Res.string.select_all)
                    )
                }
                if (currentFilterMode != FileFilterMode.TRASH) {
                    IconButton(onClick = onSetColor) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = stringResource(Res.string.note_color)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
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
            Icons.AutoMirrored.Outlined.List to Icons.AutoMirrored.Filled.List
        ),
        Triple(
            FileFilterMode.FAVORITES,
            Res.string.favorites,
            Icons.Outlined.StarOutline to Icons.Default.Star
        ),
        Triple(
            FileFilterMode.ARCHIVE,
            Res.string.archive,
            Icons.Outlined.Archive to Icons.Default.Archive
        ),
        Triple(
            FileFilterMode.TRASH,
            Res.string.trash,
            Icons.Outlined.Delete to Icons.Default.Delete
        )
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filters.forEachIndexed { index, (mode, labelRes, icons) ->
                val isSelected = mode == currentFilterMode
                ToggleButton(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenSearch),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
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
                        if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                        contentDescription = if (isGridView) stringResource(Res.string.switch_to_list_view) else stringResource(
                            Res.string.switch_to_grid_view
                        )
                    )
                }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
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
                                    Icons.Default.Check,
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
                                    Icons.Default.Check,
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
                                    Icons.Default.Check,
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
                        Icons.Default.Settings,
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
