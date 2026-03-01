package com.bernaferrari.remarkor.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import kotlinx.coroutines.launch

@Composable
fun MainNavigationDrawer(
    drawerState: DrawerState,
    currentFilterMode: FileFilterMode,
    labels: List<com.bernaferrari.remarkor.data.local.db.LabelEntity>,
    currentLabel: String?,
    onSelectFilterMode: (FileFilterMode) -> Unit,
    onSelectLabel: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))

                Text(
                    "Markor",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                NavigationDrawerItem(
                    label = { Text("Notes") },
                    icon = { Icon(Icons.Outlined.Description, null) },
                    selected = currentFilterMode == FileFilterMode.ALL,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSelectFilterMode(FileFilterMode.ALL)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Archive") },
                    icon = { Icon(Icons.Outlined.Archive, null) },
                    selected = currentFilterMode == FileFilterMode.ARCHIVE,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSelectFilterMode(FileFilterMode.ARCHIVE)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Trash") },
                    icon = { Icon(Icons.Outlined.Delete, null) },
                    selected = currentFilterMode == FileFilterMode.TRASH,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSelectFilterMode(FileFilterMode.TRASH)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    icon = { Icon(Icons.Outlined.Settings, null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        content = content
    )
}
