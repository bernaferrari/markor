package com.bernaferrari.remarkor.ui.screens.editor

import com.bernaferrari.remarkor.ui.icons.MaterialSymbols

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.document_info
import markor.shared.generated.resources.document_outline
import markor.shared.generated.resources.export
import markor.shared.generated.resources.archive
import markor.shared.generated.resources.more_options
import markor.shared.generated.resources.unarchive
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditorTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    isPreviewMode: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    hasOutline: Boolean,
    isFocusMode: Boolean,
    isArchived: Boolean = false,
    embeddedInDialog: Boolean = false,
    onBack: () -> Unit,
    onTogglePreview: () -> Unit,
    onShowColorSheet: () -> Unit,
    onArchive: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFindReplace: () -> Unit,
    onShowOutline: () -> Unit,
    onShowDocumentInfo: () -> Unit,
    onExport: () -> Unit,
    onToggleFocusMode: () -> Unit,
) {
    TopAppBar(
        title = { Spacer(modifier = Modifier) },
        windowInsets = if (embeddedInDialog) WindowInsets(0, 0, 0, 0) else TopAppBarDefaults.windowInsets,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = if (embeddedInDialog) MaterialSymbols.Filled.Close else MaterialSymbols.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (embeddedInDialog) "Close" else "Back",
                )
            }
        },
        actions = {
            IconButton(
                onClick = onTogglePreview,
                shape = MaterialTheme.shapes.medium,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    imageVector = if (!isPreviewMode) MaterialSymbols.Filled.Visibility else MaterialSymbols.Filled.EditNote,
                    contentDescription = if (isPreviewMode) "Edit" else "Preview",
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onShowColorSheet,
                shape = MaterialTheme.shapes.medium,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(MaterialSymbols.Filled.Palette, contentDescription = "Color")
            }

            Spacer(modifier = Modifier.width(4.dp))

            var showOverflowMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { showOverflowMenu = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        MaterialSymbols.Filled.MoreVert,
                        contentDescription = stringResource(Res.string.more_options),
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(if (isArchived) Res.string.unarchive else Res.string.archive))
                        },
                        leadingIcon = { Icon(MaterialSymbols.Filled.Archive, null) },
                        onClick = {
                            showOverflowMenu = false
                            onArchive()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Undo") },
                        leadingIcon = { Icon(MaterialSymbols.AutoMirrored.Filled.Undo, null) },
                        onClick = {
                            onUndo()
                            showOverflowMenu = false
                        },
                        enabled = canUndo,
                    )
                    DropdownMenuItem(
                        text = { Text("Redo") },
                        leadingIcon = { Icon(MaterialSymbols.AutoMirrored.Filled.Redo, null) },
                        onClick = {
                            onRedo()
                            showOverflowMenu = false
                        },
                        enabled = canRedo,
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Find & Replace") },
                        leadingIcon = { Icon(MaterialSymbols.Filled.Search, null) },
                        onClick = {
                            onFindReplace()
                            showOverflowMenu = false
                        },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.document_outline)) },
                        leadingIcon = { Icon(MaterialSymbols.AutoMirrored.Filled.List, null) },
                        onClick = {
                            onShowOutline()
                            showOverflowMenu = false
                        },
                        enabled = hasOutline,
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.document_info)) },
                        leadingIcon = { Icon(MaterialSymbols.Filled.Info, null) },
                        onClick = {
                            onShowDocumentInfo()
                            showOverflowMenu = false
                        },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.export)) },
                        leadingIcon = { Icon(MaterialSymbols.Filled.Share, null) },
                        onClick = {
                            onExport()
                            showOverflowMenu = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(if (isFocusMode) "Exit Focus Mode" else "Focus Mode") },
                        leadingIcon = { Icon(MaterialSymbols.Filled.CenterFocusStrong, null) },
                        onClick = {
                            onToggleFocusMode()
                            showOverflowMenu = false
                        },
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}
